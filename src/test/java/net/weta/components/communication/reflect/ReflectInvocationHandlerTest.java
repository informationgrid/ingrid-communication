package net.weta.components.communication.reflect;

import java.io.File;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.tcp.TcpCommunication;
import net.weta.components.communication.tcp.TimeoutException;
import sun.security.tools.KeyTool;

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
        
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(55556);
        serverConfiguration.setKeystorePath(keystoreServer.getAbsolutePath());
        serverConfiguration.setKeystorePassword("password");
        serverConfiguration.setName(SERVER);
        
        _tcpCommunicationServer.setConfiguration(serverConfiguration);
        _tcpCommunicationServer.startup();

        _tcpCommunicationClient = new TcpCommunication();
        
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setName(CLIENT);
        ClientConnection clientConnection = clientConfiguration.new ClientConnection();
        clientConnection.setServerIp("127.0.0.1");
        clientConnection.setServerPort(55556);
        clientConnection.setServerName(SERVER);
        clientConnection.setKeystorePath(keystoreClient.getAbsolutePath());
        clientConnection.setKeystorePassword("password");
        clientConfiguration.setHandleTimeout(2);
        clientConfiguration.addClientConnection(clientConnection);
        _tcpCommunicationClient.setConfiguration(clientConfiguration);
        
        
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
