/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


public class JavaKeystore {

    private static final String PROVIDER = "JKS";

    private KeyStore fKeyStore;

    private String fPassw;

    private static final Logger LOG = LogManager.getLogger(JavaKeystore.class);
    
    /**
     * Load an keystore by the given parameter.
     * 
     * @param file
     * @param passw
     * @throws SecurityException
     * @throws IOException
     */
    public JavaKeystore(File file, String passw) throws SecurityException, IOException {
        try {
            this.fKeyStore = KeyStore.getInstance(PROVIDER);
            this.fPassw = passw;
            this.fKeyStore.load(new FileInputStream(file), passw.toCharArray());
        } catch (KeyStoreException e) {
            throw new SecurityException("Can not create a keystore.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Can not load the keystore, because the algorithm is not available.", e);
        } catch (CertificateException e) {
            throw new SecurityException("Can not load the keystore, because it exist a certificate problem.", e);
        }
    }

    /**
     * Returns all aliases from a keystore.
     * 
     * @return A string array.
     * @throws SecurityException
     */
    public String[] getAliases() throws SecurityException {
        List list = new LinkedList();

        Enumeration enumeration = null;
        try {
            enumeration = this.fKeyStore.aliases();
        } catch (KeyStoreException e) {
            throw new SecurityException("Can not extract the aliase from the keystore.", e);
        }
        while (enumeration.hasMoreElements()) {
            String element = (String) enumeration.nextElement();
            list.add(element);
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Returns an certificate to the given alias from the keystore.
     * 
     * @param alias
     * @return A X509 certificate.
     * @throws SecurityException
     */
    public X509Certificate getX509Certificate(String alias) throws SecurityException {
        try {
            return (X509Certificate) this.fKeyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new SecurityException(
                    "Can not extract the certificate from the keystore, because the keystore is  not loaded.", e);
        }

    }

    /**
     * Returns the private key to the given alias from the keystore.
     * 
     * @param alias
     * @return A private key.
     * @throws SecurityException
     */
    public PrivateKey getPrivateKey(String alias) throws SecurityException {
        Key key = null;
        try {
            LOG.debug("try to load key from keystore with alias: " + alias);
            key = this.fKeyStore.getKey(alias, this.fPassw.toCharArray());
        } catch (KeyStoreException e) {
            throw new SecurityException("can not read the keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("no such algorithm", e);
        } catch (UnrecoverableKeyException e) {
            throw new SecurityException("key problem", e);
        }
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        return null;
    }
}
