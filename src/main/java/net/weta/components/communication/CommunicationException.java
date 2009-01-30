package net.weta.components.communication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CommunicationException extends Exception implements Externalizable {

    private static final long serialVersionUID = 7814850482745259583L;

    private String _message;

    public CommunicationException() {
        // nothing todo
    }

    public CommunicationException(String message) {
        _message = message;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _message = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_message);
    }

     @Override
    public String toString() {
        return _message;
    }

}
