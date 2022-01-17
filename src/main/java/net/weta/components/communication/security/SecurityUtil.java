/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;


public class SecurityUtil {

    private static final Logger LOG = LogManager.getLogger(SecurityUtil.class);

    public static final String SHA256_RSA = "SHA256WithRSAEncryption";

    private final JavaKeystore _javaKeystore;

    public SecurityUtil(JavaKeystore javaKeystore) {
        _javaKeystore = javaKeystore;
    }

    public byte[] computeSignature(String alias, byte[] bytesToSign) throws SecurityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("try to load private with alias: [" + alias + "]");
        }
        PrivateKey privateKey = _javaKeystore.getPrivateKey(alias);
        if (privateKey == null) {
            throw new SecurityException("private key for alias [" + alias + "] not found.", null);
        }
        return computeSignature(privateKey, bytesToSign);
    }

    public boolean verifySignature(String alias, byte[] data, byte[] signature) throws SecurityException {
        X509Certificate certificate = _javaKeystore.getX509Certificate(alias);
        return verifySignature(certificate, data, signature);
    }

    private byte[] computeSignature(PrivateKey key, byte[] bytesToSign) throws SecurityException {
        byte[] ret = null;
        try {
            Signature signature = Signature.getInstance(SHA256_RSA);
            signature.initSign(key);
            signature.update(bytesToSign, 0, bytesToSign.length);
            ret = signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("no such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("invalid key", e);
        } catch (SignatureException e) {
            throw new SecurityException("signature fails", e);
        }
        return ret;
    }

    private boolean verifySignature(X509Certificate certificate, byte[] data, byte[] signature)
            throws SecurityException {
        PublicKey publicKey = certificate.getPublicKey();
        boolean ret = false;
        try {
            Signature sig = Signature.getInstance(SHA256_RSA);
            sig.initVerify(publicKey);
            sig.update(data);
            ret = sig.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("no such algorithm", e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("invalid key", e);
        } catch (SignatureException e) {
            throw new SecurityException("signatur fails", e);
        }
        return ret;
    }
}
