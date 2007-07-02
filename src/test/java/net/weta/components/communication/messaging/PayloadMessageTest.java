package net.weta.components.communication.messaging;

import junit.framework.TestCase;
import net.weta.components.test.DummyExternalizable;

public class PayloadMessageTest extends TestCase {

    public void testSetPayload() {
        DummyExternalizable externalizable1 = new DummyExternalizable();
        PayloadMessage msg = new PayloadMessage(externalizable1, String.class.getName());
        assertEquals(externalizable1, msg.getPayload());
    }

}
