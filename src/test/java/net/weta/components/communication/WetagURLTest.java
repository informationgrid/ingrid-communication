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
