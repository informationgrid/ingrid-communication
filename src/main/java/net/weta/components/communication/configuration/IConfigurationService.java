package net.weta.components.communication.configuration;

import java.io.File;

public interface IConfigurationService {

	public int SERVER = 0;

	public int CLIENT = 1;
	
	void registerConfigurationFile(File file) throws Exception;
	
	int getConfigurationType();
	
	Configuration parseConfiguration() throws Exception;
}
