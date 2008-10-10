package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ConfigurationValidatorTest extends TestCase {

	public void testValidateServer() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		validator.validateConfiguration(new File(
				"src/test/resources/validServerConfiguration.xml"));
		assertTrue(true);
	}
	
	public void testValidateClient() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		validator.validateConfiguration(new File(
				"src/test/resources/validClientConfiguration.xml"));
		assertTrue(true);
	}

	public void testInValidateServer() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		try {
			validator.validateConfiguration(new File(
					"src/test/resources/inValidServerConfiguration.xml"));
			fail();
		} catch (IOException e) {
		}
	}

	public void testInValidateClient() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		try {
			validator.validateConfiguration(new File(
					"src/test/resources/inValidClientConfiguration.xml"));
			fail();
		} catch (IOException e) {
		}
	}

}
