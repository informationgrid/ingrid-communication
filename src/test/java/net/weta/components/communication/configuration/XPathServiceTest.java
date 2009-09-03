package net.weta.components.communication.configuration;

import java.io.File;

import junit.framework.TestCase;

public class XPathServiceTest extends TestCase {

    private final static String FILE_PATH = "src/test/resources/validClientConfiguration.xml";
    
    private final static String SERVER_NODE_PATH = "/communication/client/connections/server";
    
    private File _xml = null;
    
    private IXPathService _service = null;
    
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
    
	public void testParseAttribute() throws Exception {
		String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
		assertEquals("peerName", name);
		name = _service.parseAttribute(SERVER_NODE_PATH, "name", 1);
		assertEquals("peerName2", name);
	}
	
	public void testSetAttribute() throws Exception {
	    _service.setAttribute(SERVER_NODE_PATH, "name", "changed");
	    String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
	    assertEquals("changed", name);
	    
	    _service.setAttribute(SERVER_NODE_PATH, "name", "another", 1);
	    name = _service.parseAttribute(SERVER_NODE_PATH, "name", 1);
	    assertEquals("another", name);
	}
	
	public void testExistsAttribute() {
	    assertTrue(_service.existsAttribute(SERVER_NODE_PATH, "name"));
        assertTrue(_service.existsAttribute(SERVER_NODE_PATH, "name", 1));
        assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "invalid"));
        assertFalse(_service.existsAttribute(SERVER_NODE_PATH, "invalid", 1));
	}
	
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
	
    public void testAddAttribute() throws Exception {
        _service.addAttribute(SERVER_NODE_PATH, "test", "true");
        assertEquals("true", _service.parseAttribute(SERVER_NODE_PATH, "test"));
        
        _service.addAttribute(SERVER_NODE_PATH, "test", "false", 1);
        assertEquals("true", _service.parseAttribute(SERVER_NODE_PATH, "test"));
        assertEquals("false", _service.parseAttribute(SERVER_NODE_PATH, "test", 1));
    }
	
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
	
	public void testCountNodes() throws Exception {
	    int count = _service.countNodes(SERVER_NODE_PATH);
	    assertEquals(2, count);
	}
	
	public void testAddNode() throws Exception {
	    _service.addNode("/communication/client/connections", "server");
	    int count = _service.countNodes(SERVER_NODE_PATH);
        assertEquals(3, count);
        
        _service.addNode(SERVER_NODE_PATH, "test", 2);
        count = _service.countNodes(SERVER_NODE_PATH + "/test");
        assertEquals(1, count);
	}
	
	public void testRemoveFirstNode() throws Exception {
	    _service.removeNode(SERVER_NODE_PATH);
	    String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
	    assertEquals("peerName2", name);
	}
	
	public void testRemoveSecondNode() throws Exception {
        _service.removeNode(SERVER_NODE_PATH, 1);
        String name = _service.parseAttribute(SERVER_NODE_PATH, "name");
        assertEquals("peerName", name);
    }
	
	public void testRemoveAllNodes() throws Exception {
        _service.removeNodes(SERVER_NODE_PATH);
        boolean exists = _service.exsistsNode(SERVER_NODE_PATH);
        assertFalse(exists);
    }
	
	public void testStore() throws Exception {
	    File file = new File("/tmp/temp.xml");
	    _service.setAttribute(SERVER_NODE_PATH, "name", "changed", 1);
	    _service.store(file);
	    
        IXPathService path = new XPathService();
        path.registerDocument(file);
        
        assertEquals(_service.parseAttribute(SERVER_NODE_PATH, "name"), path.parseAttribute(SERVER_NODE_PATH, "name"));
        assertEquals("changed", path.parseAttribute(SERVER_NODE_PATH, "name", 1));
	}
}
