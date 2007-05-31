package net.weta.components.communication.reflect;

import java.io.Serializable;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.PayloadMessage;

public class ReflectMessageHandlerTest extends TestCase {

    public void testAddObjectToCall() throws Exception {
        ReflectMessageHandler handler = new ReflectMessageHandler();
        String s = "hello";
        handler.addObjectToCall(String.class, s);
    }

    public void testHandleMessage() {
        ReflectMessageHandler handler = new ReflectMessageHandler();
        String s = "hello";
        handler.addObjectToCall(String.class, s);
        ReflectMessage message = new ReflectMessage("toString", String.class.getName());
        Message msg = handler.handleMessage(message);
        if (null == msg) {
            fail();
        }
        PayloadMessage retrieved = (PayloadMessage) msg;
        Serializable payload = retrieved.getPayload();
        assertEquals(s, payload);

        message = new ReflectMessage("notExistentMethod", String.class.getName());
        msg = handler.handleMessage(message);
        retrieved = (PayloadMessage) msg;
        Exception e = new NoSuchMethodException("notExistentMethod");
        assertEquals(e.toString(), retrieved.getPayload().toString());
    }

}
