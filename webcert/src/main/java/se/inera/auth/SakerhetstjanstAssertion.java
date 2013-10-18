package se.inera.auth;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;

/**
 * @author andreaskaltenbach
 */
public class SakerhetstjanstAssertion {

    public static final String TITEL_ATTRIBUTE = "urn:sambi:names:attribute:title";
    private static final String TITEL_KOD_ATTRIBUTE = "urn:sambi:names:attribute:titleCode";

    private static final String FORSKRIVARKOD_ATTRIBUTE = "urn:sambi:names:attribute:personalPrescriptionCode";

    public static final String HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:employeeHsaId";

    public static final String FORNAMN_ATTRIBUTE = "urn:sambi:names:attribute:givenName";
    public static final String MELLAN_OCH_EFTERNAMN_ATTRIBUTE = "urn:sambi:names:attribute:middleAndSurname";

    private static final String ENHET_HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:careUnitHsaId";
    private static final String ENHET_NAMN_ATTRIBUTE = "urn:sambi:names:attribute:careUnitName";

    private static final String VARDGIVARE_HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:careProviderHsaId";
    private static final String VARDGIVARE_NAMN_ATTRIBUTE = "urn:sambi:names:attribute:careProviderName";

    private String titelKod;
    private String titel;

    private String forskrivarkod;

    private String hsaId;

    private String fornamn;
    private String mellanOchEfternamn;

    private String enhetHsaId;
    private String enhetNamn;

    private String vardgivareHsaId;
    private String vardgivareNamn;

    public SakerhetstjanstAssertion(Assertion assertion) {
        if (assertion.getAttributeStatements() != null) {
            for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                extractAttributes(assertion.getAttributeStatements().get(0).getAttributes());
            }
        }
    }

    private void extractAttributes(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            switch (attribute.getName()) {
            case TITEL_ATTRIBUTE:
                titel = getValue(attribute);
                break;
            case TITEL_KOD_ATTRIBUTE:
                titelKod = getValue(attribute);
                break;
            case FORSKRIVARKOD_ATTRIBUTE:
                forskrivarkod = getValue(attribute);
                break;
            case HSA_ID_ATTRIBUTE:
                hsaId = getValue(attribute);
                break;
            case FORNAMN_ATTRIBUTE:
                fornamn = getValue(attribute);
                break;
            case MELLAN_OCH_EFTERNAMN_ATTRIBUTE:
                mellanOchEfternamn = getValue(attribute);
                break;
            case ENHET_HSA_ID_ATTRIBUTE:
                enhetHsaId = getValue(attribute);
                break;
            case ENHET_NAMN_ATTRIBUTE:
                enhetNamn = getValue(attribute);
                break;
            case VARDGIVARE_HSA_ID_ATTRIBUTE:
                vardgivareHsaId = getValue(attribute);
                break;
            case VARDGIVARE_NAMN_ATTRIBUTE:
                vardgivareNamn = getValue(attribute);
                break;
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

    public String getTitel() {
        return titel;
    }

    public String getTitelKod() {
        return titelKod;
    }

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public String getHsaId() {
        return hsaId;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getMellanOchEfternamn() {
        return mellanOchEfternamn;
    }

    public String getEnhetHsaId() {
        return enhetHsaId;
    }

    public String getEnhetNamn() {
        return enhetNamn;
    }

    public String getVardgivareHsaId() {
        return vardgivareHsaId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }
}
