package net.weta.components.communication.messaging;

import java.io.IOException;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public class RegistrationMessage extends Message {

    private static final long serialVersionUID = 4579214339793912508L;

    private String _registrationName = "";

    private byte[] _signature = new byte[0];

    public String getRegistrationName() {
        return _registrationName;
    }

    public void setRegistrationName(String registrationName) {
        _registrationName = registrationName;
    }

    public byte[] getSignature() {
        return _signature;
    }

    public void setSignature(byte[] signature) {
        _signature = signature;
    }

    public void read(IInput in) throws IOException {
        _registrationName = in.readString();
        _signature = in.readBytes();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeString(_registrationName);
        out.writeBytes(_signature);
        super.write(out);
    }

}
