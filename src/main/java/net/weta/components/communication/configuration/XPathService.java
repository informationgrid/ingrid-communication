/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package net.weta.components.communication.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XPathService implements IXPathService {

	private final XPath _xpath;
	private DocumentBuilder _documentBuilder;
	private Document _document;

	public XPathService() throws Exception {
		final XPathFactory factory = XPathFactory.newInstance();
		_xpath = factory.newXPath();
		_documentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
	}

	public void registerDocument(File xmlFile) throws Exception {
		_document = _documentBuilder.parse(xmlFile);
	}

	public void registerDocument(InputStream xmlFile) throws Exception {
        _document = _documentBuilder.parse(xmlFile);
    }
	
	public String parseAttribute(String nodePath, String attributeName)
			throws Exception {
		Node node = parseNode(_document, nodePath);
		Node attribute = getAttributeValue(attributeName, node);
		return attribute.getTextContent();
	}

	public String parseAttribute(String nodePath, String attributeName,
			int item)
			throws Exception {
		NodeList nodeList = parseNodes(_document, nodePath);
		Node node = nodeList.item(item);
		Node attribute = getAttributeValue(attributeName, node);
		return attribute.getTextContent();
	}
	
	public void setAttribute(String nodePath, String attributeName, String value) throws Exception {
        Node node = parseNode(_document, nodePath);
        Node attribute = getAttributeValue(attributeName, node);
        attribute.setTextContent(value);
    }
	
    public void setAttribute(String nodePath, String attributeName, String value, int item) throws Exception {
        NodeList nodeList = parseNodes(_document, nodePath);
        Node node = nodeList.item(item);
        Node attribute = getAttributeValue(attributeName, node);
        attribute.setTextContent(value);
    }

	public int countNodes(String nodePath) throws Exception {
		return parseNodes(_document, nodePath).getLength();
	}

	public boolean exsistsNode(String nodePath) {
		boolean ret = false;
		try {
			ret = parseNode(_document, nodePath) != null ? true : false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean exsistsNode(String nodePath, int item) {
		boolean ret = false;
		try {
			NodeList nodeList = parseNodes(_document, nodePath);
			ret = nodeList.item(item) != null ? true : false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void addAttribute(String nodePath, String attributeName, String value) throws Exception {
	    Node node = parseNode(_document, nodePath);
	    Attr attribute = _document.createAttribute(attributeName);
	    attribute.setTextContent(value);
	    NamedNodeMap attributes = node.getAttributes();
	    attributes.setNamedItem(attribute);
	}
	
	public void addAttribute(String nodePath, String attributeName, String value, int item) throws Exception {
	    Node node = parseNodes(_document, nodePath).item(item);
        Attr attribute = _document.createAttribute(attributeName);
        attribute.setTextContent(value);
        NamedNodeMap attributes = node.getAttributes();
        attributes.setNamedItem(attribute);
    }
	
	public boolean existsAttribute(String nodePath, String attributeName) {
	    boolean result = false;
	    try {
            Node node = parseNode(_document, nodePath);
            NamedNodeMap attributes = node.getAttributes();
            result = attributes.getNamedItem(attributeName) != null ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
	}
	
	public boolean existsAttribute(String nodePath, String attributeName, int item) {
        boolean result = false;
        try {
            Node node = parseNodes(_document, nodePath).item(item);
            NamedNodeMap attributes = node.getAttributes();
            result = attributes.getNamedItem(attributeName) != null ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
	
	public void removeAttribute(String nodePath, String attributeName) throws Exception {
	    Node node = parseNode(_document, nodePath);
	    NamedNodeMap attributes = node.getAttributes();
	    attributes.removeNamedItem(attributeName);
	}
	
	public void removeAttribute(String nodePath, String attributeName, int item) throws Exception {
	    Node node = parseNodes(_document, nodePath).item(item);
        NamedNodeMap attributes = node.getAttributes();
        attributes.removeNamedItem(attributeName);
	}
	
	public void removeAttributes(String nodePath, String attributeName) throws Exception {
        NodeList nodes = parseNodes(_document, nodePath);
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            attributes.removeNamedItem(attributeName);
        }
    }
	
	public void addNode(String parentPath, String elementName) throws Exception {
        Node parent = parseNode(_document, parentPath);
        Node node = _document.createElement(elementName);
        parent.appendChild(node);
    }
	
	public void addNode(String parentPath, String elementName, int item) throws Exception {
        Node parent = parseNodes(_document, parentPath).item(item);
        Node node = _document.createElement(elementName);
        parent.appendChild(node);
    }
	
	public void removeNode(String nodePath) throws Exception {
	    Node node = parseNode(_document, nodePath);
	    node.getParentNode().removeChild(node);
	}
	
	public void removeNode(String nodePath, int index) throws Exception {
	    Node node = parseNodes(_document, nodePath).item(index);
	    node.getParentNode().removeChild(node);
	}
	
	public void removeNodes(String nodePath) throws Exception {
        NodeList nodes = parseNodes(_document, nodePath);
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            node.getParentNode().removeChild(node);
        }
    }

    public void store(File xmlFile) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        LSOutput output = impl.createLSOutput();
        output.setEncoding("UTF-8");
        Writer fileWriter = new FileWriter(xmlFile);
        output.setCharacterStream(fileWriter );
        writer.write(_document, output);
        fileWriter.close();
    }


	private Node getAttributeValue(String attribute, Node element) {
		NamedNodeMap attributes = element.getAttributes();
		Node namedItem = attributes.getNamedItem(attribute);
		return namedItem;
	}

	private Node parseNode(Document document, String elementPath)
			throws XPathExpressionException {
		XPathExpression expr = _xpath.compile(elementPath);
		Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
		return node;
	}

	private NodeList parseNodes(Document document, String nodePath)
			throws Exception {
		XPathExpression expr = _xpath.compile(nodePath);
		NodeList list = (NodeList) expr.evaluate(document,
				XPathConstants.NODESET);
		return list;
	}
}