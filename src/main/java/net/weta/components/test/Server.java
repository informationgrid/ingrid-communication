package net.weta.components.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class Server {

    public static void main(String[] args) {
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:server");
        tc.setIsCommunicationServer(true);
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

        Map hm = new HashMap();
        hm.put("bla", "blavalue");
        hm.put("ms", "msvalue");
        hm.put("mb", "mb is a cool guy");

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
