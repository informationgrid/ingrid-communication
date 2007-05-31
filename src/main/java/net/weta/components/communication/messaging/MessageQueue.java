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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A Queue for messages. Handles diffeent type of messages and calls their handler.
 * 
 * @version $Revision$
 */
public class MessageQueue implements IMessageQueue {
    private static Logger LOGGER = Logger.getLogger(MessageQueue.class);

    private Map _messages = new HashMap();

    private Map _ids = new HashMap();

    private MessageProcessorRegistry _messageProcessorRegistry = new MessageProcessorRegistry();

    public MessageQueue() {
        // nothing todo
    }

    private synchronized void addMessage(Message message) {
        Integer messageId = Integer.valueOf("" + message.getId());
        Object mutex = getSynchronizedMutex(messageId);
        synchronized (mutex) {
            _messages.put(messageId, message);
            mutex.notify();
        }
    }

    private Object getSynchronizedMutex(Integer messageId) {
        Object mutex = null;
        synchronized (_ids) {
            mutex = _ids.remove(messageId);
            if (mutex == null) {
                mutex = new Object();
                _ids.put(messageId, mutex);
            }
        }
        return mutex;
    }

    public int size() {
        return _messages.size();
    }

    public Message waitForMessage(int id, int timeout) {
        Message message = null;
        Integer messageId = Integer.valueOf("" + id);
        Object mutex = getSynchronizedMutex(messageId);
        synchronized (mutex) {
            message = (Message) _messages.remove(messageId);
            try {
                if (null == message) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("message [" + id + "] not found, wait for max. " + timeout + " sec.");
                    }
                    mutex.wait(timeout * 1000);

                    message = (Message) _messages.remove(messageId);
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
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
                PayloadMessage reply = new PayloadMessage(
                        new IllegalStateException("no handler for message installed"), message.getType());
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
}
