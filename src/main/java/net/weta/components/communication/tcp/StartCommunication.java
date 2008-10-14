package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.configuration.Configuration;
import net.weta.components.communication.configuration.ConfigurationService;
import net.weta.components.communication.configuration.ConfigurationValidator;
import net.weta.components.communication.configuration.IConfigurationService;
import net.weta.components.communication.configuration.IConfigurationValidator;
import net.weta.components.communication.configuration.IXPathService;
import net.weta.components.communication.configuration.XPathService;

import org.apache.log4j.Logger;

public class StartCommunication {

    private static final Logger LOG = Logger.getLogger(StartCommunication.class);

    public static ICommunication create(InputStream inputStream) throws IOException {
        TcpCommunication communication = new TcpCommunication();
        configureFromXmlFile(inputStream, communication);
        return communication;
    }

    private static TcpCommunication configureFromXmlFile(InputStream inputStream, TcpCommunication communication) throws IOException {
        try {
            URL xsdFileUrl = StartCommunication.class.getResource("/communication.xsd");
            IConfigurationValidator configurationValidator = new ConfigurationValidator(xsdFileUrl);
            IXPathService xpathService = new XPathService();
            IConfigurationService configurationService = new ConfigurationService(configurationValidator, xpathService);
            configurationService.registerConfigurationFile(inputStream);
            Configuration configuration = configurationService.parseConfiguration();
            communication.setPeerName(configuration.getName());
            communication.setConfiguration(configuration);
        } catch (Exception e) {
            LOG.error("can not create communication", e);
            throw new IOException(e.getMessage());
        }
        return communication;
    }
    
}
