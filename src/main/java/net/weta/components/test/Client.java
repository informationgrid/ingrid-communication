package net.weta.components.test;

import java.io.IOException;
import java.util.Map;

import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.TcpCommunication;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Do get real results turn assertions on.");
        TcpCommunication tc = new TcpCommunication();
        tc.setPeerName("/101tec-group:client-ms");
        tc.setIsCommunicationServer(false);
        //tc.addServer("192.168.200.52:55555");
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
