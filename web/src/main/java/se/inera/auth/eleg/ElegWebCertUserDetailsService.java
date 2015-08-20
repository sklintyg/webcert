package se.inera.auth.eleg;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Component;

import se.inera.auth.common.BaseWebCertUserDetailsService;
import se.inera.auth.exceptions.HsaServiceException;
import se.inera.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;

/**
 * Created by eriklupander on 2015-06-16.
 */
@Component
public class ElegWebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(ElegWebCertUserDetailsService.class);

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    private PPService ppService;

    private AvtalService avtalService;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
        try {
            String personId = getAuthenticationAttribute(samlCredential, CgiElegAssertion.PERSON_ID_ATTRIBUTE);

            boolean authorized = verfifyHosPersonIsAuthorized(personId);
            if (!authorized) {
                // Throw exception that spring-security can pick up and redirect user to privatläkarportalen
                throw new PrivatePractitionerAuthorizationException("User is not authorized to access webcert according to private practitioner portal");
            }

            HoSPersonType hosPerson = getHosPerson(personId);
            if (hosPerson == null) {
                throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
            }

            // Note that privatlakare must accept webcert terms in order to use the software. However, that's
            // handled separately in the TermsFilter.

            WebCertUser webCertUser = createWebcertUser(samlCredential, hosPerson);
            return webCertUser;
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }
            LOG.error("Error building user {}, failed with message {}", e.getMessage());
            throw new HsaServiceException("privatlakare, ej hsa", e);
        }
    }

    private boolean verfifyHosPersonIsAuthorized(String personId) {
        return ppService.validatePrivatePractitioner(logicalAddress, null, personId);
    }

    private String getAuthenticationAttribute(SAMLCredential samlCredential, String attributeName) {
        for (AttributeStatement attributeStatement : samlCredential.getAuthenticationAssertion().getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(attributeName)) {
                    for (XMLObject xmlObject : attribute.getAttributeValues()) {
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

    private WebCertUser createWebcertUser(SAMLCredential samlCredential, HoSPersonType hosPerson) {
        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setHsaId(hosPerson.getHsaId().getExtension());
        webCertUser.setForskrivarkod(hosPerson.getForskrivarkod());
        webCertUser.setLakare(true);
        webCertUser.setNamn(hosPerson.getFullstandigtNamn());

        decorateWithVardgivare(hosPerson, webCertUser);
        decorateWithLegitimeradeYrkesgrupper(hosPerson, webCertUser);
        decorateWithSpecialiceringar(hosPerson, webCertUser);
        decorateWebCertUserWithAvailableFeatures(webCertUser);

        if (samlCredential.getAuthenticationAssertion() != null) {
            String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
            webCertUser.setAuthenticationScheme(authnContextClassRef);
        }

        setDefaultSelectedVardenhetOnUser(webCertUser);

        return webCertUser;
    }

    private void decorateWithVardgivare(HoSPersonType hosPerson, WebCertUser webCertUser) {
        Vardgivare vardgivare = new Vardgivare(hosPerson.getEnhet().getVardgivare().getVardgivareId(), hosPerson.getEnhet().getVardgivare().getVardgivarenamn());

        Vardenhet vardenhet = new Vardenhet(hosPerson.getEnhet().getEnhetsId().getExtension(), hosPerson.getEnhet().getEnhetsnamn());
        resolveArbetsplatsKod(hosPerson, vardenhet);
        vardenhet.setPostadress(hosPerson.getEnhet().getPostadress());
        vardenhet.setPostnummer(hosPerson.getEnhet().getPostnummer());
        vardenhet.setPostort(hosPerson.getEnhet().getPostort());
        vardenhet.setTelefonnummer(hosPerson.getEnhet().getTelefonnummer());
        vardenhet.setEpost(hosPerson.getEnhet().getEpost());

        List<Vardenhet> vardenhetList = new ArrayList<>();
        vardenhetList.add(vardenhet);
        vardgivare.setVardenheter(vardenhetList);

        List<Vardgivare> vardgivareList = new ArrayList<>();
        vardgivareList.add(vardgivare);
        webCertUser.setVardgivare(vardgivareList);

        webCertUser.setValdVardenhet(vardenhet);
        webCertUser.setValdVardgivare(vardgivare);
    }

    /**
     * Arbetsplatskod is not mandatory for Privatläkare. In that case, use the HSA-ID of the practitioner.
     * (See Informationspecification Webcert, version 4.6, page 83)
     */
    private void resolveArbetsplatsKod(HoSPersonType hosPerson, Vardenhet vardenhet) {
        if (hosPerson.getEnhet().getArbetsplatskod() == null || hosPerson.getEnhet().getArbetsplatskod().getExtension() == null || hosPerson.getEnhet().getArbetsplatskod().getExtension().trim().length() == 0) {
            vardenhet.setArbetsplatskod(hosPerson.getHsaId().getExtension());
        } else {
            vardenhet.setArbetsplatskod(hosPerson.getEnhet().getArbetsplatskod().getExtension());
        }
    }

    private void decorateWithLegitimeradeYrkesgrupper(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> legitimeradeYrkesgrupper = new ArrayList<>();
        for (String str : hosPerson.getLegitimeradYrkesgrupp()) {
            legitimeradeYrkesgrupper.add(str);
        }
        webCertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    }

    private void decorateWithSpecialiceringar(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> specialiteter = new ArrayList<>();
        for (SpecialitetType st : hosPerson.getSpecialitet()) {
            specialiteter.add(st.getNamn());
        }
        webCertUser.setSpecialiseringar(specialiteter);
    }

    private HoSPersonType getHosPerson(String personId) {
        HoSPersonType hoSPersonType = ppService.getPrivatePractitioner(logicalAddress, null, personId);
        return hoSPersonType;
    }



    private void setDefaultSelectedVardenhetOnUser(WebCertUser user) {
        setFirstVardenhetOnFirstVardgivareAsDefault(user);
    }

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }



    @Autowired
    public void setPpService(PPService ppService) {
        this.ppService = ppService;
    }

    @Autowired
    public void setAvtalService(AvtalService avtalService) {
        this.avtalService = avtalService;
    }
}
