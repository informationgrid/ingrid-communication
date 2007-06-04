package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

public class StartCommunicationTest extends TestCase {

    public void testStart() {
        InputStream resourceAsStream = StartCommunication.class.getResourceAsStream("/communication.properties");
        try {
            TcpCommunication communication = (TcpCommunication) StartCommunication.create(resourceAsStream);
            assertEquals("message-server", communication.getPeerName());
            List server = communication.getServers();
            assertTrue(server.contains("127.0.0.1:8080"));
            assertTrue(server.contains("127.0.0.1:8081"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMain() {
        try {
            StartCommunication.main(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
