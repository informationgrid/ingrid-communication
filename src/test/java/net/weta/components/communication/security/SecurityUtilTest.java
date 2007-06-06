package net.weta.components.communication.security;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test for the SecurityUtil class. Created on 06.06.2005.
 */
public class SecurityUtilTest extends TestCase {

    private File _file;

    private File _securityFolder;

    protected void setUp() throws Exception {
        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        _file = new File(_securityFolder, "keystore");
        JavaKeystoreTest.generateKeyInKeyStore(_file, "alice");
    }

    protected void tearDown() {
        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());
    }

    /**
     * @throws SecurityException
     * @throws IOException
     * 
     */
    public void testSign() throws SecurityException, IOException {
        byte[] bytes = "Hello World!".getBytes();
        JavaKeystore keystore = new JavaKeystore(_file, "password");
        SecurityUtil util = new SecurityUtil(keystore);
        byte[] bs = util.computeSignature("alice", bytes);
        assertNotNull(bs);
        assertTrue(bs.length > 0);
        assertTrue(util.verifySignature("alice", bytes, bs));
    }
}
