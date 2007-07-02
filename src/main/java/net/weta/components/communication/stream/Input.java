package net.weta.components.communication.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Input implements IInput {

    private final DataInput _dataInput;

    public Input(DataInput dataInput) {
        _dataInput = dataInput;
    }

    public byte[] readBytes() throws IOException {
        int length = readInt();
        byte[] bytes = new byte[length];
        _dataInput.readFully(bytes);
        return bytes;
    }

    public int readInt() throws IOException {
        return _dataInput.readInt();
    }

    public Object readObject() throws IOException {
        byte[] bytes = readBytes();
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object;
        try {
            object = stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        } finally {
            stream.close();
        }
        return object;
    }

    public String readString() throws IOException {
        byte[] bytes = readBytes();
        return new String(bytes);
    }

    public boolean readBoolean() throws IOException {
        return _dataInput.readBoolean();
    }

}