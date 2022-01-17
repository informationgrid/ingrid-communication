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
package net.weta.components.test;

import java.io.IOException;
import java.util.Map;

import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Do get real results turn assertions on.");
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:client-ms");
        
        ClientConfiguration configuration = new ClientConfiguration();
        ClientConnection clientConnection = configuration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
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

        Thread.sleep(1000);
        Map proxyS = (Map) ProxyService.createProxy(tc, Map.class, "/101tec-group:server");
        Thread.sleep(1000);
        long start = System.currentTimeMillis();
        int i = 1;
        while (true) {
            try {
                Caller c2 = new Caller(proxyS);
                Thread thread2 = new Thread(c2);
                Caller c1 = new Caller(proxyS);
                Thread thread1 = new Thread(c1);
                thread2.start();
                thread1.start();
                thread2.join();
                thread1.join();
                // assert ((Map) proxyS.get("key")).containsKey("a");
                // assert ((Map) _proxyS.get("key")).keySet().size() == 1001;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((i % 1000) == 0) {
                System.out.println(i + " proxy calls sent with "
                        + (2000 / ((System.currentTimeMillis() - start) / 1000.0)) + " calls per second");
                start = System.currentTimeMillis();
            }
            i++;
        }
    }
    
    public static class Caller implements Runnable {

        private final Map _proxyS;

        public Caller(Map proxyS) {
            _proxyS = proxyS;
        }
        
        public void run() {
            try {
                assert ((Map) _proxyS.get("key")).keySet().size() == 1001;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
