/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
            files[i].delete();
        }
        _securityFolder.delete();
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
