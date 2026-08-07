
package org.freedesktop.dbus.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.freedesktop.dbus.utils.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlUtil {
    private XmlUtil() {
    }

    public static boolean isElementType(Node _node) {
        return _node instanceof Element;
    }

    public static Element toElement(Node _node) {
        if (XmlUtil.isElementType(_node)) {
            return (Element)_node;
        }
        return null;
    }

    public static NodeList applyXpathExpressionToDocument(String _xpathExpression, Node _xmlDocumentOrNode) throws IOException {
        XPathFactory xfactory = XPathFactory.newInstance();
        XPath xpath = xfactory.newXPath();
        XPathExpression expr = null;
        try {
            expr = xpath.compile(_xpathExpression);
        }
        catch (XPathExpressionException _ex) {
            throw new IOException(_ex);
        }
        Object result = null;
        try {
            result = expr.evaluate(_xmlDocumentOrNode, XPathConstants.NODESET);
        }
        catch (Exception _ex) {
            throw new IOException(_ex);
        }
        return (NodeList)result;
    }

    public static Document parseXmlString(String _xmlStr, boolean _validating, boolean _namespaceAware) throws IOException {
        DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
        dbFac.setNamespaceAware(_namespaceAware);
        dbFac.setValidating(_validating);
        try {
            dbFac.setFeature("http:
            return dbFac.newDocumentBuilder().parse(new ByteArrayInputStream(_xmlStr.getBytes(StandardCharsets.UTF_8)));
        }
        catch (IOException _ex) {
            throw _ex;
        }
        catch (Exception _ex) {
            throw new IOException("Failed to parse " + Util.abbreviate(_xmlStr, 500), _ex);
        }
    }

    public static List<Element> convertToElementList(NodeList _nodeList) {
        ArrayList<Element> elemList = new ArrayList<Element>();
        for (int i = 0; i < _nodeList.getLength(); ++i) {
            Element elem = (Element)_nodeList.item(i);
            elemList.add(elem);
        }
        return elemList;
    }

    public static Map<String, String> convertToAttributeMap(NamedNodeMap _nodeMap) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < _nodeMap.getLength(); ++i) {
            Node node = _nodeMap.item(i);
            map.put(node.getNodeName(), node.getNodeValue());
        }
        return map;
    }
}

