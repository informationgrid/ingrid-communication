package net.weta.components.communication.tcp.client;

import java.io.IOException;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.tcp.server.IMessageSender;

public class MultiCommunicationClient extends Thread implements IMessageSender {

    private final CommunicationClient[] _clients;

    public MultiCommunicationClient(CommunicationClient[] clients) {
        assert clients.length > 0;
        _clients = clients;
    }

    public void run() {
        connect(null);
    }

    public void sendMessage(String peerName, Message message) throws IOException {
        // FIXME currently only the first client is used to send messages
        _clients[0].sendMessage(peerName, message);
    }

    public void interrupt() {
        for (int i = 0; i < _clients.length; i++) {
            CommunicationClient client = _clients[i];
            client.interrupt();
        }
    }

    public void connect(String url) {
        for (int i = 0; i < _clients.length; i++) {
            CommunicationClient client = _clients[i];
            client.connect(url);
        }
    }
}
