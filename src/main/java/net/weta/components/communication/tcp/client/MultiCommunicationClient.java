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
}
