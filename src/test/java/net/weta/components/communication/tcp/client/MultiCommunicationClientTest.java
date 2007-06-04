/*
 * Created on 04.06.2007
 */
package net.weta.components.communication.tcp.client;

import java.io.IOException;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.tcp.TcpCommunication;
import junit.framework.TestCase;

public class MultiCommunicationClientTest extends TestCase {

    private static final String CLIENT = "/kug-group:client";

    private static final String SERVER = "/kug-group:server";
    
    private static final String SERVER2 = "/kug-group:server2";

    private Runnable _serverRunnable;

    private Runnable _clientRunnable;

    TcpCommunication _tcpCommunicationServer;

    TcpCommunication _tcpCommunicationClient;

    private Runnable _serverRunnable2;
    
    TcpCommunication _tcpCommunicationServer2;

    protected void setUp() throws Exception {

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                _tcpCommunicationServer.setIsCommunicationServer(true);
                _tcpCommunicationServer.addServer("127.0.0.1:9191");
                _tcpCommunicationServer.setPeerName(SERVER);
                try {
                    _tcpCommunicationServer.startup();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                _tcpCommunicationServer.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
            }
        };
        
        _serverRunnable2 = new Runnable() {
            public void run() {
                _tcpCommunicationServer2 = new TcpCommunication();
                _tcpCommunicationServer2.setIsCommunicationServer(true);
                _tcpCommunicationServer2.addServer("127.0.0.1:9192");
                _tcpCommunicationServer2.setPeerName(SERVER);
                try {
                    _tcpCommunicationServer2.startup();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                _tcpCommunicationServer2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
            }
        };

        _clientRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationClient = new TcpCommunication();
                _tcpCommunicationClient.setIsCommunicationServer(false);
                _tcpCommunicationClient.setPeerName(CLIENT);
                _tcpCommunicationClient.addServer("127.0.0.1:9191");
                _tcpCommunicationClient.addServer("127.0.0.1:9192");
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
        
        Thread thread2 = new Thread(_serverRunnable2);
        thread2.start();
        thread2.join();

        Thread thread3 = new Thread(_clientRunnable);
        thread3.start();
        thread3.join();
    }
    
    protected void tearDown() throws Exception {
        _tcpCommunicationClient.closeConnection(null);
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.closeConnection(CLIENT);
        _tcpCommunicationServer.shutdown();
        _tcpCommunicationServer2.closeConnection(CLIENT);
        _tcpCommunicationServer2.shutdown();
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
        
        message = new Message("type");
        result = null;
        try {
            result = _tcpCommunicationServer2.sendSyncMessage(message, CLIENT);
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
