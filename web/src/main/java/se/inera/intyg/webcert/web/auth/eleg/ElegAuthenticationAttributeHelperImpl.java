package se.inera.intyg.webcert.web.auth.eleg;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;

/**
 * Helper service for extracting attribute values from the SAMLCredential. Should be able to handle
 * values as both {@link org.opensaml.xml.schema.XSString} and as raw text content within the DOM element.
 *
 * Created by eriklupander on 2015-08-24.
 */
@Service
public class ElegAuthenticationAttributeHelperImpl implements ElegAuthenticationAttributeHelper {

    @Override
    public String getAttribute(SAMLCredential samlCredential, String attributeName) {
        for (AttributeStatement attributeStatement : samlCredential.getAuthenticationAssertion().getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(attributeName)) {

                    if (attribute.getAttributeValues().size() > 0) {
                        XMLObject xmlObject = attribute.getAttributeValues().get(0);
                        if (xmlObject instanceof XSString && ((XSString) xmlObject).getValue() != null) {
                            return ((XSString) xmlObject).getValue();
                        } else if (xmlObject.getDOM() != null) {
                            return xmlObject.getDOM().getTextContent();
                        }
                        throw new IllegalArgumentException("Cannot parse SAML2 response attribute '" + attributeName + "', is not XSString or DOM is null");
                    }
                }
            }
        }
        throw new IllegalArgumentException("Could not extract attribute '" + attributeName + "' from SAMLCredential.");
    }
}
