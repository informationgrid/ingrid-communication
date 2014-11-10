/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.util.Set;

import net.weta.components.communication.util.One2ManyHashMap;

/**
 * Registry for the implemented message processors.
 * 
 * @version $Revision$
 * 
 */
public class MessageProcessorRegistry {
    protected One2ManyHashMap fHandlersByMessageType = new One2ManyHashMap();

    /**
     * Adds a message handler for a given message type. All arriving messages with the given message types will be
     * delivered to the given handler.<p/>
     * 
     * @param messageType
     * @param handler
     * @throws IllegalArgumentException
     *             if message handler is an {@link IResponsefullMessageHandler} but there is already an responsefull
     *             message handler for this message type registered
     * @return if message handler not exists before
     */
    public synchronized boolean addMessageHandler(String messageType, IMessageHandler handler) {
        if (isHandlerRegistered(messageType)) {
            throw new IllegalArgumentException("just one responsefull message handler per message type allowed");
        }

        return this.fHandlersByMessageType.add(messageType, handler);
    }

    /**
     * Removes a registered message handler.
     * 
     * @param messageType
     * @param handler
     * @return true if message handler exists
     */
    public synchronized boolean removeMessageHandler(String messageType, IMessageHandler handler) {
        return this.fHandlersByMessageType.getValues(messageType).remove(handler);
    }

    /**
     * Removes all registered message handlers for the given message type.
     * 
     * @param messageType
     * @return all removed message handlers
     */
    public synchronized IMessageHandler[] removeMessageHandlers(String messageType) {
        return (IMessageHandler[]) this.fHandlersByMessageType.removeKey(messageType).toArray(
                new IMessageHandler[this.fHandlersByMessageType.size()]);
    }

    /**
     * Returns all message handlers for a given type.
     * 
     * @param messageType
     * @return Array of message handlers.
     */
    public synchronized IMessageHandler[] getHandlersForType(String messageType) {
        Set values = this.fHandlersByMessageType.getValues(messageType);
        if (values == null) {
            return new IMessageHandler[0];
        }
        return (IMessageHandler[]) values.toArray(new IMessageHandler[values.size()]);
    }

    /**
     * @return all message handlers
     */
    public synchronized IMessageHandler[] getMessageHandlers() {
        Set allValues = this.fHandlersByMessageType.valueSet();

        return (IMessageHandler[]) allValues.toArray(new IMessageHandler[allValues.size()]);
    }

    protected boolean isHandlerRegistered(String messageType) {
        boolean result = false;

        Set messageHandlers = this.fHandlersByMessageType.getValues(messageType);
        if (messageHandlers != null) {
            return true;
        }

        return result;
    }
}