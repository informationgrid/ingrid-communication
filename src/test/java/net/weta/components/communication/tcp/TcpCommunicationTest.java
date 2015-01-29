/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
/*
 * Created on 16.05.2007
 */
package net.weta.components.communication.tcp;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.security.JavaKeystoreTest;

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

        JavaKeystoreTest.generateKeyInKeyStore(keystoreServer, SERVER);
        JavaKeystoreTest.generateKeyInKeyStore(keystoreClient, CLIENT);
        JavaKeystoreTest.exportCertficate(keystoreClient, CLIENT, clientCertificate);
        JavaKeystoreTest.importCertficate(keystoreServer, CLIENT, clientCertificate);

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                ServerConfiguration serverConfiguration = new ServerConfiguration();
                serverConfiguration.setName(SERVER);
                serverConfiguration.setPort(10091);
                serverConfiguration.setKeystorePath(keystoreServer.getAbsolutePath());
                serverConfiguration.setKeystorePassword("password");
                _tcpCommunicationServer.configure(serverConfiguration);
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
                ClientConfiguration clientConfiguration = new ClientConfiguration();
                clientConfiguration.setName(CLIENT);
                ClientConnection clientConnection = clientConfiguration.new ClientConnection();
                clientConnection.setServerIp("127.0.0.1");
                clientConnection.setServerPort(10091);
                clientConnection.setServerName(SERVER);
                clientConnection.setKeystorePath(keystoreClient.getAbsolutePath());
                clientConnection.setKeystorePassword("password");
                
                clientConfiguration.addClientConnection(clientConnection);
                _tcpCommunicationClient.configure(clientConfiguration);

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

        // unable to delete files under windows, because they are locked
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
	        File[] files = _securityFolder.listFiles();
	        for (int i = 0; i < files.length; i++) {
	            assertTrue(files[i].delete());
	        }
	        assertTrue(_securityFolder.delete());
        }
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
