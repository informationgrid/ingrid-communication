package net.weta.components.communication.configuration;

import java.io.File;
import java.io.InputStream;

public interface IConfigurationService {

	public int SERVER = 0;

	public int CLIENT = 1;
	
	void registerConfigurationFile(File file) throws Exception;
	
	void registerConfigurationFile(InputStream streamToConfigurationFile) throws Exception;
	
	int getConfigurationType();
	
	Configuration parseConfiguration() throws Exception;
}
