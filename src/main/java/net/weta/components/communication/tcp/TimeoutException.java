package net.weta.components.communication.tcp;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TimeoutException extends Exception implements Externalizable {

    private static final long serialVersionUID = -3386906193157967733L;

    public TimeoutException(String string) {
        super(string);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // nothing todo
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // nothing todo
    }

}
