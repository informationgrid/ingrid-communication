/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
    
    public void testHashCode() {
        String p1 = "param";
        ReflectMessage m1 = new ReflectMessage("search", ReflectMessage.class.getName(), new Object[] { p1, 10, 1 });
        String p2 = "param";
        ReflectMessage m2 = new ReflectMessage("search", ReflectMessage.class.getName(), new Object[] { p1, 10, 1 });
        assertTrue(m1.hashCode() == m2.hashCode());
    }

}
