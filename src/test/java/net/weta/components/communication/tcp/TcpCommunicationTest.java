/*
 * Created on 16.05.2007
 */
package net.weta.components.communication.tcp;

import java.io.IOException;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;

public class TcpCommunicationTest extends TestCase {

    private static final String CLIENT = "/kug-group:client";

    private static final String SERVER = "/kug-group:server";

    private Runnable _serverRunnable;

    private Runnable _clientRunnable;

    TcpCommunication _tcpCommunicationServer;

    TcpCommunication _tcpCommunicationClient;

    protected void setUp() throws Exception {

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                _tcpCommunicationServer.setIsCommunicationServer(true);
                _tcpCommunicationServer.addServer("127.0.0.1:10091");
                _tcpCommunicationServer.setPeerName(SERVER);
                try {
                    _tcpCommunicationServer.startup();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                _tcpCommunicationServer.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
            }
        };

        _clientRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationClient = new TcpCommunication();
                _tcpCommunicationClient.setIsCommunicationServer(false);
                _tcpCommunicationClient.setPeerName(CLIENT);
                _tcpCommunicationClient.addServer("127.0.0.1:10091");
                _tcpCommunicationClient.addServerName(SERVER);
                try {
                    _tcpCommunicationClient.startup();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                _tcpCommunicationClient.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
            }
        };

        Thread thread = new Thread(_serverRunnable);
        thread.start();
        thread.join();

        Thread thread2 = new Thread(_clientRunnable);
        thread2.start();
        thread2.join();

    }

    protected void tearDown() throws Exception {
        _tcpCommunicationClient.closeConnection(null);
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.closeConnection(CLIENT);
        _tcpCommunicationServer.shutdown();
    }

    public void testSendMessage() throws Exception {
        Thread.sleep(10000);
        sendSyncMessageFromClientToServer();
        sendSyncMessageFromServerToClient();
    }

    public void sendSyncMessageFromClientToServer() throws Exception {
        Message message = new Message("type");
        Message result = null;
        try {
            result = _tcpCommunicationClient.sendSyncMessage(message, SERVER);
        } catch (IOException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
        assertNotNull(result);
        assertEquals(message.getId(), result.getId());
        assertEquals("", result.getType());
    }

    public void sendSyncMessageFromServerToClient() throws Exception {
        Message message = new Message("type");
        Message result = null;
        try {
            result = _tcpCommunicationServer.sendSyncMessage(message, CLIENT);
        } catch (IOException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());
    }
}
