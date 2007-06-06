package net.weta.components.communication.reflect;

import java.io.File;
import java.lang.reflect.Proxy;

import sun.security.tools.KeyTool;

import junit.framework.TestCase;
import net.weta.components.communication.tcp.TcpCommunication;
import net.weta.components.communication.tcp.TimeoutException;

public class ReflectInvocationHandlerTest extends TestCase {

    public interface ITest {
        String testMethod();
    }

    private TcpCommunication _tcpCommunicationClient;

    private TcpCommunication _tcpCommunicationServer;

    private File _securityFolder;

    private static final String CLIENT = "/kug-group:client";

    private static final String SERVER = "/kug-group:server";

    protected void setUp() throws Exception {
        _securityFolder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());
        _securityFolder.mkdirs();
        final File keystoreServer = new File(_securityFolder, "keystore-server");
        final File keystoreClient = new File(_securityFolder, "keystore-client");
        File clientCertificate = new File(_securityFolder, "client.cer");

        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreServer.getAbsolutePath(), "-alias", SERVER,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-genkey", "-keystore", keystoreClient.getAbsolutePath(), "-alias", CLIENT,
                "-keyalg", "DSA", "-sigalg", "SHA1withDSA", "-keypass", "password", "-storepass", "password", "-dname",
                "CN=hmmm, OU=hmmm, O=hmmm, L=hmmm, ST=hmmm, C=hmmm" });
        KeyTool.main(new String[] { "-export", "-keystore", keystoreClient.getAbsolutePath(), "-storepass", "password",
                "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });
        KeyTool.main(new String[] { "-import", "-keystore", keystoreServer.getAbsolutePath(), "-noprompt",
                "-storepass", "password", "-alias", CLIENT, "-file", clientCertificate.getAbsolutePath() });

        _tcpCommunicationServer = new TcpCommunication();
        _tcpCommunicationServer.setIsCommunicationServer(true);
        _tcpCommunicationServer.addServer("127.0.0.1:55556");
        _tcpCommunicationServer.setPeerName(SERVER);
        _tcpCommunicationServer.setKeystore(keystoreServer.getAbsolutePath());
        _tcpCommunicationServer.setKeystorePassword("password");
        _tcpCommunicationServer.startup();

        _tcpCommunicationClient = new TcpCommunication();
        _tcpCommunicationClient.setIsCommunicationServer(false);
        _tcpCommunicationClient.setPeerName(CLIENT);
        _tcpCommunicationClient.addServer("127.0.0.1:55556");
        _tcpCommunicationClient.addServerName(SERVER);
        _tcpCommunicationClient.setKeystore(keystoreClient.getAbsolutePath());
        _tcpCommunicationClient.setKeystorePassword("password");
        _tcpCommunicationClient.setMessageHandleTimeout(2);
        _tcpCommunicationClient.startup();

    }

    protected void tearDown() throws Exception {
        _tcpCommunicationClient.shutdown();
        _tcpCommunicationServer.shutdown();

        File[] files = _securityFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            assertTrue(files[i].delete());
        }
        assertTrue(_securityFolder.delete());

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
