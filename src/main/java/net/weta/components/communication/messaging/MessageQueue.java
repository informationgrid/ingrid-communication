/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.weta.components.communication.CommunicationException;

import org.apache.log4j.Logger;

/**
 * A Queue for messages. Handles diffeent type of messages and calls their
 * handler.
 * 
 * @version $Revision$
 */
public class MessageQueue implements IMessageQueue {

    private static Logger LOGGER = Logger.getLogger( MessageQueue.class );

    private List<String> _queueSize = new CopyOnWriteArrayList<String>();

    private Map<String, Message> _messages = new ConcurrentHashMap<String, Message>();

    private Map<String, MutexType> _ids = new ConcurrentHashMap<String, MutexType>();

    private MessageProcessorRegistry _messageProcessorRegistry = new MessageProcessorRegistry();

    private int _maxSize = 2000;

    private long _maxMutexTimeout = 600 * 1000; // one hour in ms

    private long lastMutexTimeout = 0; // one hour in ms

    private int _maxMutexListeSizeBeforeGarbageCollect = 500;

    private class MutexType {
        public static final byte MUTEX_MESSAGE_PROCESSED = 1;

        private byte state = 0;

        private long created = System.currentTimeMillis();

        public void setState(byte state) {
            this.state = state;
        }

        public byte getState() {
            return this.state;
        }

        public long getCreated() {
            return created;
        }
    }

    public MessageQueue() {
        // nothing todo
    }

    private void addMessage(Message message) {
        String messageId = message.getId();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug( "Add Message [" + messageId + "]." );
        }
        MutexType mutex = getSynchronizedMutex( messageId );
        synchronized (mutex) {
            // check for message that have been processed already (timeout in
            // waitForMessage())
            if (mutex.getState() == MutexType.MUTEX_MESSAGE_PROCESSED) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug( "Message [" + messageId + "] already processed. Ignore this message." );
                }
                // remove timeout message from mutex store
                _ids.remove( messageId );
            } else {
                if (_queueSize.size() == _maxSize) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info( "Max size of message queue reached: " + _maxSize + " with message [" + messageId + "]." );
                    }
                    String oldestMessageId = (String) _queueSize.remove( 0 );
                    _messages.remove( oldestMessageId );
                    _ids.remove( oldestMessageId );
                }
                _messages.put( messageId, message );
                _queueSize.add( messageId );

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug( "Notify mutex: [" + mutex + "]." );
                }
                mutex.notify();
            }
        }
    }

    private synchronized MutexType getSynchronizedMutex(String messageId) {
        MutexType mutex = null;
        mutex = _ids.remove( messageId );
        if (mutex == null) {
            mutex = new MutexType();
            _ids.put( messageId, mutex );
            if (_ids.size() > _maxMutexListeSizeBeforeGarbageCollect) {
                long now = System.currentTimeMillis();
                long then = now - _maxMutexTimeout;
                if (lastMutexTimeout < then) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info( "Start mutex list garbage collection. Removing mutex messages older than " + (_maxMutexTimeout / 1000) + " sec." );
                    }
                    for (String mutexId : _ids.keySet()) {
                        MutexType tmpMutex = _ids.get( mutexId );
                        if (tmpMutex != null && tmpMutex.getCreated() < then) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info( "Remove old (" + ((now - tmpMutex.getCreated()) / 1000.0) + " sec) mutex message [" + mutexId + "] from message queue." );
                            }
                            _ids.remove( mutexId );
                            _messages.remove( mutexId );
                            _queueSize.remove( mutexId );
                            tmpMutex = null;
                        }
                    }
                    lastMutexTimeout = then;
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug( "Get mutex: [" + mutex + "] in thread [" + Thread.currentThread().getName() + "] for message id [" + messageId + "]." );
        }

        return mutex;
    }

    public int size() {
        return _messages.size();
    }

    public Message waitForMessage(String id, int timeout) {
        Message message = null;
        MutexType mutex = getSynchronizedMutex( id );
        synchronized (mutex) {
            message = (Message) _messages.remove( id );
            try {
                if (null == message) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug( "message [" + id + "] not found, wait thread [" + Thread.currentThread().getName() + "] for max. " + timeout + " sec on mutex [" + mutex
                                + "]." );
                    }
                    long msTimeout = System.currentTimeMillis() + timeout * 1000;
                    // wait for message, ignore interrupts
                    while (message == null && System.currentTimeMillis() < msTimeout) {
                        try {
                            mutex.wait( 1000 );
                            message = (Message) _messages.remove( id );
                            if (LOGGER.isDebugEnabled()) {
                                if (message == null && System.currentTimeMillis() < msTimeout) {
                                    LOGGER.debug( "Waiting for message in thread [" + Thread.currentThread().getName() + "] finished  after " + (System.currentTimeMillis() - (msTimeout - timeout * 1000) ) + " ms. Message not found yet. Wait another 1000 ms for message to arrive." );
                                }
                            }
                            
                        } catch (InterruptedException e) {
                            LOGGER.warn( "Message [" + id + "]: thread [" + Thread.currentThread().getName() + "] waiting was interrupted on mutex [" + mutex + "].", e );
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug( "Thread [" + Thread.currentThread().getName() + "] was interrupted after " + (System.currentTimeMillis() - (msTimeout - timeout * 1000) ) + " ms.");
                            }
                        }
                    }

                }
            } finally {
                /*
                 * set message as state processed messages that will be received
                 * after this point will be removed from mutex storage. See
                 * addMessage() as well.
                 */
                mutex.setState( MutexType.MUTEX_MESSAGE_PROCESSED );
                // remove message from queue
                _queueSize.remove( id );
            }

            if (LOGGER.isDebugEnabled()) {
                if (message != null) {
                    LOGGER.debug( "Found message [" + id + "]" );
                } else {
                    LOGGER.debug( "Timeout [" + timeout * 1000 + " ms] reading message [" + id + "]" );
                }
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
        IMessageHandler[] handlersForType = _messageProcessorRegistry.getHandlersForType( message.getType() );
        Message replyMessage = null;

        if (handlersForType.length == 0) {
            if (message.getType().equals( "" )) {
                addMessage( message );
            } else {
                LOGGER.error( "no handler for message installed '" + message.getId() + "' - " + message.getType() );
                PayloadMessage reply = new PayloadMessage( new CommunicationException( "no handler for message installed" ), message.getType() );
                return reply;
            }
        } else {
            try {
                for (int i = 0; i < handlersForType.length; i++) {
                    replyMessage = handlersForType[i].handleMessage( message );
                }
            } catch (Throwable t) {
                LOGGER.error( "throwable in handler on message '" + message + "'", t );
            }
        }
        return replyMessage;
    }

    public void addMessageHandler(String messageType, IMessageHandler messageHandler) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug( "add message handler to registry: " + messageType + " / " + messageHandler.getClass().getName() );
        }
        _messageProcessorRegistry.addMessageHandler( messageType, messageHandler );
    }

    /**
     * @return the maxSize
     */
    public int getMaxSize() {
        return _maxSize;
    }

    /**
     * @param maxSize
     *            the maxSize to set
     */
    public void setMaxSize(int maxSize) {
        _maxSize = maxSize;
    }

    /**
     * @param maxMutexTimeout
     */
    public void setMaxMutexTimeout(int maxMutexTimeout) {
        _maxMutexTimeout = maxMutexTimeout;
    }

    /**
     * @param maxMutexListeSizeBeforeGarbageCollect
     */
    public void setMaxMutexListeSizeBeforeGarbageCollect(int maxMutexListeSizeBeforeGarbageCollect) {
        _maxMutexListeSizeBeforeGarbageCollect = maxMutexListeSizeBeforeGarbageCollect;
    }

}
