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

/**
 * Interface for an exchangeable MessageQueue. Created on 28.08.2005.
 * 
 * @version $Revision$
 */
public interface IMessageQueue {

    /**
     * Returns the size of the message queue.
     * 
     * @return The size of the message queue.
     */
    public int size();

    /**
     * Waits a given time intervall for the next message if the message queue is empty.
     * 
     * @param timeout
     *            Time in seconds to wait.
     * @return A serializable object.
     */
    public Message waitForMessage(int id, int timeout);

    /**
     * Returns the message prozessor registry for this queue.
     * 
     * @return A MessageProcessorRegistry object.
     */
    public MessageProcessorRegistry getProcessorRegistry();

    /**
     * @param message
     * @return the reply or null
     */
    public Message messageEvent(Message message);

    void addMessageHandler(String messageType, IMessageHandler messageHandler);
}