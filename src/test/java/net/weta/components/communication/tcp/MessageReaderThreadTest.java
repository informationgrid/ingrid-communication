package net.weta.components.communication.tcp;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.util.MessageUtil;

public class MessageReaderThreadTest extends TestCase {

    Thread _thread;

    boolean _isStarted = false;

    MessageReaderThread _mrThread;

    private ServerSocket _serverSocket;

    public void testBla() throws Exception {
        assertTrue(true);
    }
//    protected void setUp() throws Exception {
//        Runnable runnable = new Runnable() {
//
//            public void run() {
//                try {
//                    _serverSocket = new ServerSocket(65535);
//                } catch (IOException e) {
//                    fail(e.getMessage());
//                }
//                synchronized (_thread) {
//                    _thread.notify();
//                    _isStarted = true;
//                }
//                Socket isocket = null;
//                try {
//                    isocket = _serverSocket.accept();
//                } catch (IOException e) {
//                    fail();
//                }
//                _mrThread = new MessageReaderThread("name", isocket, new MessageQueue(), null, 1, 1024 * 1204);
//                _mrThread.start();
//            }
//        };
//        _thread = new Thread(runnable);
//        _thread.start();
//    }
//
//    public void testEOFException() throws InterruptedException {
//        Socket socket = null;
//        try {
//            synchronized (_thread) {
//                if (!_isStarted) {
//                    _thread.wait();
//                }
//            }
//            socket = new Socket("localhost", 65535);
//            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//            os.writeInt(1);
//            os.write(new byte[] { 1 });
//            os.flush();
//        } catch (EOFException e) {
//            assertTrue(true);
//        } catch (IOException e) {
//            fail();
//        }
//        try {
//            _serverSocket.close();
//        } catch (IOException e) {
//            // nothing todo
//        }
//    }
//
//    public void testMaxThreadCount() {
//        try {
//            synchronized (_thread) {
//                if (!_isStarted) {
//                    _thread.wait();
//                }
//            }
//            Socket socket = new Socket("localhost", 65535);
//            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//            PayloadMessage pmsg = new PayloadMessage("bla", "bla");
//            pmsg.setId(1);
//            byte[] msg = MessageUtil.serialize(pmsg);
//            os.writeInt(msg.length);
//            os.write(msg);
//            os.flush();
//            pmsg.setId(2);
//            msg = MessageUtil.serialize(pmsg);
//            os.writeInt(msg.length);
//            os.write(msg);
//            os.flush();
//        } catch (Exception e) {
//            fail();
//        }
//        try {
//            if (_thread != null) {
//                _thread.join();
//            }
//            _serverSocket.close();
//        } catch (Exception e) {
//            // nothing todo
//        }
//    }
}
