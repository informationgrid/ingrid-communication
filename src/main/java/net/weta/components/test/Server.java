package net.weta.components.test;

import java.io.IOException;
import java.util.Map;

import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class Server {

    public static void main(String[] args) {
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:server");
        tc.setIsCommunicationServer(true);
        tc.addServer("127.0.0.1:55555");
        tc.addServerName("/101tec-group:server");
        tc.setUseProxy(false);
        tc.setIsSecure(false);

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

        DummyExternalizable externalizable = new DummyExternalizable();
        DummyExternalizable externalizable2 = new DummyExternalizable();
        externalizable2.put("a", "b");
        for (int i = 0; i < 1000; i++) {
            externalizable2.put(new Integer(i), ""+System.currentTimeMillis()+"dfdsgfdhfghfgjgf");
        }
        externalizable.put("key", externalizable2);

        ProxyService.createProxyServer(tc, Map.class, externalizable);
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
