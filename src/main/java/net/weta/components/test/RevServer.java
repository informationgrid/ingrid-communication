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
        tc.setConfiguration(serverConfiguration);

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
