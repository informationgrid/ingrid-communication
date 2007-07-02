package net.weta.components.communication.reflect;

import junit.framework.TestCase;

public class ReflectMessageTest extends TestCase {

    public void testReflectMessageStringString() {
        ReflectMessage message = new ReflectMessage("method", "class");
        assertNotNull(message);
    }

    public void testReflectMessageStringStringObjectArray() {
        ReflectMessage message = new ReflectMessage("method", "class", new Object[] {});
        assertNotNull(message);
    }

    public void testGetMethodName() {
        ReflectMessage message = new ReflectMessage("method", "class", new Object[] {});
        assertEquals("method", message.getMethodName());
    }

    public void testGetObjectToCallClass() {
        ReflectMessage message = new ReflectMessage("method", "class", new Object[] {});
        assertEquals("class", message.getObjectToCallClass());
    }

    public void testGetArgumentClasses() {
        ReflectMessage message = new ReflectMessage("method", "class");
        Class[] argumentClasses = message.getArgumentClasses();
        assertEquals(0, argumentClasses.length);

        message = new ReflectMessage("method", "class", new Object[] { new Integer(0), new Object() });
        argumentClasses = message.getArgumentClasses();
        assertNotNull(argumentClasses);
    }

    public void testGetArguments() {
        Object[] argumentClasses = null;
        ReflectMessage message = new ReflectMessage("method", "class");

        Object object1 = new Object();
        Object object2 = new Object();
        message = new ReflectMessage("method", "class", new Object[] { object1, object2 });

        argumentClasses = message.getArguments();
        assertNotNull(argumentClasses);

        assertEquals(object1, argumentClasses[0]);
        assertEquals(object2, argumentClasses[1]);
    }

}
