/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source$
 */

package net.weta.components.communication.messaging;

import junit.framework.TestCase;

/**
 * Test the message processor registry. Created on 28.12.2004.
 * 
 * @version $Revision$
 */
public class MessageProcessorRegistryTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testAddHandler() throws Exception {
        TestMessageProcessor handler = new TestMessageProcessor();
        MessageProcessorRegistry instance = new MessageProcessorRegistry();
        assertNotNull(instance);
        instance.addMessageHandler("test", handler);
        assertEquals(1, instance.getMessageHandlers().length);
        instance.removeMessageHandler("test", handler);
        assertEquals(0, instance.getMessageHandlers().length);
    }

    /**
     * @throws Exception
     */
    public void testGetHandlerForType() throws Exception {
        TestMessageProcessor handler = new TestMessageProcessor();
        MessageProcessorRegistry instance = new MessageProcessorRegistry();
        assertNotNull(instance);
        instance.addMessageHandler("test2", handler);
        assertEquals(1, instance.getMessageHandlers().length);
        IMessageHandler[] handlersForType = instance.getHandlersForType("test2");
        assertEquals(1, handlersForType.length);
    }

    public void testAddMessageHandler() throws Exception {
        TestMessageProcessor handler = new TestMessageProcessor();
        MessageProcessorRegistry instance = new MessageProcessorRegistry();
        assertNotNull(instance);
        instance.addMessageHandler("test3", handler);
        assertEquals(1, instance.getMessageHandlers().length);
        IMessageHandler[] handlersForType = instance.getHandlersForType("test3");
        assertEquals(1, handlersForType.length);

        TestMessageProcessor handler2 = new TestMessageProcessor();
        try {
            instance.addMessageHandler("test3", handler2);
            fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getLocalizedMessage().startsWith(
                    "just one responsefull message handler per message type allowed"));
        }
    }

    public void testRemoveMessageHandlers() throws Exception {
        TestMessageProcessor handler = new TestMessageProcessor();
        MessageProcessorRegistry instance = new MessageProcessorRegistry();
        assertNotNull(instance);
        instance.addMessageHandler("test4", handler);
        assertEquals(1, instance.getMessageHandlers().length);
        IMessageHandler[] handlersForType = instance.getHandlersForType("test4");
        assertEquals(1, handlersForType.length);

        IMessageHandler[] removedHandlers = instance.removeMessageHandlers("test4");
        assertTrue(removedHandlers.length == 1);
        assertEquals(0, instance.getMessageHandlers().length);
    }
}
