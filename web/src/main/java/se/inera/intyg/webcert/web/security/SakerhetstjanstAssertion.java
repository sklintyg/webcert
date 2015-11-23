package se.inera.intyg.webcert.web.security;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSAnyImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * This SAML-assertion is based on the one described in the document
 * sakerhetstjanster_sambi_saml_profil_1.03.pdf.
 *
 * @author andreaskaltenbach, nikpet
 */
@SuppressWarnings("FieldMayBeFinal")
public class SakerhetstjanstAssertion {

    // HSA-titel för specificerad person
    public static final String TITEL_ATTRIBUTE = "urn:sambi:names:attribute:title";

    // Personens befattningskod
    public static final String TITEL_KOD_ATTRIBUTE = "urn:sambi:names:attribute:titleCode";

    // Förskrivarkod och gruppförskrivarkod för specificerad person
    // Om användaren är en legitimerad läkare så kommer förskrivarkoden i attributetet.
    // Om användaren inte är en legitimerad läkare så kommer gruppförskrivarkoden i attributet.
    public static final String FORSKRIVARKOD_ATTRIBUTE = "urn:sambi:names:attribute:personalPrescriptionCode";

    // Användarens HSA-ID.
    public static final String HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:employeeHsaId";

    // Användarens förnamn
    public static final String FORNAMN_ATTRIBUTE = "urn:sambi:names:attribute:givenName";

    // Användarens mellan och efternamn
    public static final String MELLAN_OCH_EFTERNAMN_ATTRIBUTE = "urn:sambi:names:attribute:middleAndSurname";

    // HSA-identitet på den vårdenhet aktuellt uppdrag tillhör
    public static final String ENHET_HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:careUnitHsaId";

    // Namn på den vårdenhet aktuellt uppdrag tillhör
    public static final String ENHET_NAMN_ATTRIBUTE = "urn:sambi:names:attribute:careUnitName";

    // HSA-identitet på den vårdgivare aktuellt uppdrag tillhör
    public static final String VARDGIVARE_HSA_ID_ATTRIBUTE = "urn:sambi:names:attribute:careProviderHsaId";

    // Namn på den vårdgivare aktuellt uppdrag tillhör
    public static final String VARDGIVARE_NAMN_ATTRIBUTE = "urn:sambi:names:attribute:careProviderName";

    // Syfte med aktuell uppdrag
    public static final String MEDARBETARUPPDRAG_TYPE = "urn:sambi:names:attribute:commissionPurpose";

    // HSA-identitet för valt uppdrag
    public static final String MEDARBETARUPPDRAG_ID = "urn:sambi:names:attribute:assignmentHsaId";

    private final List<String> titelKod = new ArrayList<>();
    private final List<String> titel = new ArrayList<>();

    private final List<String> forskrivarkod = new ArrayList<>();

    private String hsaId;

    private String fornamn;
    private String mellanOchEfternamn;

    private String enhetHsaId;
    private String enhetNamn;

    private String vardgivareHsaId;
    private String vardgivareNamn;

    private String medarbetaruppdragHsaId;
    private String medarbetaruppdragType;

    private String authenticationScheme;


    /* Constructor taking an Assertion object */
    public SakerhetstjanstAssertion(Assertion assertion) {
        if (assertion.getAttributeStatements() != null) {
            for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                extractAttributes(attributeStatement.getAttributes());
            }
        }

        if (!assertion.getAuthnStatements().isEmpty()) {
            authenticationScheme = assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        }
    }


    // - - - - -  Getters and setters - - - - -

    public List<String> getTitel() {
        return titel;
    }

    public List<String> getTitelKod() {
        return titelKod;
    }

    public List<String> getForskrivarkod() {
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

    public String getMedarbetaruppdragType() {
        return medarbetaruppdragType;
    }

    public String getMedarbetaruppdragHsaId() {
        return medarbetaruppdragHsaId;
    }

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }


    // - - - - - Private scope - - - - -

    private void extractAttributes(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            switch (attribute.getName()) {
                case TITEL_ATTRIBUTE:
                    titel.addAll(getValues(attribute));
                    break;
                case TITEL_KOD_ATTRIBUTE:
                    titelKod.addAll(getValues(attribute));
                    break;
                case FORSKRIVARKOD_ATTRIBUTE:
                    forskrivarkod.addAll(getValues(attribute));
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
                case MEDARBETARUPPDRAG_TYPE:
                    medarbetaruppdragType = getValue(attribute);
                    break;
                case MEDARBETARUPPDRAG_ID:
                    medarbetaruppdragHsaId = getValue(attribute);
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
            if (xmlObject.getDOM() != null) {
                values.add(xmlObject.getDOM().getTextContent());
            } else if (xmlObject instanceof XSAnyImpl) {
                values.add(((XSAnyImpl) xmlObject).getTextContent());
            }
        }
        return values;
    }

}
