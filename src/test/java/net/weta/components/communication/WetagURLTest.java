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
package net.weta.components.communication;

import junit.framework.TestCase;

public class WetagURLTest extends TestCase {

    public void testUrl() throws Exception {
        WetagURL wetagURL = new WetagURL("/101tec-group:client");
        assertEquals("wetag:///101tec-group:client", wetagURL.getURL());
        assertEquals("/101tec-group", wetagURL.getGroupPath());
        assertEquals("client", wetagURL.getPeerName());
        assertEquals("/101tec-group:client", wetagURL.getPath());
        assertEquals("wetag:///101tec-group:client", wetagURL.toString());
    }

    public void testNullPointerException() {
        try {
            new WetagURL(null);
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    public void testIllegalArgumentException() {
        try {
            new WetagURL("wetag_abc");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            new WetagURL("wetag://");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testURLPATH() {
        try {
            WetagURL wetagURL = new WetagURL("wetag:///group");
            assertEquals("/group", wetagURL.getGroupPath());
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    public void testGetUrl() {
        WetagURL wetagURL = new WetagURL("/101tec-group:client");
        String url = wetagURL.getURL();
    }

    public void testCreate() throws Exception {
        WetagURL wetagURL = WetagURL.createUrl("/101tec-group", "client");
        assertEquals(new WetagURL("/101tec-group:client").toString(), wetagURL.toString());
        assertTrue(wetagURL.hasPeerName());
    }

    public void testFailure() throws Exception {
        try {
            new WetagURL("101tec-group");
            fail();
        } catch (IllegalArgumentException e) {
            // nothing todo
        }

        try {
            new WetagURL(null);
        } catch (NullPointerException e) {
            // nothing todo
        }

    }
}
