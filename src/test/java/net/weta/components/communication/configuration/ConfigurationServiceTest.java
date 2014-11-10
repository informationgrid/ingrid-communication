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
package net.weta.components.communication.configuration;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;

public class ConfigurationServiceTest extends TestCase {

	public void testCreateServer() throws Exception {
		final IXPathService xpathService = new XPathService();
		final IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		final IConfigurationService service = new ConfigurationService(
				validator, xpathService);
		service.registerConfigurationFile(new File(
				"src/test/resources/validServerConfiguration.xml"));
		assertEquals(IConfigurationService.SERVER, service
				.getConfigurationType());
		Configuration communicationConfiguration = service.parseConfiguration();
		assertTrue(communicationConfiguration instanceof ServerConfiguration);
		ServerConfiguration serverConfiguration = (ServerConfiguration) communicationConfiguration;
		assertEquals("abc", serverConfiguration.getName());
		assertEquals("password", serverConfiguration.getKeystorePassword());
		assertEquals("/tmp/keystore.jks", serverConfiguration.getKeystorePath());
		assertEquals(1048576, serverConfiguration.getMaxMessageSize());
		assertEquals(100, serverConfiguration.getMessageThreadCount());
		assertEquals(80, serverConfiguration.getPort());
		assertEquals(10, serverConfiguration.getSocketTimeout());
	}

	public void testCreateClient() throws Exception {
		final IXPathService xpathService = new XPathService();
		final IConfigurationValidator validator = new ConfigurationValidator(
				new File("src/main/resources/communication.xsd"));
		final IConfigurationService service = new ConfigurationService(
				validator, xpathService);
		service.registerConfigurationFile(new File(
				"src/test/resources/validClientConfiguration.xml"));
		assertEquals(IConfigurationService.CLIENT, service
				.getConfigurationType());
		Configuration communicationConfiguration = service.parseConfiguration();
		assertTrue(communicationConfiguration instanceof ClientConfiguration);
		ClientConfiguration clientConfiguration = (ClientConfiguration) communicationConfiguration;
		assertEquals("clientName", clientConfiguration.getName());

		List clientConnections = clientConfiguration.getClientConnections();
		assertEquals(2, clientConnections.size());
		ClientConnection clientConnection1 = (ClientConnection) clientConnections
				.get(0);
		assertEquals("peerName", clientConnection1.getServerName());
		assertEquals("127.0.0.1", clientConnection1.getServerIp());
		assertEquals(80, clientConnection1.getServerPort());
		assertEquals(10, clientConnection1.getSocketTimeout());

		assertEquals("127.0.0.1", clientConnection1.getProxyIp());
		assertEquals(81, clientConnection1.getProxyPort());
		assertEquals("password", clientConnection1.getProxyPassword());
		assertEquals("userName", clientConnection1.getProxyUser());

		assertEquals("password", clientConnection1.getKeystorePassword());
		assertEquals("/tmp/keystore.jks", clientConnection1.getKeystorePath());
		assertEquals(1048576, clientConnection1.getMaxMessageSize());
		assertEquals(100, clientConnection1.getMessageThreadCount());

		ClientConnection clientConnection2 = (ClientConnection) clientConnections
				.get(1);
		assertEquals("peerName2", clientConnection2.getServerName());
		assertEquals("127.0.0.2", clientConnection2.getServerIp());
		assertEquals(82, clientConnection2.getServerPort());
		assertEquals(10, clientConnection2.getSocketTimeout());

		assertEquals(null, clientConnection2.getProxyIp());
		assertEquals(0, clientConnection2.getProxyPort());
		assertEquals(null, clientConnection2.getProxyPassword());
		assertEquals(null, clientConnection2.getProxyUser());

		assertEquals(null, clientConnection2.getKeystorePassword());
		assertEquals(null, clientConnection2.getKeystorePath());
		assertEquals(1048576, clientConnection2.getMaxMessageSize());
		assertEquals(100, clientConnection2.getMessageThreadCount());

	}

}
