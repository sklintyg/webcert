package se.inera.intyg.webcert.web.auth.common;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides some common functionality for Fake authentication providers.
 *
 * Created by eriklupander on 2015-06-16.
 */
public abstract class BaseFakeAuthenticationProvider implements AuthenticationProvider {

    private static DocumentBuilder documentBuilder;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to instantiate DocumentBuilder", e);
        }
    }

    protected void attachAuthenticationContext(Assertion assertion, String authContextRef) {
        AuthnStatement authnStatement = new AuthnStatementBuilder().buildObject();
        AuthnContext authnContext = new AuthnContextBuilder().buildObject();
        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();

        authnContextClassRef.setAuthnContextClassRef(authContextRef);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);
    }

    protected Attribute createAttribute(String name, String value) {

        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setName(name);

        Document doc = documentBuilder.newDocument();
        Element element = doc.createElement("element");
        element.setTextContent(value);

        XMLObject xmlObject = new XSStringBuilder().buildObject(new QName("ns", "local"));
        xmlObject.setDOM(element);
        attribute.getAttributeValues().add(xmlObject);

        return attribute;
    }

}
