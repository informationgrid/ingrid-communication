package net.weta.components.communication.stream;

import java.io.IOException;

public interface IOutput {

    void writeInt(int i) throws IOException;

    void writeString(String string) throws IOException;

    void writeObject(Object object) throws IOException;

    void writeBytes(byte[] token) throws IOException;

    void writeBoolean(boolean b) throws IOException;

    void flush() throws IOException;
}
