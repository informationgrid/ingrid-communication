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
package net.weta.components.communication.reflect;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import org.junit.jupiter.api.Test;

public class ProxyServiceTest {

    @Test
    public void testCreateProxyICommunicationClassString() {
        ICommunication communication = new TcpCommunication();
        Class theInterface = IMessageHandler.class;
        String proxyServerUrl = "url";
        ProxyService.createProxy(communication, theInterface, proxyServerUrl);
    }

    @Test
    public void testCreateProxyServer() {
        ICommunication communication = new TcpCommunication();
        Class theInterface = IMessageHandler.class;
        String string = "string";
        ProxyService.createProxyServer(communication, theInterface, string);
    }

}
