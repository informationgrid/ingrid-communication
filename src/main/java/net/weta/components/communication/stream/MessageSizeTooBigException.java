package net.weta.components.communication.stream;

import java.io.Serializable;

public class MessageSizeTooBigException extends IllegalArgumentException implements Serializable {


    private static final long serialVersionUID = -2659781317564260580L;

    public MessageSizeTooBigException(String string) {
        super(string);
    }

}
