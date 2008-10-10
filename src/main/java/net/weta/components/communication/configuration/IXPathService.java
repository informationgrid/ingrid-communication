package net.weta.components.communication.configuration;

import java.io.File;

public interface IXPathService {

	void registerDocument(File xmlFile) throws Exception;

	String parseAttribute(String nodePath, String attributeName)
			throws Exception;

	String parseAttribute(String nodePath, String attributeName, int item)
			throws Exception;

	boolean exsistsNode(String nodePath);

	boolean exsistsNode(String nodePath, int item);

	int countNodes(String nodePath) throws Exception;

}
