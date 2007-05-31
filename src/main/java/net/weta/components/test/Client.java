package net.weta.components.test;

import java.io.IOException;
import java.util.Map;

import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class Client {

    public static void main(String[] args) {
        System.out.println("Do get real results turn assertions on.");
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:client");
        tc.setIsCommunicationServer(false);
        tc.addServer("localhost:55555");
        tc.setUseProxy(false);

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

        Map proxyS = (Map) ProxyService.createProxy(tc, Map.class, "/101tec-group:server");

        final long start = System.currentTimeMillis();
        int i = 1;
        while (true) {
            try {
                assert proxyS.get("ms").equals("msvalue");
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
