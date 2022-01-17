/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package net.weta.components.communication.reflect;

import java.io.File;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.security.JavaKeystoreTest;
import net.weta.components.communication.tcp.TcpCommunication;
import net.weta.components.communication.tcp.TimeoutException;

public class ReflectInvocationHandlerTest extends TestCase {

    public interface ITest {
        String testMethod();
    }

    private TcpCommunication _tcpCommunicationClient;

    private TcpCommunication _tcpCommunicationServer;

    private File _securityFolder;

    private static final String CLIENT = "/kug-group:client";

    private static final String SERVER = "/kug-group:server";

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

        _tcpCommunicationServer = new TcpCommunication();

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(55556);
        serverConfiguration.setKeystorePath(keystoreServer.getAbsolutePath());
        serverConfiguration.setKeystorePassword("password");
        serverConfiguration.setName(SERVER);

        _tcpCommunicationServer.configure(serverConfiguration);
        _tcpCommunicationServer.startup();

        _tcpCommunicationClient = new TcpCommunication();

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55556);
        clientConnection.setServerName(SERVER);
        clientConnection.setKeystorePath(keystoreClient.getAbsolutePath());
        clientConnection.setKeystorePassword("password");
        clientConfiguration.setHandleTimeout(2);
        clientConfiguration.addClientConnection(clientConnection);
        _tcpCommunicationClient.configure(clientConfiguration);

        _tcpCommunicationClient.startup();

    }

    protected void tearDown() throws Exception {
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.shutdown();

        File[] files = _securityFolder.listFiles();

        // unable to delete files under windows, because they are locked
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            for (int i = 0; i < files.length; i++) {
                assertTrue(files[i].delete());
            }
            assertTrue(_securityFolder.delete());
        }

    }

    public void testInvoke() throws Exception {

        ReflectInvocationHandler handler = new ReflectInvocationHandler(_tcpCommunicationClient, "/kug-group:a");
        ITest test = (ITest) Proxy.newProxyInstance(ITest.class.getClassLoader(), new Class[] { ITest.class }, handler);
        try {
            test.testMethod();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            TimeoutException exception = (TimeoutException) cause;
            assertNotNull(exception);
        }
    }
}
