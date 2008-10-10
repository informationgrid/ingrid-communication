package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


public class ConfigurationValidator implements IConfigurationValidator {

	private Validator _validator;

	public ConfigurationValidator(File xsdFile) throws Exception {
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(xsdFile);
		_validator = schema.newValidator();
	}

	public void validateConfiguration(File configurationFile)
			throws IOException {
		Source source = new StreamSource(configurationFile);
		try {
			_validator.validate(source);
		} catch (Exception e) {
			throw new IOException("configuration file is ot valid: "
					+ e.getMessage());
		}
	}
}
