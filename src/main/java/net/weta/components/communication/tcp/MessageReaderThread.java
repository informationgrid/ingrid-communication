package net.weta.components.communication.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.server.TooManyRunningThreads;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.MessageSizeTooBigException;
import net.weta.components.communication.tcp.server.IMessageSender;
import net.weta.components.communication.util.PooledThreadExecutor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MessageReaderThread extends Thread {

    protected static final Logger LOG = Logger.getLogger(MessageReaderThread.class);

    protected final MessageQueue _messageQueue;

    protected final String _peerName;

    protected final IMessageSender _messageSender;

    protected int _threadCount = 0;

    protected final int _maxThreadCount;

    private final IInput _in;
    
    private ExecutorService _threadExecutor = null;
    
    private Map<Runnable, Future<?>> _futures = Collections.synchronizedMap(new HashMap<Runnable, Future<?>>());

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
        	_threadExecutor = Executors.newFixedThreadPool(_maxThreadCount);
            if (LOG.isInfoEnabled()) {
                LOG.info("start to read messages for peer: [" + _peerName + "]");
            }

            while (!isInterrupted()) {
                Message message = (Message) _in.readObject();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("read message [" + message.getId() + "] for client [" + _peerName + "]");
                }
                waitForAnswer(message);
            }

        } catch (SocketException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("connection shutdown by peer (SocketException, " + e.getMessage() + "): " + _peerName);
            }
            if (_messageSender != null) {
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
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn(e.getMessage() + " for peer: " + _peerName);
            }
            if (_messageSender != null) {
                // disconnect in case of client
                _messageSender.disconnect(_peerName);
                // connect in case of client and server 
                _messageSender.connect(_peerName);
            }
        } catch (OptionalDataException e) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("Optional Data Exception: " + _peerName, e);
                LOG.error("There is no more data in the buffered part of the stream: " + e.eof);
                LOG.error("The number of bytes of primitive data available to be read in the current buffer: " + e.length);
            }
            // TODO what is here todo? mailSender disconnect and connect?
        } catch (IOException e) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("error while consuming messages for peer: " + _peerName, e);
            }
        } 
    }

    private void waitForAnswer(final Message message) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Thread with name " + this.getName() + " is waiting for answer of message [" + message.getId()
                    + "] for client [" + _peerName + "]");
        }

        if (_threadCount >= _maxThreadCount) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("message not handled because, max thread count reached: " + _maxThreadCount);
            }
            throw new TooManyRunningThreads("No more threads available.");
        }
        Runnable runnable = new Runnable() {
            public void run() {
                Message answer = _messageQueue.messageEvent(message);
                if (answer != null) {
                    answer.setType("");
                    try {
                        _messageSender.sendMessage(_peerName, answer);
                    } catch (IOException e) {
                        if (LOG.isEnabledFor(Level.WARN)) {
                            LOG.warn("can not send answer message to [" + _peerName + "]: " + e.getMessage());
                        }
                        if (LOG.isEnabledFor(Level.DEBUG)) {
                            LOG.debug("Stacktrace:", e);
                        }
                    }
                }
                _futures.remove(this);
                _threadCount--;
            }
        };

        // use thread pool here
        _futures.put(runnable, PooledThreadExecutor.commit(runnable));
        
        _threadCount++;
        if (LOG.isDebugEnabled()) {
            LOG.debug("current 'waitForMessage' thread count: [" + _threadCount + "]");
        }
    }

    public void interrupt() {
        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Shutdown MessageReaderThread.");
        }
        if (_futures != null && !_futures.isEmpty()) {
        	for (Future<?> f : _futures.values()) {
        		f.cancel(true);
        	}
        	_futures.clear();
        }
        super.interrupt();
    }
}
