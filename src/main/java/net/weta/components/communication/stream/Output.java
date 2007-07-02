package net.weta.components.communication.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Output implements IOutput {

    private final DataOutputStream _dataOutput;

    public Output(DataOutputStream dataOutput) {
        _dataOutput = dataOutput;
    }

    public void writeInt(int i) throws IOException {
        _dataOutput.writeInt(i);
    }

    public void writeObject(Object object) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(arrayOutputStream);
        stream.writeObject(object);
        byte[] bytes = arrayOutputStream.toByteArray();
        writeBytes(bytes);
        stream.close();
    }

    public void writeString(String string) throws IOException {
        writeBytes(string.getBytes());
    }

    public void writeBytes(byte[] bytes) throws IOException {
        writeInt(bytes.length);
        _dataOutput.write(bytes);
    }

    public void writeBoolean(boolean b) throws IOException {
        _dataOutput.writeBoolean(b);
    }

    public void flush() throws IOException {
        _dataOutput.flush();
    }
}
