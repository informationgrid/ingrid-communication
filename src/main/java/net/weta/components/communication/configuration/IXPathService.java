package net.weta.components.communication.configuration;

import java.io.File;
import java.io.InputStream;

public interface IXPathService {

    void registerDocument(File xmlFile) throws Exception;

    void registerDocument(InputStream xmlFile) throws Exception;

    String parseAttribute(String nodePath, String attributeName) throws Exception;

    String parseAttribute(String nodePath, String attributeName, int item) throws Exception;

    void setAttribute(String nodePath, String attributeName, String value, int item) throws Exception;

    boolean exsistsNode(String nodePath);

    boolean exsistsNode(String nodePath, int item);

    int countNodes(String nodePath) throws Exception;
    
    void store(File xmlFile) throws Exception;

}
