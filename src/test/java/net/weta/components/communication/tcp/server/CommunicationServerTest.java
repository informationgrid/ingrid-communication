package net.weta.components.communication.tcp.server;

import java.net.SocketException;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.tcp.TcpCommunication;

public class CommunicationServerTest extends TestCase {

    public void testRegsiter() throws Exception {
        TcpCommunication server = new TcpCommunication();
        server.setIsCommunicationServer(true);
        server.addServer("127.0.0.1:55556");
        server.setPeerName("server");
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        client1.setIsCommunicationServer(false);
        client1.setPeerName("client");
        client1.addServer("127.0.0.1:55556");
        client1.addServerName("server");
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        Message message = client1.sendSyncMessage(new Message("type"), "server");
        assertNotNull(message);

        TcpCommunication client2 = new TcpCommunication();
        client2.setIsCommunicationServer(false);
        client2.setPeerName("client");
        client2.addServer("127.0.0.1:55556");
        client2.addServerName("server");
        client2.startup();
        client2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        try {
            message = client2.sendSyncMessage(new Message("type"), "server");
        } catch (SocketException e) {
            fail();
        }
    }
}
