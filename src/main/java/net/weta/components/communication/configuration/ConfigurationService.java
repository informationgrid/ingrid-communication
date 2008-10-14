package net.weta.components.communication.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;

public class ConfigurationService implements IConfigurationService {

    private final IConfigurationValidator _configurationValidator;
    private final IXPathService _xpathService;

    public ConfigurationService(IConfigurationValidator configurationValidator, IXPathService pathService) throws Exception {
        _configurationValidator = configurationValidator;
        _xpathService = pathService;
    }

    public int getConfigurationType() {
        int configType = IConfigurationService.CLIENT;
        boolean exsistsNode = _xpathService.exsistsNode("/communication/server");
        if (exsistsNode) {
            configType = IConfigurationService.SERVER;
        }
        return configType;
    }

    public void registerConfigurationFile(InputStream streamToConfigurationFile) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = -1;
        while ((read = streamToConfigurationFile.read(buffer, 0, buffer.length)) > -1) {
            byteArrayOutputStream.write(buffer, 0, read);
        }
        streamToConfigurationFile.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        _configurationValidator.validateConfiguration(byteArrayInputStream);

        byteArrayInputStream = new ByteArrayInputStream(byteArray);
        _xpathService.registerDocument(byteArrayInputStream);
    }
    
    public void registerConfigurationFile(File file) throws Exception {
        _configurationValidator.validateConfiguration(file);
        _xpathService.registerDocument(file);
    }
    
    public Configuration parseConfiguration() throws Exception {
        int configurationType = getConfigurationType();
        Configuration communicationConfiguration = null;
        switch (configurationType) {
        case IConfigurationService.CLIENT:
            communicationConfiguration = createClientConfiguration();
            break;
        case IConfigurationService.SERVER:
            communicationConfiguration = createServerConfiguration();
            break;
        default:
            break;
        }
        
        String queueSize = _xpathService.parseAttribute("/communication/messages", "queueSize");
        String handleTimeout = _xpathService.parseAttribute("/communication/messages", "handleTimeout");
        communicationConfiguration.setQueueSize(Integer.parseInt(queueSize));
        communicationConfiguration.setHandleTimeout(Integer.parseInt(handleTimeout));
        
        return communicationConfiguration;
    }

    private Configuration createServerConfiguration() throws Exception {
        ServerConfiguration serverConfiguration = new ServerConfiguration();

        String serverName = _xpathService.parseAttribute("/communication/server", "name");
        serverConfiguration.setName(serverName);

        String port = _xpathService.parseAttribute("/communication/server/socket", "port");
        serverConfiguration.setPort(Integer.parseInt(port));

        String timeoutName = _xpathService.parseAttribute("/communication/server/socket", "timeout");
        serverConfiguration.setSocketTimeout(Integer.parseInt(timeoutName));

        if (_xpathService.exsistsNode("/communication/server/security")) {
            String keyStorePath = _xpathService.parseAttribute("/communication/server/security", "keystore");
            String password = _xpathService.parseAttribute("/communication/server/security", "password");
            serverConfiguration.setKeystorePath(keyStorePath);
            serverConfiguration.setKeystorePassword(password);
        }

        String maxSize = _xpathService.parseAttribute("/communication/server/messages", "maximumSize");
        serverConfiguration.setMaxMessageSize(Integer.parseInt(maxSize));

        String threadCount = _xpathService.parseAttribute("/communication/server/messages", "threadCount");
        serverConfiguration.setMessageThreadCount(Integer.parseInt(threadCount));

        return serverConfiguration;
    }

    private Configuration createClientConfiguration() throws Exception {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        String name = _xpathService.parseAttribute("/communication/client", "name");
        clientConfiguration.setName(name);

        int count = _xpathService.countNodes("/communication/client/connections/server");
        for (int i = 0; i < count; i++) {
            ClientConnection clientConnection = clientConfiguration.new ClientConnection();
            String serverName = _xpathService.parseAttribute("/communication/client/connections/server", "name", i);
            clientConnection.setServerName(serverName);

            String port = _xpathService.parseAttribute("/communication/client/connections/server/socket", "port", i);
            clientConnection.setServerPort(Integer.parseInt(port));

            String timeout = _xpathService.parseAttribute("/communication/client/connections/server/socket", "timeout", i);
            clientConnection.setSocketTimeout(Integer.parseInt(timeout));

            String ip = _xpathService.parseAttribute("/communication/client/connections/server/socket", "ip", i);
            clientConnection.setServerIp(ip);

            if (_xpathService.exsistsNode("/communication/client/connections/server/socket/proxy", i)) {
                String proxyPort = _xpathService.parseAttribute("/communication/client/connections/server/socket/proxy", "port", i);
                clientConnection.setProxyPort(Integer.parseInt(proxyPort));

                String proxyIp = _xpathService.parseAttribute("/communication/client/connections/server/socket/proxy", "ip", i);
                clientConnection.setProxyIp(proxyIp);

                if (_xpathService.exsistsNode("/communication/client/connections/server/socket/proxy/authentication", i)) {
                    String proxyPassword = _xpathService.parseAttribute("/communication/client/connections/server/socket/proxy/authentication", "userPassword", i);
                    String proxyUser = _xpathService.parseAttribute("/communication/client/connections/server/socket/proxy/authentication", "userName", i);
                    clientConnection.setProxyPassword(proxyPassword);
                    clientConnection.setProxyUser(proxyUser);
                }
            }
            
            if (_xpathService.exsistsNode("/communication/client/connections/server/security", i)) {
                String keyStorePath = _xpathService.parseAttribute("/communication/client/connections/server/security", "keystore", i);
                String password = _xpathService.parseAttribute("/communication/client/connections/server/security", "password", i);
                clientConnection.setKeystorePath(keyStorePath);
                clientConnection.setKeystorePassword(password);
            }

            String maxSize = _xpathService.parseAttribute("/communication/client/connections/server/messages", "maximumSize", i);
            clientConnection.setMaxMessageSize(Integer.parseInt(maxSize));

            String threadCount = _xpathService.parseAttribute("/communication/client/connections/server/messages", "threadCount", i);
            clientConnection.setMessageThreadCount(Integer.parseInt(threadCount));

            clientConfiguration.addClientConnection(clientConnection);
        }

        return clientConfiguration;
    }

  

}
