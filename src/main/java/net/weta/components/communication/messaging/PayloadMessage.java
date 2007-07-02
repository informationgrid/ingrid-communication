package net.weta.components.communication.messaging;

import java.io.IOException;
import java.io.Serializable;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public class PayloadMessage extends Message {

    private static final long serialVersionUID = 2685915429239362958L;

    private Serializable _payload;

    public PayloadMessage() {
    }

    public PayloadMessage(Serializable payload, String type) {
        super(type);
        _payload = payload;
    }

    public Serializable getPayload() {
        return _payload;
    }

    public void read(IInput in) throws IOException {
        _payload = (Serializable) in.readObject();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeObject(_payload);
        super.write(out);
    }
}
