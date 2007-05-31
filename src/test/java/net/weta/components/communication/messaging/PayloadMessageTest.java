package net.weta.components.communication.messaging;

import junit.framework.TestCase;

public class PayloadMessageTest extends TestCase {

    public void testSetPayload() {
        PayloadMessage msg = new PayloadMessage("some Payload", String.class.getName());
        assertEquals("some Payload", msg.getPayload());
        msg.setPayload("new Payload");
        assertEquals("new Payload", msg.getPayload());
    }

}
