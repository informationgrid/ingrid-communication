package net.weta.components.communication.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.weta.components.communication.messaging.Message;

public class MessageUtil {

    public static Message deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object = stream.readObject();
        stream.close();
        Message message = (Message) object;
        return message;
    }

    public static byte[] serialize(Message message) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(arrayOutputStream);
        stream.writeObject(message);
        byte[] bytes = arrayOutputStream.toByteArray();
        stream.close();
        return bytes;
    }
}
