/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source$
 */

package net.weta.components.communication.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.weta.components.communication.CommunicationException;

import org.apache.log4j.Logger;

/**
 * A Queue for messages. Handles diffeent type of messages and calls their
 * handler.
 * 
 * @version $Revision$
 */
public class MessageQueue implements IMessageQueue {

    private static Logger LOGGER = Logger.getLogger(MessageQueue.class);

    private List<String> _queueSize = Collections.synchronizedList(new ArrayList<String>());
    
    private Map<String, Message> _messages = new ConcurrentHashMap<String, Message>();

    private Map<String, MutexType> _ids = new ConcurrentHashMap<String, MutexType>();

    private MessageProcessorRegistry _messageProcessorRegistry = new MessageProcessorRegistry();

    private int _maxSize = 2000;
    
    private class MutexType {
    	public static final byte MUTEX_MESSAGE_PROCESSED = 1;
    	
    	private byte state = 0;
    	
    	public void setState(byte state) {
    		this.state = state;
    	}
    	
    	public byte getState() {
    		return this.state;
    	}
    }
    
    

    public MessageQueue() {
        // nothing todo
    }

    private void addMessage(Message message) {
        String messageId = message.getId();
        MutexType mutex = getSynchronizedMutex(messageId);
        synchronized (mutex) {
        	// check for message that have been processed already (timeout in waitForMessage())
        	if (mutex.getState() == MutexType.MUTEX_MESSAGE_PROCESSED) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Message [" + messageId + "] already processed. Ignore this message.");
                }
                // remove timeout message from mutex store
                _ids.remove(messageId);
            } else {
            	if (_queueSize.size() == _maxSize) {
	                if (LOGGER.isInfoEnabled()) {
	                    LOGGER.info("Max size of message queue reached: " + _maxSize + " with message [" + messageId + "].");
	                }
	                String oldestMessageId = (String) _queueSize.remove(0);
	                _messages.remove(oldestMessageId);
	            	_ids.remove(oldestMessageId);
            	}
                _messages.put(messageId, message);
                _queueSize.add(messageId);
                mutex.notify();
            }
        }
    }

    private MutexType getSynchronizedMutex(String messageId) {
    	MutexType mutex = null;
        mutex = _ids.remove(messageId);
        if (mutex == null) {
            mutex = new MutexType();
            _ids.put(messageId, mutex);
            if (LOGGER.isInfoEnabled()) {
            	if (_ids.size() > 0 && _ids.size() % 100 == 0) {
            		LOGGER.info("Size of synchronized mutex list: " + _ids.size() + ". Last message is: " + messageId);
            	}
            }
        }
        return mutex;
    }

    public int size() {
        return _messages.size();
    }

    public Message waitForMessage(String id, int timeout) {
        Message message = null;
        MutexType mutex = getSynchronizedMutex(id);
        synchronized (mutex) {
            message = (Message) _messages.remove(id);
            try {
                if (null == message) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("message [" + id + "] not found, wait for max. " + timeout + " sec.");
                    }
                    mutex.wait(timeout * 1000);

                    message = (Message) _messages.remove(id);
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            finally {
            	/* set message as state processed
            	   messages that will be received after this point will be 
            	   removed from mutex storage. See addMessage() as well.
            	*/
            	mutex.setState(MutexType.MUTEX_MESSAGE_PROCESSED);
            	// remove message from queue
            	_queueSize.remove(id);
            }

            if (LOGGER.isDebugEnabled() && message != null) {
                LOGGER.debug("found message [" + id + "]");
            } else if (LOGGER.isDebugEnabled() && message == null) {
                LOGGER.debug("timeout by reading message [" + id + "]");
            }
        }
        return message;
    }

    /**
     * Removes all stored messages.
     */
    public synchronized void clear() {
        _messages.clear();
        _ids.clear();
        _queueSize.clear();
    }

    public MessageProcessorRegistry getProcessorRegistry() {
        return _messageProcessorRegistry;
    }

    public Message messageEvent(Message message) {
        IMessageHandler[] handlersForType = _messageProcessorRegistry.getHandlersForType(message.getType());
        Message replyMessage = null;

        if (handlersForType.length == 0) {
            if (message.getType().equals("")) {
                addMessage(message);
            } else {
                LOGGER.error("no handler for message installed '" + message.getId() + "' - " + message.getType());
                PayloadMessage reply = new PayloadMessage(new CommunicationException(
                        "no handler for message installed"), message.getType());
                return reply;
            }
        } else {
            try {
                for (int i = 0; i < handlersForType.length; i++) {
                    replyMessage = handlersForType[i].handleMessage(message);
                }
            } catch (Throwable t) {
                LOGGER.error("throwable in handler on message '" + message + "'", t);
            }
        }
        return replyMessage;
    }

    public void addMessageHandler(String messageType, IMessageHandler messageHandler) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("add message handler to registry: " + messageType + " / "
                    + messageHandler.getClass().getName());
        }
        _messageProcessorRegistry.addMessageHandler(messageType, messageHandler);
    }

    /**
     * @return the maxSize
     */
    public int getMaxSize() {
        return _maxSize;
    }

    /**
     * @param maxSize the maxSize to set
     */
    public void setMaxSize(int maxSize) {
        _maxSize = maxSize;
    }
}
