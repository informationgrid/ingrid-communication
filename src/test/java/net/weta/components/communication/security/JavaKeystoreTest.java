package net.weta.components.communication.security;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import sun.security.tools.KeyTool;

/**
 * To test the keystore.
 * 
 * created on 06.06.2005
 * <p>
 */
public class JavaKeystoreTest extends TestCase {

    private File _keystore;

    private File _securityFolder;

    protected void setUp() throws Exception {
        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        _keystore = new File(_securityFolder, "keystore");

        JavaKeystoreTest.generateKeyInKeyStore(_keystore, "bob");
        JavaKeystoreTest.generateKeyInKeyStore(_keystore, "alice");
        JavaKeystoreTest.generateKeyInKeyStore(_keystore, "mallory");

    }

    protected void tearDown() {
        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());
    }

    /**
     * @throws IOException
     * @throws SecurityException
     * 
     */
    public void testNewKeyStore() throws Exception {
        JavaKeystore keystore = null;
        keystore = new JavaKeystore(_keystore, "password");
        assertNotNull(keystore);
        String[] aliases = null;
        aliases = keystore.getAliases();
        assertNotNull(aliases);

        assertEquals(3, aliases.length);

        List list = new ArrayList();
        for (int i = 0; i < aliases.length; i++) {
            list.add(aliases[i]);
        }
        aliases = null;
        assertTrue(list.contains("bob"));
        assertTrue(list.contains("alice"));
        assertTrue(list.contains("mallory"));

        X509Certificate certificate = null;
        certificate = keystore.getX509Certificate("alice");
        assertNotNull(certificate);
        PublicKey publicKey = certificate.getPublicKey();
        assertNotNull(publicKey);

        PrivateKey privateKey = keystore.getPrivateKey("bon");
        assertNull(privateKey);

        privateKey = keystore.getPrivateKey("alice");
        assertNotNull(privateKey);

    }

    /**
     * @param keystore
     * @param alias
     * @throws Exception
     */
    public static void generateKeyInKeyStore(File keystore, String alias) throws Exception {
        KeyTool.main(new String[] { "-genkey", "-keystore", keystore.getAbsolutePath(), "-alias", alias, "-keypass",
                "password", "-storepass", "password", "-dname", "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
    }
}
