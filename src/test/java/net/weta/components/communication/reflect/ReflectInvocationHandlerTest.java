package net.weta.components.communication.reflect;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import net.weta.components.communication.tcp.TcpCommunication;
import net.weta.components.communication.tcp.TimeoutException;

public class ReflectInvocationHandlerTest extends TestCase {

    public interface ITest {
        String testMethod();
    }

    private TcpCommunication _tcpCommunicationClient;

    private TcpCommunication _tcpCommunicationServer;

    protected void setUp() throws Exception {
        _tcpCommunicationServer = new TcpCommunication();
        _tcpCommunicationServer.setIsCommunicationServer(true);
        _tcpCommunicationServer.addServer("127.0.0.1:55556");
        _tcpCommunicationServer.setPeerName("b");
        _tcpCommunicationServer.startup();

        _tcpCommunicationClient = new TcpCommunication();
        _tcpCommunicationClient.setIsCommunicationServer(false);
        _tcpCommunicationClient.setPeerName("a");
        _tcpCommunicationClient.addServer("127.0.0.1:55556");
        _tcpCommunicationClient.addServerName("b");
        _tcpCommunicationClient.setMessageHandleTimeout(2);
        _tcpCommunicationClient.startup();
    }

    protected void tearDown() throws Exception {
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.shutdown();
    }

    public void testInvoke() throws Exception {

        ReflectInvocationHandler handler = new ReflectInvocationHandler(_tcpCommunicationClient, "/kug-group:a");
        ITest test = (ITest) Proxy.newProxyInstance(ITest.class.getClassLoader(), new Class[] { ITest.class }, handler);
        try {
            test.testMethod();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            TimeoutException exception = (TimeoutException) cause;
            assertNotNull(exception);
        }
    }
}
