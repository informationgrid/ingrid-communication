package net.weta.components.communication.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Input implements IInput {

    private final DataInput _dataInput;

    private final int _maxMessageSize;

    private static final Logger LOG = Logger.getLogger(Input.class);
    
    public Input(DataInput dataInput, int maxMessageSize) {
        _dataInput = dataInput;
        _maxMessageSize = maxMessageSize;
    }

    public byte[] readBytes() throws MessageSizeTooBigException, IOException {
        int length = readInt();
        if (length > _maxMessageSize) {
            throw new MessageSizeTooBigException("message size too big: [" + length + "]");
        }
        byte[] bytes = new byte[length];
        _dataInput.readFully(bytes);
        return bytes;
    }

    public int readInt() throws IOException {
        return _dataInput.readInt();
    }

    public Object readObject() throws MessageSizeTooBigException, IOException {
        byte[] bytes = readBytes();
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object;
        try {
            object = stream.readObject();
        } catch (ClassNotFoundException e) {
            LOG.log(Level.ERROR, "class not found: " + e.getMessage(), e);
            throw new IOException("class not found: " + e.getMessage());
        } finally {
            stream.close();
        }
        return object;
    }

    public String readString() throws MessageSizeTooBigException, IOException {
        byte[] bytes = readBytes();
        return new String(bytes);
    }

    public boolean readBoolean() throws IOException {
        return _dataInput.readBoolean();
    }

}
