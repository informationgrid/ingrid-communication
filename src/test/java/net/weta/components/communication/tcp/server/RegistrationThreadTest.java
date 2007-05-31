package net.weta.components.communication.tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class RegistrationThreadTest extends TestCase {

    Thread _thread;

    boolean _isStarted = false;

    protected void setUp() throws Exception {
        _isStarted = false;
        Runnable runnable = new Runnable() {

            public void run() {
                ServerSocket serversocket = null;
                try {
                    serversocket = new ServerSocket(65535);
                } catch (IOException e) {
                    fail();
                }
                synchronized (_thread) {
                    _thread.notify();
                    _isStarted = true;
                }
                Socket isocket = null;
                try {
                    isocket = serversocket.accept();
                } catch (IOException e) {
                    fail();
                }
                RegistrationThread registrationThread = new RegistrationThread(isocket, null, 50, 3);
                registrationThread.start();
            }
        };
        _thread = new Thread(runnable);
        _thread.start();
    }

    public void testTimeout() throws UnknownHostException, IOException, InterruptedException {
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            new Socket("localhost", 65535);
            Thread.sleep(4000);
        } catch (IOException e) {
            fail();
        }
    }
}
