package net.weta.components.communication.tcp.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import net.weta.components.communication.security.JavaKeystore;
import net.weta.components.communication.security.SecurityUtil;

import sun.security.tools.KeyTool;

import junit.framework.TestCase;

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
                RegistrationThread registrationThread = new RegistrationThread(isocket, null, 50, util);
                registrationThread.start();
            }
        };
        _thread = new Thread(runnable);
        _thread.start();
    }

    protected void tearDown() {
        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());
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
