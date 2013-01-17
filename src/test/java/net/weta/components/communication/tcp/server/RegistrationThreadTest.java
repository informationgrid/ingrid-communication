package net.weta.components.communication.tcp.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import net.weta.components.communication.security.JavaKeystore;
import net.weta.components.communication.security.SecurityUtil;
import sun.security.tools.KeyTool;

public class RegistrationThreadTest extends TestCase {

    Thread _thread;

    boolean _isStarted = false;

    private File _keystore;

    private File _securityFolder;

    protected void setUp() throws Exception {
        _isStarted = false;
        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        _keystore = new File(_securityFolder, "keystore");

        KeyTool.main(new String[] { "-genkey", "-keystore", _keystore.getAbsolutePath(), "-alias", "testAlias",
                "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        JavaKeystore keystore = new JavaKeystore(_keystore, "password");
        final SecurityUtil util = new SecurityUtil(keystore);

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
                RegistrationThread registrationThread = new RegistrationThread(isocket, null, 50, 1024, util);
                registrationThread.start();
            }
        };
        _thread = new Thread(runnable);
        _thread.start();
    }

    protected void tearDown() {
        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        _securityFolder.delete();
    }

    public void testMessageSizeTooBig() throws UnknownHostException, IOException, InterruptedException {
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            Thread.sleep(2000);
            Socket s = new Socket("localhost", 65535);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            try {
                out.print(2048);
                out.print("This is a faked long message. the string here should be at lease 25 characters long.");
                out.flush();
            } finally {
                out.close();
            }
            
            
        } catch (IOException e) {
            fail();
        }
        System.out.println("A MessageSizeTooBigException should have been thrown.");
        
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
