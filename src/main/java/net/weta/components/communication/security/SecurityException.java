package net.weta.components.communication.security;

public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 6767348551299736798L;

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
