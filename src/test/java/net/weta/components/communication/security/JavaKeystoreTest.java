/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * To test the keystore.
 * 
 * created on 06.06.2005
 * <p>
 */
public class JavaKeystoreTest extends TestCase {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

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
            files[i].delete();
        }
        _securityFolder.delete();
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

        List<String> list = new ArrayList<String>();
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
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        if (keystore.exists()) {
            ks.load(new FileInputStream(keystore), password);
        } else {
            ks.load(null, password);
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();

        // Generate self-signed certificate
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.OU, "OU");
        builder.addRDN(BCStyle.O, "O");
        builder.addRDN(BCStyle.CN, "cn");

        Date notBefore = new Date(System.currentTimeMillis() - 360000 * 24);
        Date notAfter = new Date(System.currentTimeMillis() + 10 * 360000 * 365);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(builder.build(), serial, notBefore, notAfter, builder.build(), pair.getPublic());
        ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC).build(pair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));

        PrivateKeyEntry entry = new PrivateKeyEntry(pair.getPrivate(), new java.security.cert.Certificate[] { cert });
        ks.setEntry(alias, entry, new KeyStore.PasswordProtection("password".toCharArray()));

        // Store away the keystore.
        FileOutputStream fos = new FileOutputStream(keystore.getAbsolutePath());
        ks.store(fos, password);
        fos.close();
    }

    public static void exportCertficate(File keystore, String alias, File cert) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        ks.load(new FileInputStream(keystore), password);
        java.security.cert.Certificate c = ks.getCertificate(alias);

        JcaPEMWriter writer = new JcaPEMWriter(new FileWriter(cert));
        writer.writeObject(c);
        writer.close();
    }

    public static void importCertficate(File keystore, String alias, File cert) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        ks.load(new FileInputStream(keystore), password);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate c = cf.generateCertificate(new FileInputStream(cert));
        ks.setCertificateEntry(alias, c);

        // Store away the keystore.
        FileOutputStream fos = new FileOutputStream(keystore.getAbsolutePath());
        ks.store(fos, password);
        fos.close();
    }

}
