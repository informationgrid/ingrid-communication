package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.Configuration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;

public class StartCommunicationTest extends TestCase {

    public void testStart() {
        InputStream resourceAsStream = StartCommunication.class.getResourceAsStream("/validClientConfiguration.xml");
        try {
            TcpCommunication communication = (TcpCommunication) StartCommunication.create(resourceAsStream);
            assertEquals("clientName", communication.getPeerName());
            communication.getRegisteredClients();
            Configuration configuration = communication.getConfiguration();
            ClientConfiguration clientConfiguration = (ClientConfiguration) configuration;
            ClientConnection clientConnection = (ClientConnection) clientConfiguration.getClientConnections().get(0);
            assertEquals("127.0.0.1", clientConnection.getServerIp());
            assertEquals(80, clientConnection.getServerPort());
            ClientConnection clientConnection2 = (ClientConnection) clientConfiguration.getClientConnections().get(1);
            assertEquals("127.0.0.2", clientConnection2.getServerIp());
            assertEquals(82, clientConnection2.getServerPort());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
