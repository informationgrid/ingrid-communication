package net.weta.components.communication.util;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;

public class MessageUtilTest extends TestCase {
    public void testSerialization() throws Exception {
        new MessageUtil();

        Message messageToSerialize = new Message("messagename");
        messageToSerialize.setId(-1);
        messageToSerialize.setType("type");
        byte[] bs = MessageUtil.serialize(messageToSerialize);
        Message messageFromDeserialize = MessageUtil.deserialize(bs);

        assertEquals(-1, messageFromDeserialize.getId());
        assertEquals("type", messageFromDeserialize.getType());
    }
}
