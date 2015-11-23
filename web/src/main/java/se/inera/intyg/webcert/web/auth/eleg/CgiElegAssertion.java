package se.inera.intyg.webcert.web.auth.eleg;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;

/**
 * SAML-assertion for CGI säkerhetstjänst used for e-leg/privatläkare.
 *
 * @author eriklupander
 */
public class CgiElegAssertion {

    public static final String PERSON_ID_ATTRIBUTE = "Subject_SerialNumber";
    public static final String FORNAMN_ATTRIBUTE = "Subject_GivenName";
    public static final String MELLAN_OCH_EFTERNAMN_ATTRIBUTE = "Subject_Surname";

    public static final String UTFARDARE_ORGANISATIONSNAMN_ATTRIBUTE = "Issuer_OrganizationName";
    public static final String UTFARDARE_CA_NAMN_ATTRIBUTE = "Issuer_CommonName";
    public static final String SECURITY_LEVEL_ATTRIBUTE = "SecurityLevel"; // 3 == e-leg på fil, 4 == e-leg på kort.
    public static final String LOGIN_METHOD = "LoginMethod";  // ccp1,2,8,10,11,12,13

    private String personId;
    private String fornamn;
    private String efternamn;

    private String utfardareOrganisationsNamn;
    private String utfardareCANamn;
    private String securityLevel;

    private String authenticationScheme;
    private String loginMethod;

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
                case PERSON_ID_ATTRIBUTE:
                    personId = getValue(attribute);
                    break;
                case FORNAMN_ATTRIBUTE:
                    fornamn = getValue(attribute);
                    break;
                case MELLAN_OCH_EFTERNAMN_ATTRIBUTE:
                    efternamn = getValue(attribute);
                    break;
                case UTFARDARE_CA_NAMN_ATTRIBUTE:
                    utfardareCANamn = getValue(attribute);
                    break;
                case UTFARDARE_ORGANISATIONSNAMN_ATTRIBUTE:
                    utfardareOrganisationsNamn = getValue(attribute);
                    break;
                case SECURITY_LEVEL_ATTRIBUTE:
                    securityLevel = getValue(attribute);
                    break;
                case LOGIN_METHOD:
                    loginMethod = getValue(attribute);
                    break;
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

    public String getPersonId() {
        return personId;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public String getUtfardareOrganisationsNamn() {
        return utfardareOrganisationsNamn;
    }

    public String getUtfardareCANamn() {
        return utfardareCANamn;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public String getLoginMethod() {
        return loginMethod;
    }
}
