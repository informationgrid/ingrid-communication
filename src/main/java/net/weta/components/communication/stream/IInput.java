package net.weta.components.communication.stream;

import java.io.IOException;

public interface IInput {

    int readInt() throws IOException, MessageSizeTooBigException;

    String readString() throws IOException, MessageSizeTooBigException;

    Object readObject() throws IOException, MessageSizeTooBigException;

    byte[] readBytes() throws IOException, MessageSizeTooBigException;

    boolean readBoolean() throws IOException;
}
