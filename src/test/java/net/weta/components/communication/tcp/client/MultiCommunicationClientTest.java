/*
 * Created on 04.06.2007
 */
package net.weta.components.communication.tcp.client;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.tcp.TcpCommunication;
import sun.security.tools.KeyTool;

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

    private File _securityFolder;

    protected void setUp() throws Exception {

        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        final File keystoreServer = new File(_securityFolder, "keystore-server");
        final File keystoreServer2 = new File(_securityFolder, "keystore-server2");
        final File keystoreClient = new File(_securityFolder, "keystore-client");
        File clientCertificate = new File(_securityFolder, "client.cer");

        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreServer.getAbsolutePath(), "-alias", SERVER,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreServer2.getAbsolutePath(), "-alias", SERVER2,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreClient.getAbsolutePath(), "-alias", CLIENT,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-export", "-keystore", keystoreClient.getAbsolutePath(), "-storepass", "password",
                "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-import", "-keystore", keystoreServer.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-import", "-keystore", keystoreServer2.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                _tcpCommunicationServer.setIsCommunicationServer(true);
                _tcpCommunicationServer.addServer("127.0.0.1:9193");
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

        _serverRunnable2 = new Runnable() {
            public void run() {
                _tcpCommunicationServer2 = new TcpCommunication();
                _tcpCommunicationServer2.setIsCommunicationServer(true);
                _tcpCommunicationServer2.addServer("127.0.0.1:9194");
                _tcpCommunicationServer2.setPeerName(SERVER2);
                _tcpCommunicationServer2.setKeystore(keystoreServer2.getAbsolutePath());
                _tcpCommunicationServer2.setKeystorePassword("password");

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
                _tcpCommunicationClient.addServer("127.0.0.1:9193");
                _tcpCommunicationClient.addServerName(SERVER);
                _tcpCommunicationClient.addServer("127.0.0.1:9194");
                _tcpCommunicationClient.addServerName(SERVER2);
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

        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());
    }

    public void testSendSyncMessageFromServerToClient() throws Exception {
        Thread.sleep(3000);
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
