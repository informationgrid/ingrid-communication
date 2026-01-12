/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


public class ConfigurationValidator implements IConfigurationValidator {

	private Validator _validator;

	public ConfigurationValidator(File xsdFile) throws Exception {
		this(xsdFile.toURI().toURL());
	}

	public ConfigurationValidator(URL xsdFileUrl) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(xsdFileUrl);
        _validator = schema.newValidator();
    }

    public void validateConfiguration(File configurationFile)
			throws IOException {
		Source source = new StreamSource(configurationFile);
		validateSource(source);
	}

    public void validateConfiguration(InputStream configurationFile) throws IOException {
        Source source = new StreamSource(configurationFile);
        validateSource(source);
    }

    private void validateSource(Source source) throws IOException {
        try {
            _validator.validate(source);
        } catch (Exception e) {
            throw new IOException("configuration file is not valid: " + e.getMessage());
        }
    }
}
