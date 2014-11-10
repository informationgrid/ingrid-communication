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
package net.weta.components.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class RevClient {

    public static void main(String[] args) {
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:client");

        ClientConfiguration configuration = new ClientConfiguration();
        ClientConnection clientConnection = configuration.new ClientConnection();
        clientConnection.setServerIp("192.168.200.39");
        clientConnection.setServerPort(55555);
        clientConnection.setServerName("101tec-group:server");
        configuration.addClientConnection(clientConnection);
        tc.configure(configuration);
        

        try {
            tc.startup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            tc.subscribeGroup("/101tec-group");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map hm = new HashMap();
        hm.put("bla", "blavalue");
        ProxyService.createProxyServer(tc, Map.class, hm);

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
