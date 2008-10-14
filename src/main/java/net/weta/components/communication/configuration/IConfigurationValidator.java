package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IConfigurationValidator {

	void validateConfiguration(File configurationFile) throws IOException;
	
	void validateConfiguration(InputStream configurationFile) throws IOException;
}
