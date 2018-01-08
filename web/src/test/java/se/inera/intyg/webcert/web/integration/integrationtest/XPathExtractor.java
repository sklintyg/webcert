/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.integration.integrationtest;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/*
 * Detta är en kopia av motsvarande klass i IT. Efter införande av gradle som byggsystem
 * skall dessa testutilklasser flyttas till en test-jar i common som alla applikationer SOAP tjänster kan använda.
 *
 * Se INTYG-2391, INTYG-2536
 */
public class XPathExtractor {
    private XPath xpath;
    private Document xmlDocument;

    public XPathExtractor(final String message, final Map<String, String> namespaceMap) {
        InputSource inputSource = new InputSource(new StringReader(message));
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            xmlDocument = domFactory.newDocumentBuilder().parse(inputSource);

            xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new XPathNamespaceContext(namespaceMap));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String getFragmentFromXPath(String xPathExpression) {
        try {
            XPathExpression expr = xpath.compile(xPathExpression);
            NodeList matches = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
            if (matches.getLength() > 0) {
                Node node = matches.item(0);
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(node), new StreamResult(writer));
                return writer.toString();
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

    private static class XPathNamespaceContext implements NamespaceContext {

        ImmutableMap<String, String> soapNamespace = ImmutableMap.of("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        private final Map<String, String> namespaceMap;

        public XPathNamespaceContext(final Map<String, String> namespaceMap) {
            this.namespaceMap = new ImmutableMap.Builder<String, String>().putAll(soapNamespace).putAll(namespaceMap).build();
        }

        public String getNamespaceURI(final String prefix) {
            if (namespaceMap.get(prefix) != null) {
                return namespaceMap.get(prefix);
            }
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(final String uri) {
            throw new UnsupportedOperationException();
        }

        public Iterator<String> getPrefixes(final String uri) {
            throw new UnsupportedOperationException();
        }

    }

}
