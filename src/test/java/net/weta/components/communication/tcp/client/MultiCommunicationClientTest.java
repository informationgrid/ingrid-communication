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
/*
 * Created on 04.06.2007
 */
package net.weta.components.communication.tcp.client;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.TestMessageProcessor;
import net.weta.components.communication.security.JavaKeystoreTest;
import net.weta.components.communication.tcp.TcpCommunication;

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

        JavaKeystoreTest.generateKeyInKeyStore(keystoreServer, SERVER);
        JavaKeystoreTest.generateKeyInKeyStore(keystoreServer2, SERVER2);
        JavaKeystoreTest.generateKeyInKeyStore(keystoreClient, CLIENT);
        JavaKeystoreTest.exportCertficate(keystoreClient, CLIENT, clientCertificate);
        JavaKeystoreTest.importCertficate(keystoreServer, CLIENT, clientCertificate);
        JavaKeystoreTest.importCertficate(keystoreServer2, CLIENT, clientCertificate);

        _serverRunnable = new Runnable() {
            public void run() {
                _tcpCommunicationServer = new TcpCommunication();
                ServerConfiguration serverConfiguration = new ServerConfiguration();
                serverConfiguration.setName(SERVER);
                serverConfiguration.setPort(9193);
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

        _serverRunnable2 = new Runnable() {
            public void run() {
                _tcpCommunicationServer2 = new TcpCommunication();
                ServerConfiguration serverConfiguration = new ServerConfiguration();
                serverConfiguration.setName(SERVER2);
                serverConfiguration.setPort(9194);
                serverConfiguration.setKeystorePath(keystoreServer2.getAbsolutePath());
                serverConfiguration.setKeystorePassword("password");
                _tcpCommunicationServer2.configure(serverConfiguration);

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
                ClientConfiguration clientConfiguration = new ClientConfiguration();
                clientConfiguration.setName(CLIENT);

                ClientConnection clientConnection = clientConfiguration.new ClientConnection();
                clientConnection.setServerIp("127.0.0.1");
                clientConnection.setServerPort(9193);
                clientConnection.setServerName(SERVER);
                clientConnection.setKeystorePath(keystoreClient.getAbsolutePath());
                clientConnection.setKeystorePassword("password");

                ClientConnection clientConnection2 = clientConfiguration.new ClientConnection();
                clientConnection2.setServerIp("127.0.0.1");
                clientConnection2.setServerPort(9194);
                clientConnection2.setServerName(SERVER2);
                clientConnection2.setKeystorePath(keystoreClient.getAbsolutePath());
                clientConnection2.setKeystorePassword("password");

                clientConfiguration.addClientConnection(clientConnection);
                clientConfiguration.addClientConnection(clientConnection2);

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

        // unable to delete files under windows, because they are locked
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            File[] files = _securityFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                assertTrue(files[i].delete());
            }
            assertTrue(_securityFolder.delete());
        }
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

    public void testSendSyncMessageFromClientToServer() throws Exception {
        Thread.sleep(3000);
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
        assertEquals("", result.getType());
        assertEquals(message.getId(), result.getId());
    }
}
