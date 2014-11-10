/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
        // unable to delete files under windows, because they are locked
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
	        for (int i = 0; i < files.length; i++) {
	            assertTrue(files[i].delete());
	        }
	        assertTrue(_securityFolder.delete());
        }

    }
    
    public void testDuplicateRegistrationSameIP() throws Exception {
        TcpCommunication server = new TcpCommunication();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setName(SERVER);
        serverConfiguration.setPort(55558);
        server.configure(serverConfiguration);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        
        Thread.sleep(1000);

        TcpCommunication client1 = new TcpCommunication();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55558);
        clientConnection.setServerName(SERVER);
        clientConfiguration.addClientConnection(clientConnection);
        client1.configure(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());


        Thread.sleep(1000);
        Message message = new Message("type");
        Message result = client1.sendSyncMessage(message, SERVER);
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());
        
        System.out.println("Startup second client with same IP.");
        TcpCommunication client2 = new TcpCommunication();
        ClientConfiguration clientConfiguration2 = new ClientConfiguration();
        clientConfiguration2.setName(CLIENT);
        ClientConnection clientConnection2 = clientConfiguration2.new ClientConnection();
        clientConnection2.setServerIp("127.0.0.1");
        clientConnection2.setServerPort(55558);
        clientConnection2.setServerName(SERVER);
        clientConfiguration2.addClientConnection(clientConnection2);
        client2.configure(clientConfiguration2);
        client2.startup();
        client2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        System.out.println("Send Message from second client.");
    	message = new Message("type");
    	result = client2.sendSyncMessage(message, SERVER);
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals("", result.getId());
        
        System.out.println("Shutdown first client.");
        client1.shutdown();
        Thread.sleep(1000);

        System.out.println("Send message from second client.");
    	message = new Message("type");
    	client2.startup();
    	Thread.sleep(1000);
    	result = client2.sendSyncMessage(message, SERVER);
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());
        
    }
    
    public void testTimeoutClientInfo() throws Exception {
        TcpCommunication server = new TcpCommunication();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setName(SERVER);
        serverConfiguration.setPort(55568);
        serverConfiguration.setMaxClientInfoLifetime(1000);
        server.configure(serverConfiguration);
        server.startup();
        server.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        
        System.out.println("Wait 1000 ms.");
        Thread.sleep(1000);
        System.out.println("Continue...");

        TcpCommunication client1 = new TcpCommunication();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55568);
        clientConnection.setServerName(SERVER);
        clientConfiguration.addClientConnection(clientConnection);
        client1.configure(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        
        System.out.println("Wait 2000 ms.");
        Thread.sleep(2000);
        System.out.println("Continue...");
        
        client1.shutdown();
        server.shutdown();
    }    
    
    
    public void testRegsiterWithoutSecurity() throws Exception {
        TcpCommunication server = new TcpCommunication();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setName(SERVER);
        serverConfiguration.setPort(55556);
        server.configure(serverConfiguration);
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
        client1.configure(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());



        Thread.sleep(1000);
        Message message = new Message("type");
        Message result = client1.sendSyncMessage(message, SERVER);
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());
        

        TcpCommunication client2 = new TcpCommunication();
        ClientConfiguration clientConfiguration2 = new ClientConfiguration();
        clientConfiguration2.setName(CLIENT2);
        ClientConnection clientConnection2 = clientConfiguration2.new ClientConnection();
        clientConnection2.setServerIp("127.0.0.1");
        clientConnection2.setServerPort(55556);
        clientConnection2.setServerName(SERVER);
        clientConfiguration2.addClientConnection(clientConnection2);
        client2.configure(clientConfiguration2);
        client2.startup();
        client2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        try {
        	message = new Message("type");
        	result = client2.sendSyncMessage(message, SERVER);
            assertNotNull(result);
            assertEquals("", result.getType());
            assertEquals(message.getId(), result.getId());
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

        server.configure(serverConfiguration);
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
        client1.configure(clientConfiguration);
        client1.startup();
        client1.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());

        Thread.sleep(1000);
        Message message = new Message("type");
        Message result = client1.sendSyncMessage(message, SERVER);
        assertNotNull(result);
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());

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
        client2.configure(clientConfiguration2);
        client2.startup();
        client2.getMessageQueue().addMessageHandler("type", new TestMessageProcessor());
        

        Thread.sleep(1000);
        try {
        	message = new Message("type");
        	result = client2.sendSyncMessage(message, SERVER);
            assertNotNull(result);
            assertEquals("", result.getType());
            assertEquals(message.getId(), result.getId());
        } catch (SocketException e) {
            fail();
        }

    }
}
