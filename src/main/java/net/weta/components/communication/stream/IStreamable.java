package net.weta.components.communication.stream;

import java.io.IOException;

public interface IStreamable {

    void write(IOutput output) throws IOException;

    void read(IInput input) throws IOException;
}
