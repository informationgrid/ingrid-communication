package net.weta.components.communication.messaging;

import java.io.IOException;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public class AuthenticationMessage extends Message {

    private static final long serialVersionUID = 4250041361202865796L;

    private byte[] _token = new byte[0];

    public AuthenticationMessage() {
        // nothig todo
    }

    public AuthenticationMessage(byte[] token) {
        _token = token;
    }

    public byte[] getToken() {
        return _token;
    }

    public void read(IInput in) throws IOException {
        _token = in.readBytes();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeBytes(_token);
        super.write(out);
    }

}
