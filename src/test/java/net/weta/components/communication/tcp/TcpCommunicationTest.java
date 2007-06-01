/*
 * Created on 16.05.2007
 */
package net.weta.components.communication.tcp;

import java.io.IOException;
import java.net.Socket;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;

public class TcpCommunicationTest extends TestCase {

    private static final String CLIENT = "/kug-group:client";

    private static final String SERVER = "/kug-group:server";

    private TcpCommunication _tcpCommunicationClient;

    private TcpCommunication _tcpCommunicationServer;

    protected void setUp() throws Exception {
        _tcpCommunicationServer = new TcpCommunication();
        _tcpCommunicationServer.setIsCommunicationServer(true);
        _tcpCommunicationServer.addServer("127.0.0.1:55556");
        _tcpCommunicationServer.setPeerName(SERVER);
        _tcpCommunicationServer.startup();
        _tcpCommunicationServer.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        boolean started = false;
        while (!started )
        try {
            new Socket("127.0.0.1", 55556);
            started = true;
        } catch (Exception e) {
            //
        }
        _tcpCommunicationClient = new TcpCommunication();
        _tcpCommunicationClient.setIsCommunicationServer(false);
        _tcpCommunicationClient.setPeerName(CLIENT);
        _tcpCommunicationClient.addServer("127.0.0.1:55556");
        _tcpCommunicationClient.startup();
        _tcpCommunicationClient.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        
        Thread.sleep(10000);
    }

    protected void tearDown() throws Exception {
        _tcpCommunicationClient.closeConnection(null);
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.closeConnection(CLIENT);
        _tcpCommunicationServer.shutdown();
    }

    public void testSendSyncMessageFromClientToServer() {
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

    public void testSendSyncMessageFromServerToClient() throws Exception {
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
