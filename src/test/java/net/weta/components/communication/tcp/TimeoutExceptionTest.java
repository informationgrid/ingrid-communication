package net.weta.components.communication.tcp;

import junit.framework.TestCase;

public class TimeoutExceptionTest extends TestCase {

    public void testTimeoutException() {
        TimeoutException exception = new TimeoutException("exception message");
        assertEquals("exception message", exception.getMessage());
    }
}
