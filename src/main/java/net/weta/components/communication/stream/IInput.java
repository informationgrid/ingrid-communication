package net.weta.components.communication.stream;

import java.io.IOException;

public interface IInput {

    int readInt() throws IOException;

    String readString() throws IOException;

    Object readObject() throws IOException;

    byte[] readBytes() throws IOException;

    boolean readBoolean() throws IOException;
}
