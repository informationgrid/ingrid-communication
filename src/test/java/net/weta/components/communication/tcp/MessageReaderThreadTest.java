package net.weta.components.communication.tcp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.stream.Input;
import net.weta.components.communication.stream.Output;
import net.weta.components.test.DummyExternalizable;

public class MessageReaderThreadTest extends TestCase {

    Thread _thread;

    boolean _isStarted = false;

    MessageReaderThread _mrThread;

    private ServerSocket _serverSocket;

    protected void setUp() throws Exception {
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    _serverSocket = new ServerSocket(65535);
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                synchronized (_thread) {
                    _thread.notify();
                    _isStarted = true;
                }
                Socket isocket = null;
                IInput input = null;
                try {
                    isocket = _serverSocket.accept();
                    input = new Input(new DataInputStream(isocket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }
                _mrThread = new MessageReaderThread("name", input, new MessageQueue(), null, 1024 * 1204);
                _mrThread.start();
            }
        };
        _thread = new Thread(runnable);
        _thread.start();
    }

    public void testEOFException() throws InterruptedException {
        Socket socket = null;
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            socket = new Socket("localhost", 65535);
            IOutput out = new Output(new DataOutputStream(socket.getOutputStream()));
            new Message().write(out);
        } catch (EOFException e) {
            assertTrue(true);
        } catch (IOException e) {
            fail();
        }
        try {
            _serverSocket.close();
        } catch (IOException e) {
            // nothing todo
        }
    }

    public void testMaxThreadCount() {
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            Socket socket = new Socket("localhost", 65535);
            IOutput os = new Output(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));
            PayloadMessage pmsg = new PayloadMessage(new DummyExternalizable(), "bla");
            pmsg.setId("1");
            pmsg.write(os);
            pmsg.setId("2");
            pmsg.write(os);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        try {
            if (_thread != null) {
                _thread.join();
            }
            _serverSocket.close();
        } catch (Exception e) {
            // nothing todo
        }
    }
}
