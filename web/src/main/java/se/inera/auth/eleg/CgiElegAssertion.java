package se.inera.auth.eleg;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;

/**
 * SAML-assertion for CGI säkerhetstjänst.
 *
 * This is just a placeholder for now, add constants and extractions etc. once we have a definite contract to work with.
 */
public class CgiElegAssertion {

    public static final String PERSON_ID_ATTRIBUTE = "urn:fix:me:cgi:attribute:personId";
    public static final String FORNAMN_ATTRIBUTE = "urn:fix:me:cgi:attribute:givenName";
    public static final String MELLAN_OCH_EFTERNAMN_ATTRIBUTE = "urn:fix:me:cgi:attribute:middleAndSurname";

    private String authenticationScheme;

    public CgiElegAssertion(Assertion assertion) {
        if (assertion.getAttributeStatements() != null) {
            for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                extractAttributes(attributeStatement.getAttributes());
            }
        }

        if (!assertion.getAuthnStatements().isEmpty()) {
            authenticationScheme = assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        }
    }

    private void extractAttributes(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            switch (attribute.getName()) {

                default:
                    // Ignore.
            }
        }
    }

    private String getValue(Attribute attribute) {
        List<String> values = getValues(attribute);
        return (values.isEmpty()) ? null : values.get(0);
    }

    private List<String> getValues(Attribute attribute) {
        List<String> values = new ArrayList<>();
        if (attribute.getAttributeValues() == null) {
            return values;
        }
        for (XMLObject xmlObject : attribute.getAttributeValues()) {
            values.add(xmlObject.getDOM().getTextContent());
        }
        return values;
    }

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }
}
