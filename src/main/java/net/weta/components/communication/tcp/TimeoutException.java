package net.weta.components.communication.tcp;

import java.io.Serializable;

public class TimeoutException extends Exception implements Serializable {

    private static final long serialVersionUID = -3386906193157967733L;

    public TimeoutException(String string) {
        super(string);
    }

}
