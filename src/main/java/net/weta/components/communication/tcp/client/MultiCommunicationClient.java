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
package net.weta.components.communication.tcp.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.tcp.server.IMessageSender;

import org.apache.log4j.Logger;

public class MultiCommunicationClient extends Thread implements IMessageSender, ICommunicationClient {

    private static final Logger LOG = Logger.getLogger(MultiCommunicationClient.class);

    private final HashMap _clients = new HashMap();

    public MultiCommunicationClient(CommunicationClient[] clients) {
        assert clients.length > 0;
        for (int i = 0; i < clients.length; i++) {
            _clients.put(clients[i].getServerName(), clients[i]);
        }
    }

    public void run() {
        connect(null);
    }

    public void sendMessage(String peerName, Message message) throws IOException {
        CommunicationClient client = (CommunicationClient) _clients.get(peerName);
        if (client != null) {
            client.sendMessage(peerName, message);
        } else {
            LOG.error("No client for server (" + peerName + ") initialized.");
        }
    }

    public void interrupt() {
        for (Iterator iterator = _clients.values().iterator(); iterator.hasNext();) {
            CommunicationClient client = (CommunicationClient) iterator.next();
            client.interrupt();
        }
    }

    public void connect(String url) {
        for (Iterator iterator = _clients.values().iterator(); iterator.hasNext();) {
            CommunicationClient client = (CommunicationClient) iterator.next();
            client.connect(url);
        }
    }

    public void disconnect(String url) {
        for (Iterator iterator = _clients.values().iterator(); iterator.hasNext();) {
            CommunicationClient client = (CommunicationClient) iterator.next();
            client.disconnect(url);
        }
    }

    public void shutdown() {
        for (Iterator iterator = _clients.values().iterator(); iterator.hasNext();) {
            CommunicationClient client = (CommunicationClient) iterator.next();
            client.shutdown();
        }
    }

    public boolean isConnected(String serverName) {
        Object object = _clients.get(serverName);
        if ((null != object) && (object instanceof CommunicationClient)) {
            CommunicationClient client = (CommunicationClient) object;
            return client.isConnected(serverName);
        }
        return false;
    }
}
