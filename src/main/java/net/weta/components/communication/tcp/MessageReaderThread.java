/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.net.SocketException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.server.TooManyRunningThreads;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.MessageSizeTooBigException;
import net.weta.components.communication.tcp.server.IMessageSender;
import net.weta.components.communication.util.PooledThreadExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageReaderThread extends Thread {

    protected static final Logger LOG = LogManager.getLogger(MessageReaderThread.class);

    protected final MessageQueue _messageQueue;

    protected final String _peerName;

    protected final IMessageSender _messageSender;

    protected int _threadCount = 0;

    protected final int _maxThreadCount;

    private final IInput _in;
    
    private Map<String, Future<?>> _futures = new ConcurrentHashMap<String, Future<?>>();

    private class WaitForAnswerRunnable implements Runnable {

    	Message message = null;
    	String tracker = null;
    	
		public WaitForAnswerRunnable(Message message) {
			this.message = message;
			this.tracker = message.getId();
		}
    	
    	@Override
		public void run() {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Send message [" + message.getId() + "] to messager queue. Thread [" + Thread.currentThread().getName() + "].");
                }
                Message answer = _messageQueue.messageEvent(message);
                if (answer != null) {
                    answer.setType("");
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Got answer [" + answer.getId() + "], type [" + answer + "] for message [" + message.getId() + "]. Send message to peer [" + _peerName + "]");
                        }
                        _messageSender.sendMessage(_peerName, answer);
                    } catch (IOException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("can not send answer message to [" + _peerName + "]: " + e.getMessage());
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Stacktrace:", e);
                        }
                    }
                }
            } catch (Throwable t) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Unexpected interruption of sending message [" + message + "] to peer [" + _peerName + "].", t);
                }
            }
            finally {
            	Future<?> f = _futures.remove(this.tracker);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove future from future list by message [" + this.tracker + "]. Thread [" + Thread.currentThread().getName() + "].");
                }
            	
            	if (f != null) {
            	    // DO NOT cancel the future because this causes an InterruptedException in MessageQueue.waitForMessage() 
            	    if (!f.isDone()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Future [" + f.toString() + "] for message [" + this.tracker + "] is NOT done yet.");
                        }
            	    }
            	}
            	this.message = null;
                _threadCount--;
            }		
         }
    }
    
    
    public MessageReaderThread(String peerName, IInput in, MessageQueue messageQueue, IMessageSender messageSender,
            int maxThreadCount) {
        _peerName = peerName;
        _in = in;
        _messageQueue = messageQueue;
        _messageSender = messageSender;
        _maxThreadCount = maxThreadCount;
    }
    
    public void run() {
        try {
        	long nMessages = 0;
        	long startMillis = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info("start to read messages for peer: [" + _peerName + "]");
            }

            while (!isInterrupted()) {
                Message message = (Message) _in.readObject();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Read message [" + message.getId() + "] of type [" + message.getType() + "] and id [" + message + "] for client [" + _peerName + "]");
                }
                if (LOG.isInfoEnabled()) {
                    nMessages++;
                    if (nMessages % 500 == 0) {
                    	LOG.info("Number of messages for peer [" + _peerName + "]: " + nMessages + " (" + (nMessages*1.0/(System.currentTimeMillis()-startMillis)*1000*60) + " msg/min) since " + new Date(startMillis) + ". Number of running WaitForAnswerRunnable tasks: " + _futures.size());
                    }
                }
                waitForAnswer(message);
            }

        } catch (SocketException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("connection shutdown by peer (SocketException, " + e.getMessage() + "): " + _peerName);
            }
            if (_messageSender != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Try to reconnect to peer: " + _peerName);
                }
                _messageSender.connect(_peerName);
            }
        } catch (EOFException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("connection shutdown by peer (EOFException, " + e.getMessage() + "): " + _peerName);
            }
            if (_messageSender != null) {
                _messageSender.connect(_peerName);
            }
        } catch (MessageSizeTooBigException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("MessageSizeTooBigException for peer: " + _peerName, e);
            }
            if (_messageSender != null) {
                // disconnect in case of client
                _messageSender.disconnect(_peerName);
                // connect in case of client and server 
                _messageSender.connect(_peerName);
            }
        } catch (OptionalDataException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Optional Data Exception: " + _peerName, e);
                LOG.error("There is no more data in the buffered part of the stream: " + e.eof);
                LOG.error("The number of bytes of primitive data available to be read in the current buffer: " + e.length);
            }
            if (_messageSender != null) {
                // disconnect in case of client
                _messageSender.disconnect(_peerName);
                // connect in case of client and server 
                _messageSender.connect(_peerName);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("error while consuming messages for peer: " + _peerName, e);
            }
            if (_messageSender != null) {
                // disconnect in case of client
                _messageSender.disconnect(_peerName);
                // connect in case of client and server
                _messageSender.connect(_peerName);
            }
        } 
    }

    private void waitForAnswer(final Message message) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Thread with name " + this.getName() + " is waiting for answer of message [" + message.getId()
                    + "] for client [" + _peerName + "]");
        }

        if (_threadCount >= _maxThreadCount) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("message not handled because, max thread count reached: " + _maxThreadCount);
            }
            throw new TooManyRunningThreads("No more threads available.");
        }

        WaitForAnswerRunnable runnable = new WaitForAnswerRunnable(message);

        // use thread pool here
        Future<?> f = PooledThreadExecutor.submit(runnable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executed WaitForAnswerRunnable [" + runnable.toString() + "] in future [" + f.toString() + "] in thread [" + this.getName() + "] for message [" + message.getId()
                    + "] for client [" + _peerName + "]: " + runnable.toString());
        }
       	if (!f.isDone()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Future [" + f.toString() + "] is not done. Place in future list for message [" + message.getId() + "].");
            }
       		_futures.put(message.getId(), f);
       		// if the task is already done just after the task was added to the future map
       		// remove it. We don't want any finished tasks at this point. Otherwise they will 
       		// not get removed anymore. See WaitForAnswerRunnable.run() in finally method.
       		if (f.isDone()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Future [" + f.toString() + "] already done after adding to future list. Remove from list for message [" + message.getId() + "].");
                }
       			_futures.remove(message.getId());
       		}
       	}
        
        _threadCount++;
        if (LOG.isDebugEnabled()) {
            LOG.debug("current 'waitForMessage' thread count: [" + _threadCount + "]");
        }
    }

    public synchronized void interrupt() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Shutdown MessageReaderThread: " + this._peerName);
            LOG.info("Try cancel running tasks. Number of registered tasks: " + _futures.size());
        }
        if (_futures != null && !_futures.isEmpty()) {
        	for (String key : _futures.keySet()) {
        	    Future<?> f = _futures.get( key );
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancel future [" + f.toString() + "] for message [" + key + "].");
                }
        		f.cancel(true);
        	}
        	_futures.clear();
        }
    	PooledThreadExecutor.purge();
    	System.gc();
        super.interrupt();
    }
}
