package net.weta.components.communication.configuration;

import java.io.File;

import junit.framework.TestCase;

public class XPathServiceTest extends TestCase {

	public void testParseAttribute() throws Exception {
		File xmlFile = new File(
				"src/test/resources/validServerConfiguration.xml");
		IXPathService service = new XPathService();
		service.registerDocument(xmlFile);
		String name = service.parseAttribute("/communication/server", "name");
		assertEquals(name, "abc");
	}
}
