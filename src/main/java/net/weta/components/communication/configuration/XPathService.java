package net.weta.components.communication.configuration;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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