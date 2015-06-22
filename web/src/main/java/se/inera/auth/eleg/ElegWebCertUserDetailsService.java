package se.inera.auth.eleg;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Component;

import se.inera.auth.BaseWebCertUserDetailsService;
import se.inera.auth.exceptions.HsaServiceException;
import se.inera.webcert.client.PrivatePractitionerServiceClient;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.CV;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-06-16.
 */
@Component
public class ElegWebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ElegWebCertUserDetailsService.class);

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    private PrivatePractitionerServiceClient privatePractitionerServiceClient;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
        try {
            String personId = getAuthenticationAttribute(samlCredential, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
            HoSPersonType hosPerson = getHosPerson(personId);

            WebCertUser webCertUser = createWebcertUser(samlCredential, hosPerson);
            return webCertUser;
        } catch (Exception e) {
            logger.error("Error building user {}, failed with message {}", e.getMessage());
            throw new HsaServiceException("privatlakare, ej hsa", e);
        }
    }

    private String getAuthenticationAttribute(SAMLCredential samlCredential, String attributeName) {
        for (AttributeStatement attributeStatement : samlCredential.getAuthenticationAssertion().getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(attributeName)) {
                    for (XMLObject xmlObject : attribute.getAttributeValues()) {
                        return xmlObject.getDOM().getTextContent();
                    }
                }
            }
        }
        throw new IllegalArgumentException("Could not extract attribute '" + attributeName + "' from SAMLCredential.");
    }

    private WebCertUser createWebcertUser(SAMLCredential samlCredential, HoSPersonType hosPerson) {
        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setHsaId(hosPerson.getPersonalId().getExtension());
        webCertUser.setForskrivarkod(hosPerson.getForskrivarkod());
        webCertUser.setLakare(true);
        webCertUser.setNamn(hosPerson.getFullstandigtNamn());

        Vardenhet selectableVardenhet = new Vardenhet(hosPerson.getEnhet().getEnhetsId().getExtension(), hosPerson.getEnhet().getEnhetsnamn());
        webCertUser.setValdVardenhet(selectableVardenhet);
        webCertUser.setValdVardgivare(selectableVardenhet);

        List<Vardenhet> vardenhetList = new ArrayList<>();
        vardenhetList.add(selectableVardenhet);

        decorateWithLegitimeradeYrkesgrupper(hosPerson, webCertUser);
        decorateWithSpecialiceringar(hosPerson, webCertUser);
        decorateWebCertUserWithAvailableFeatures(webCertUser);

        if (samlCredential.getAuthenticationAssertion() != null) {
            String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
            webCertUser.setAuthenticationScheme(authnContextClassRef);
        }
        return webCertUser;
    }

    private void decorateWithLegitimeradeYrkesgrupper(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> legitimeradeYrkesgrupper = new ArrayList<>();
        for (CV cv : hosPerson.getLegitimeradYrkesgrupp()) {
            legitimeradeYrkesgrupper.add(cv.getDisplayName());
        }
        webCertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    }

    private void decorateWithSpecialiceringar(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> specialiteter = new ArrayList<>();
        for(CV cv : hosPerson.getSpecialitet()) {
            specialiteter.add(cv.getDisplayName());
        }
        webCertUser.setSpecialiseringar(specialiteter);
    }

    private HoSPersonType getHosPerson(String personId) {
        GetPrivatePractitionerResponseType response = privatePractitionerServiceClient.getPrivatePractitioner(logicalAddress, personId);
        if (response.getResultCode() != ResultCodeEnum.ERROR) {
            return response.getHoSPerson();
        } else {
            logger.error("Could not read HoSPerson from private practictioner service: " + response.getResultText());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, response.getResultText());
        }
    }

    @Autowired
    public void setPrivatePractitionerServiceClient(PrivatePractitionerServiceClient privatePractitionerServiceClient) {
        this.privatePractitionerServiceClient = privatePractitionerServiceClient;
    }
}
