package net.weta.components.communication.tcp.server;

import java.io.File;
import java.net.SocketException;

import sun.security.tools.KeyTool;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.tcp.TcpCommunication;

public class CommunicationServerTest extends TestCase {

    private File _securityFolder;

    private File _keystoreServer;

    private File _keystoreClient;

    private File _keystoreClient2;

    private static final String CLIENT = "/kug-group:client";

    private static final String CLIENT2 = "/kug-group:client2";

    private static final String SERVER = "/kug-group:server";

    protected void setUp() throws Exception {
        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        _keystoreServer = new File(_securityFolder, "keystore-server");
        _keystoreClient = new File(_securityFolder, "keystore-client");
        _keystoreClient2 = new File(_securityFolder, "keystore-client");
        File clientCertificate = new File(_securityFolder, "client.cer");
        File clientCertificate2 = new File(_securityFolder, "client2.cer");

        KeyTool.main(new String[] { "-genkey", "-keystore", _keystoreServer.getAbsolutePath(), "-alias", SERVER,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", _keystoreClient.getAbsolutePath(), "-alias", CLIENT,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", _keystoreClient2.getAbsolutePath(), "-alias", CLIENT2,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });

        KeyTool.main(new String[] { "-export", "-keystore", _keystoreClient.getAbsolutePath(), "-storepass",
                "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-export", "-keystore", _keystoreClient2.getAbsolutePath(), "-storepass",
                "password", "-alias", CLIENT2, "-file", clientCertificate2.getAbsolutePath() });

        KeyTool.main(new String[] { "-import", "-keystore", _keystoreServer.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-import", "-keystore", _keystoreServer.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT2, "-file", clientCertificate2.getAbsolutePath() });
    }

    protected void tearDown() throws Exception {

        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());

    }

    public void testRegsiterWithoutSecurity() throws Exception {
        TcpCommunication server = new TcpCommunication();
        server.setIsCommunicationServer(true);
        server.addServer("127.0.0.1:55556");
        server.setPeerName(SERVER);
        server.setKeystore(_keystoreServer.getAbsolutePath());
        server.setKeystorePassword("password");
        server.setIsSecure(false);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        client1.setIsCommunicationServer(false);
        client1.setPeerName(CLIENT);
        client1.addServer("127.0.0.1:55556");
        client1.addServerName(SERVER);
        client1.setKeystore(_keystoreClient.getAbsolutePath());
        client1.setKeystorePassword("password");
        client1.setIsSecure(false);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        Message message = client1.sendSyncMessage(new Message("type"), "server");
        assertNotNull(message);

        TcpCommunication client2 = new TcpCommunication();
        client2.setIsCommunicationServer(false);
        client2.setPeerName(CLIENT2);
        client2.addServer("127.0.0.1:55556");
        client2.addServerName(SERVER);
        client2.setKeystore(_keystoreClient2.getAbsolutePath());
        client2.setKeystorePassword("password");
        client2.setIsSecure(false);
        client2.startup();
        client2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        try {
            message = client2.sendSyncMessage(new Message("type"), "server");
        } catch (SocketException e) {
            fail();
        }
    }

    public void testWithSecurity() throws Exception {
        TcpCommunication server = new TcpCommunication();
        server.setIsCommunicationServer(true);
        server.addServer("127.0.0.1:55557");
        server.setPeerName(SERVER);
        server.setKeystore(_keystoreServer.getAbsolutePath());
        server.setKeystorePassword("password");
        server.setIsSecure(true);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        client1.setIsCommunicationServer(false);
        client1.setPeerName(CLIENT);
        client1.addServer("127.0.0.1:55557");
        client1.addServerName(SERVER);
        client1.setKeystore(_keystoreClient.getAbsolutePath());
        client1.setKeystorePassword("password");
        client1.setIsSecure(true);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        Message message = client1.sendSyncMessage(new Message("type"), "server");
        assertNotNull(message);

        TcpCommunication client2 = new TcpCommunication();
        client2.setIsCommunicationServer(false);
        client2.setPeerName(CLIENT2);
        client2.addServer("127.0.0.1:55557");
        client2.addServerName(SERVER);
        client2.setKeystore(_keystoreClient2.getAbsolutePath());
        client2.setKeystorePassword("password");
        client2.setIsSecure(true);
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
