package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;

public class StartCommunicationTest extends TestCase {

    public void testStart() {
        InputStream resourceAsStream = StartCommunication.class.getResourceAsStream("/communication.properties");
        try {
            ICommunication communication = StartCommunication.create(resourceAsStream);
            assertEquals("message-server", communication.getPeerName());
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
