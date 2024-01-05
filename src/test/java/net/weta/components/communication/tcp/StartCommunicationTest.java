/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;

import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.Configuration;
import org.junit.jupiter.api.Test;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StartCommunicationTest {

    @Test
    public void testStart() {
        InputStream resourceAsStream = StartCommunication.class.getResourceAsStream("/validClientConfiguration.xml");
        try {
            TcpCommunication communication = (TcpCommunication) StartCommunication.create(resourceAsStream);
            assertEquals("clientName", communication.getPeerName());
            communication.getRegisteredClients();
            Configuration configuration = communication.getConfiguration();
            ClientConfiguration clientConfiguration = (ClientConfiguration) configuration;
            ClientConnection clientConnection = (ClientConnection) clientConfiguration.getClientConnections().get(0);
            assertEquals("127.0.0.1", clientConnection.getServerIp());
            assertEquals(80, clientConnection.getServerPort());
            ClientConnection clientConnection2 = (ClientConnection) clientConfiguration.getClientConnections().get(1);
            assertEquals("127.0.0.2", clientConnection2.getServerIp());
            assertEquals(82, clientConnection2.getServerPort());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
