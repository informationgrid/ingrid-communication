package net.weta.components.communication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CommunicationException extends Exception implements Externalizable {

    private static final long serialVersionUID = 7814850482745259583L;

    public CommunicationException() {
        // nothing todo
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // nothing todo
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // nothing todo
    }

}
