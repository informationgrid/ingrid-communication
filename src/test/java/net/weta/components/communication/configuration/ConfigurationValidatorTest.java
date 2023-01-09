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
package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConfigurationValidatorTest {

    @Test
    public void testValidateServer() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		validator.validateConfiguration(new File(
				"src/test/resources/validServerConfiguration.xml"));
		assertTrue(true);
	}

    @Test
    public void testValidateClient() throws Exception {
		IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		validator.validateConfiguration(new File(
				"src/test/resources/validClientConfiguration.xml"));
		assertTrue(true);
	}

    @Test
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

    @Test
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
