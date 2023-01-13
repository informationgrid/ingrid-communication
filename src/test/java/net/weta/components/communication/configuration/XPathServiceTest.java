/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package net.weta.components.communication.configuration;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XPathServiceTest {

    private final static String FILE_PATH = "src/test/resources/validClientConfiguration.xml";
    
    private final static String SERVER_NODE_PATH = "/communication/client/connections/server";
    
    private File _xml = null;
    
    private IXPathService _service = null;

    @BeforeEach
    public void setUp() {
        _xml = new File(FILE_PATH);
        try {
            _service = new XPathService();
            _service.registerDocument(_xml);
        } catch (Exception e) {
            System.out.println("XPathServiceTest.setUp() creating XPathService failed");
            e.printStackTrace();
        }
    }

    @Test
    public void testParseAttribute() throws Exception {
		String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
		assertEquals("peerName", name);
		name = _service.parseAttribute(SERVER_NODE_PATH, "name", 1);
		assertEquals("peerName2", name);
	}

    @Test
    public void testSetAttribute() throws Exception {
	    _service.setAttribute(SERVER_NODE_PATH, "name", "changed");
	    String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
	    assertEquals("changed", name);
	    
	    _service.setAttribute(SERVER_NODE_PATH, "name", "another", 1);
	    name = _service.parseAttribute(SERVER_NODE_PATH, "name", 1);
	    assertEquals("another", name);
	}

    @Test
    public void testExistsAttribute() {
	    assertTrue(_service.existsAttribute(SERVER_NODE_PATH, "name"));
        assertTrue(_service.existsAttribute(SERVER_NODE_PATH, "name", 1));
        assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "invalid"));
        assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "invalid", 1));
	}

    @Test
    public void testRemoveAttribute() throws Exception {
	    _service.removeAttribute(SERVER_NODE_PATH, "name");
	    assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "name"));
	    assertTrue(_service.existsAttribute(SERVER_NODE_PATH, "name", 1));
	    
	    _service.removeAttribute(SERVER_NODE_PATH, "name", 1);
	    assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "name", 1));
	    
	    _service.removeAttributes(SERVER_NODE_PATH + "/socket", "port");
	    assertFalse(_service.existsAttribute(SERVER_NODE_PATH + "/socket", "port"));
	    assertFalse(_service.existsAttribute(SERVER_NODE_PATH + "/socket", "port", 1));
	}

    @Test
    public void testAddAttribute() throws Exception {
        _service.addAttribute(SERVER_NODE_PATH, "test", "true");
        assertEquals("true", _service.parseAttribute(SERVER_NODE_PATH, "test"));
        
        _service.addAttribute(SERVER_NODE_PATH, "test", "false", 1);
        assertEquals("true", _service.parseAttribute(SERVER_NODE_PATH, "test"));
        assertEquals("false", _service.parseAttribute(SERVER_NODE_PATH, "test", 1));
    }

    @Test
    public void testExistsNode() {
	    boolean exists = _service.exsistsNode(SERVER_NODE_PATH);
	    assertTrue(exists);
	    exists = _service.exsistsNode(SERVER_NODE_PATH, 1);
	    assertTrue(exists);
	    exists = _service.exsistsNode(SERVER_NODE_PATH + "/invalid");
	    assertFalse(exists);
	    exists = _service.exsistsNode(SERVER_NODE_PATH + "/invalid", 1);
        assertFalse(exists);
	}

    @Test
    public void testCountNodes() throws Exception {
	    int count = _service.countNodes(SERVER_NODE_PATH);
	    assertEquals(2, count);
	}

    @Test
    public void testAddNode() throws Exception {
	    _service.addNode("/communication/client/connections", "server");
	    int count = _service.countNodes(SERVER_NODE_PATH);
        assertEquals(3, count);
        
        _service.addNode(SERVER_NODE_PATH, "test", 2);
        count = _service.countNodes(SERVER_NODE_PATH + "/test");
        assertEquals(1, count);
	}

    @Test
    public void testRemoveFirstNode() throws Exception {
	    _service.removeNode(SERVER_NODE_PATH);
	    String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
	    assertEquals("peerName2", name);
	}

    @Test
    public void testRemoveSecondNode() throws Exception {
        _service.removeNode(SERVER_NODE_PATH, 1);
        String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
        assertEquals("peerName", name);
    }

    @Test
    public void testRemoveAllNodes() throws Exception {
        _service.removeNodes(SERVER_NODE_PATH);
        boolean exists = _service.exsistsNode(SERVER_NODE_PATH);
        assertFalse(exists);
    }

    @Test
    public void testStore() throws Exception {
	    File file = new File(System.getProperty("java.io.tmpdir"), "temp.xml"); 
	    _service.setAttribute(SERVER_NODE_PATH, "name", "changed", 1);
	    _service.store(file);
	    
        IXPathService path = new XPathService();
        path.registerDocument(file);
        
        assertEquals(_service.parseAttribute(SERVER_NODE_PATH, "name"), path.parseAttribute(SERVER_NODE_PATH, "name"));
        assertEquals("changed", path.parseAttribute(SERVER_NODE_PATH, "name", 1));
        file.delete();
	}
}
