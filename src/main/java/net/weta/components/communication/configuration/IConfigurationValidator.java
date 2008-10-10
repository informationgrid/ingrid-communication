package net.weta.components.communication.configuration;

import java.io.File;
import java.io.IOException;

public interface IConfigurationValidator {

	void validateConfiguration(File configurationFile) throws IOException;
}
