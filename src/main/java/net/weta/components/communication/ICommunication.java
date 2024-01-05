/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

package net.weta.components.communication;

import java.io.IOException;

import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;

/**
 * The main access point of a communication component. It concludes basic group management and different communication
 * strategies.
 * 
 * <br/><br/>created on 14.10.2005
 * 
 * @version $Revision$
 * 
 */
public interface ICommunication {

    /**
     * Returns the MessageQueue with all its messages.
     * 
     * @return message queue
     */
    public IMessageQueue getMessageQueue();

    /**
     * Joins a peer group and listens for messages to a given peer group.
     * 
     * @param url
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void subscribeGroup(String url) throws IOException;

    /**
     * Leaves the given group.
     * 
     * @param url
     * @throws IOException
     */
    public void unsubscribeGroup(String url) throws IOException;

    /**
     * Tests if communication is subscribed in the given group.
     * 
     * @param url
     * @return True if the group exist else false.
     * @throws IllegalArgumentException
     */
    public boolean isSubscribed(String url) throws IOException;

    /**
     * Sends a message (any serializable object) to a peer or a whole peer group. It isn't sure when the message
     * arrives. It isn't guaranteed that the message arrives. The maximum size of message send with this method is 64Kb
     * (depends on your OS).
     * 
     * @param message
     * @param url
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void sendMessage(Message message, String url) throws IOException;

    /**
     * @param url
     * @throws IOException
     */
    public void closeConnection(String url) throws IOException;

    /**
     * @param message
     * @param url
     * @return the answer
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    public Message sendSyncMessage(Message message, String url) throws IOException;

    /**
     * @return the name of the peername
     */
    public String getPeerName();

    /**
     * @param peerName
     */
    public void setPeerName(String peerName);

    /**
     * @throws IOException
     * 
     */
    public void startup() throws IOException;

    /**
     * 
     */
    public void shutdown();

    boolean isConnected(String url);

}
