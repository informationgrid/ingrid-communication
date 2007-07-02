package net.weta.components.communication.reflect;

import java.io.Serializable;

import junit.framework.TestCase;
import net.weta.components.communication.CommunicationException;
import net.weta.components.communication.ExternalizableCreator;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.test.DummyExternalizable;

public class ReflectMessageHandlerTest extends TestCase {

    public void testAddObjectToCall() throws Exception {
        ReflectMessageHandler handler = new ReflectMessageHandler();
        String s = "hello";
        handler.addObjectToCall(String.class, s);
    }

    public void testHandleMessage() {
        ReflectMessageHandler handler = new ReflectMessageHandler();
        ExternalizableCreator creator = new ExternalizableCreator();
        DummyExternalizable externalizable = new DummyExternalizable();
        handler.addObjectToCall(ExternalizableCreator.class, creator);
        ReflectMessage message = new ReflectMessage("create", ExternalizableCreator.class.getName());
        Message msg = handler.handleMessage(message);
        if (null == msg) {
            fail();
        }
        PayloadMessage retrieved = (PayloadMessage) msg;
        Serializable payload = retrieved.getPayload();
        assertEquals(externalizable, payload);

        message = new ReflectMessage("notExistentMethod", String.class.getName());
        msg = handler.handleMessage(message);
        retrieved = (PayloadMessage) msg;
        Serializable payload2 = retrieved.getPayload();
        assertTrue(payload2.getClass().isAssignableFrom(CommunicationException.class));
    }

}
