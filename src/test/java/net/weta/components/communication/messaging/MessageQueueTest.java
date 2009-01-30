package net.weta.components.communication.messaging;

import junit.framework.TestCase;
import net.weta.components.test.DummyExternalizable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * test case created on 27.12.2004
 * 
 * @version $Revision$
 * 
 */
public class MessageQueueTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testAddMessage() throws Exception {
        MessageQueue messageQueue = new MessageQueue();
        messageQueue.getProcessorRegistry().addMessageHandler("type", new TestMessageProcessor());

        Message msg = new PayloadMessage(new DummyExternalizable(), "");
        int timeout = 5;
        messageQueue.messageEvent(msg);
        assertEquals(1, messageQueue.size());

        Message message = messageQueue.waitForMessage(msg.getId(), timeout);
        if (null != message) {
            assertEquals(0, messageQueue.size());
        } else {
            fail("timeout: no message received during time intervall");
        }
    }

    /**
     * @throws Exception
     */
    public void testWaitForMessage() throws Exception {
        Logger logger = Logger.getLogger(MessageQueue.class);
        logger.setLevel(Level.DEBUG);
        final MessageQueue messageQueue = new MessageQueue();
        messageQueue.getProcessorRegistry().addMessageHandler("type", new TestMessageProcessor());
        Message message1 = new PayloadMessage(new DummyExternalizable(), "");
        messageQueue.messageEvent(message1);

        assertEquals(1, messageQueue.size());

        PayloadMessage payloadMessage = (PayloadMessage) messageQueue.waitForMessage(message1.getId(), 5);
        assertNotNull(payloadMessage);

        assertNull(messageQueue.waitForMessage(message1.getId(), 2));

        final Message message = new PayloadMessage(new DummyExternalizable(), "");
        message.setId("" + 23);
        Thread thread = new Thread() {
            public void run() {
                assertNotNull(messageQueue.waitForMessage(message.getId(), 5));
            }
        };
        thread.start();

        messageQueue.messageEvent(message1);
        Thread.sleep(2000);
        messageQueue.messageEvent(message1);
        messageQueue.messageEvent(message1);
        messageQueue.messageEvent(message1);
        messageQueue.messageEvent(message);
        Thread.sleep(5000);
        assertEquals(1, messageQueue.size());
    }

    public void testGetProcessorRegistry() throws Exception {
        MessageQueue messageQueue = new MessageQueue();
        IMessageHandler[] messageHandlers = messageQueue.getProcessorRegistry().getMessageHandlers();
        assertEquals(0, messageHandlers.length);

        messageQueue.getProcessorRegistry().addMessageHandler("type", new TestMessageProcessor());
        messageHandlers = messageQueue.getProcessorRegistry().getMessageHandlers();
        assertEquals(1, messageHandlers.length);
    }

    public void testClear() throws Exception {
        MessageQueue messageQueue = new MessageQueue();
        messageQueue.getProcessorRegistry().addMessageHandler("type", new TestMessageProcessor());
        Message msg = new PayloadMessage(new DummyExternalizable(), "");
        messageQueue.messageEvent(msg);
        assertEquals(1, messageQueue.size());

        messageQueue.clear();
        assertEquals(0, messageQueue.size());
    }

    public void testMessageEvent() throws Exception {
        MessageQueue messageQueue = new MessageQueue();
        Message message = new PayloadMessage(new DummyExternalizable(), "type");
        PayloadMessage replyMessage = (PayloadMessage) messageQueue.messageEvent(message);

        assertTrue(replyMessage.getType().equals(message.getType()));
        assertTrue(replyMessage.getId() == message.getId());
        System.out.println(replyMessage.getPayload().toString());

        messageQueue.getProcessorRegistry().addMessageHandler("type", new TestMessageProcessor() {
            public Message handleMessage(Message message) {
                throw new RuntimeException("test messageHandler throws RT exception");
            }
        });
        messageQueue.messageEvent(message);
    }

    public void testMessageOverrun() {
        MessageQueue queue = new MessageQueue();
        queue.setMaxSize(1);
        
        PayloadMessage message = new PayloadMessage("bla", "");

        for (int i = 0; i < 100; i++) {
            message.setId("" + i);
            queue.messageEvent(message);
            assertEquals(1, queue.size());            
        }
    }
}
