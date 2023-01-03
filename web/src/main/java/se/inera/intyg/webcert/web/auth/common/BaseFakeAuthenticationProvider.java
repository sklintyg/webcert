/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.common;

import java.util.Arrays;
import java.util.stream.Collectors;
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
import org.springframework.security.core.Authentication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.IntygUser;

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

    protected void applyAuthenticationMethod(Authentication token, Object details) {
        if (details instanceof IntygUser) {
            if (token.getCredentials() != null && ((FakeCredential) token.getCredentials()).getAuthenticationMethod() != null) {
                String authenticationMethod = ((FakeCredential) token.getCredentials()).getAuthenticationMethod();
                try {
                    if (authenticationMethod != null && !authenticationMethod.isEmpty()) {
                        IntygUser user = (IntygUser) details;
                        AuthenticationMethod newAuthMethod = AuthenticationMethod.valueOf(authenticationMethod);
                        user.setAuthenticationMethod(newAuthMethod);
                    }
                } catch (IllegalArgumentException e) {
                    String allowedTypes = Arrays.asList(AuthenticationMethod.values())
                        .stream()
                        .map(val -> val.name())
                        .collect(Collectors.joining(", "));
                    throw new AuthoritiesException(
                        "Could not set authenticationMethod '" + authenticationMethod + "'. Unknown, allowed types are "
                            + allowedTypes);
                }
            }
        }
    }

}
