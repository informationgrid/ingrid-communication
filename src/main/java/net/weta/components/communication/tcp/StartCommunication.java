/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartCommunication {

    private static final Logger LOG = LogManager.getLogger(StartCommunication.class);

    public static ICommunication create(InputStream inputStream) throws IOException {
        TcpCommunication communication = new TcpCommunication();
        configureFromXmlFile(inputStream, communication);
        return communication;
    }

    public static ICommunication create(Configuration configuration) throws IOException {
        TcpCommunication communication = new TcpCommunication();
        communication.setPeerName(configuration.getName());
        communication.configure(configuration);
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
            communication.configure(configuration);
        } catch (Exception e) {
            LOG.error("can not create communication", e);
            throw new IOException(e.getMessage());
        }
        return communication;
    }
    
}
