/*
 * Created on 16.05.2007
 */
package net.weta.components.communication.tcp;

import java.io.File;
import java.io.IOException;

import sun.security.tools.KeyTool;

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

    private File _securityFolder;

    protected void setUp() throws Exception {

        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        final File keystoreServer = new File(_securityFolder, "keystore-server");
        final File keystoreClient = new File(_securityFolder, "keystore-client");
        File clientCertificate = new File(_securityFolder, "client.cer");

        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreServer.getAbsolutePath(), "-alias", SERVER,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreClient.getAbsolutePath(), "-alias", CLIENT,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-export", "-keystore", keystoreClient.getAbsolutePath(), "-storepass", "password",
                "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-import", "-keystore", keystoreServer.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                _tcpCommunicationServer.setIsCommunicationServer(true);
                _tcpCommunicationServer.addServer("127.0.0.1:10091");
                _tcpCommunicationServer.setPeerName(SERVER);
                _tcpCommunicationServer.setKeystore(keystoreServer.getAbsolutePath());
                _tcpCommunicationServer.setKeystorePassword("password");
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
                _tcpCommunicationClient.setKeystore(keystoreClient.getAbsolutePath());
                _tcpCommunicationClient.setKeystorePassword("password");

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

        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());
    }

    public void testSendMessage() throws Exception {
        Thread.sleep(3000);
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
