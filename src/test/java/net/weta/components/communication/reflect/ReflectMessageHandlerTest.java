/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import java.io.Serializable;

import net.weta.components.communication.CommunicationException;
import net.weta.components.communication.ExternalizableCreator;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.test.DummyExternalizable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ReflectMessageHandlerTest {

    @Test
    public void testAddObjectToCall() throws Exception {
        ReflectMessageHandler handler = new ReflectMessageHandler();
        String s = "hello";
        handler.addObjectToCall(String.class, s);
    }

    @Test
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
