package net.weta.components.communication.reflect;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;

public class ProxyServiceTest extends TestCase {

    public void testCreateProxyICommunicationClassString() {
        ICommunication communication = new TcpCommunication();
        Class theInterface = IMessageHandler.class;
        String proxyServerUrl = "url";
        ProxyService.createProxy(communication, theInterface, proxyServerUrl);
    }

    public void testCreateProxyServer() {
        ICommunication communication = new TcpCommunication();
        Class theInterface = IMessageHandler.class;
        String string = "string";
        ProxyService.createProxyServer(communication, theInterface, string);
    }

}
