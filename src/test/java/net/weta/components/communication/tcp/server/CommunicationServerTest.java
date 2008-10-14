package net.weta.components.communication.tcp.server;

import java.io.File;
import java.net.SocketException;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.tcp.TcpCommunication;
import sun.security.tools.KeyTool;

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
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setName(SERVER);
        serverConfiguration.setPort(55556);
        server.setConfiguration(serverConfiguration);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        
        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55556);
        clientConnection.setServerName(SERVER);
        clientConfiguration.addClientConnection(clientConnection);
        client1.setConfiguration(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());



        Thread.sleep(1000);
        Message message = client1.sendSyncMessage(new Message("type"), "server");
        assertNotNull(message);

        TcpCommunication client2 = new TcpCommunication();
        ClientConfiguration clientConfiguration2 = new ClientConfiguration();
        clientConfiguration2.setName(CLIENT2);
        ClientConnection clientConnection2 = clientConfiguration2.new ClientConnection();
        clientConnection2.setServerIp("127.0.0.1");
        clientConnection2.setServerPort(55556);
        clientConnection2.setServerName(SERVER);
        clientConfiguration2.addClientConnection(clientConnection2);
        client2.setConfiguration(clientConfiguration2);
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
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setName(SERVER);
        serverConfiguration.setPort(55557);
        serverConfiguration.setKeystorePath(_keystoreServer.getAbsolutePath());
        serverConfiguration.setKeystorePassword("password");

        server.setConfiguration(serverConfiguration);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        

        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55557);
        clientConnection.setServerName(SERVER);
        clientConnection.setKeystorePath(_keystoreClient.getAbsolutePath());
        clientConnection.setKeystorePassword("password");
        clientConfiguration.addClientConnection(clientConnection);
        client1.setConfiguration(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        Message message = client1.sendSyncMessage(new Message("type"), "server");
        assertNotNull(message);

        TcpCommunication client2 = new TcpCommunication();

        ClientConfiguration clientConfiguration2 = new ClientConfiguration();
        clientConfiguration2.setName(CLIENT2);
        ClientConnection clientConnection2 = clientConfiguration2.new ClientConnection();
        clientConnection2.setServerIp("127.0.0.1");
        clientConnection2.setServerPort(55557);
        clientConnection2.setServerName(SERVER);
        clientConnection2.setKeystorePath(_keystoreClient2.getAbsolutePath());
        clientConnection2.setKeystorePassword("password");
        clientConfiguration2.addClientConnection(clientConnection2);
        client2.setConfiguration(clientConfiguration2);
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
