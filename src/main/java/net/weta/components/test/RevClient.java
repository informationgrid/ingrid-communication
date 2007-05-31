package net.weta.components.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class RevClient {

    public static void main(String[] args) {
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:client");
        tc.setIsCommunicationServer(false);
        tc.addServer("192.168.200.39:55555");
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
