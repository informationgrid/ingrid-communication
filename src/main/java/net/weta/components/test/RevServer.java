/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
import java.util.Map;

import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class RevServer {

    public static void main(String[] args) {
        System.out.println("Do get real results turn assertions on.");
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:server");
        
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(55555);
        tc.configure(serverConfiguration);

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

        System.out.println("You have 10 seconds to start the clients.");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map proxyS = (Map) ProxyService.createProxy(tc, Map.class, "/101tec-group:client");

        final long start = System.currentTimeMillis();
        int i = 1;
        while (true) {
            try {
                assert proxyS.get("bla").equals("blavalue");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((i % 1000) == 0) {
                System.out.println(i + " proxy calls sent with "
                        + (i / ((System.currentTimeMillis() - start) / 1000.0)) + " calls per second");
            }
            i++;
        }
    }
}
