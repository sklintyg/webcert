package se.inera.auth.eleg;

import java.util.ArrayList;
import java.util.List;

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
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.LegitimeradYrkesgruppType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;

/**
 * Created by eriklupander on 2015-06-16.
 *
 * Note that privatlakare must accept webcert terms in order to use the software. However, that's
 * handled separately in the TermsFilter.
 */
@Component
public class ElegWebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(ElegWebCertUserDetailsService.class);

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private PPService ppService;

    @Autowired
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Autowired
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
        try {
            String personId = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.PERSON_ID_ATTRIBUTE);

            boolean authorized = verfifyHosPersonIsAuthorized(personId);
            if (!authorized) {
                // Throw exception that spring-security can pick up and redirect user to privatläkarportalen
                throw new PrivatePractitionerAuthorizationException("User is not authorized to access webcert according to private practitioner portal");
            }

            HoSPersonType hosPerson = getHosPerson(personId);
            if (hosPerson == null) {
                throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
            }

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

    private WebCertUser createWebcertUser(SAMLCredential samlCredential, HoSPersonType hosPerson) {
        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setPrivatLakare(true);
        webCertUser.setPrivatLakareAvtalGodkand(false);
        webCertUser.setHsaId(hosPerson.getHsaId().getExtension());
        webCertUser.setPersonId(hosPerson.getPersonId().getExtension());
        webCertUser.setForskrivarkod(hosPerson.getForskrivarkod());
        webCertUser.setLakare(true);
        webCertUser.setNamn(hosPerson.getFullstandigtNamn());


        decorateWithVardgivare(hosPerson, webCertUser);
        decorateWithLegitimeradeYrkesgrupper(hosPerson, webCertUser);
        decorateWithSpecialiceringar(hosPerson, webCertUser);
        decorateWebCertUserWithAvailableFeatures(webCertUser);

        decorateWithAuthenticationScheme(samlCredential, webCertUser);
        decorateWithAuthenticationMethod(samlCredential, webCertUser);

        setDefaultSelectedVardenhetOnUser(webCertUser);

        return webCertUser;
    }

    private void decorateWithAuthenticationScheme(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (samlCredential.getAuthenticationAssertion() != null) {
            String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
            webCertUser.setAuthenticationScheme(authnContextClassRef);
        }
    }

    private void decorateWithAuthenticationMethod(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (!webCertUser.getAuthenticationScheme().endsWith(":fake")) {
            webCertUser.setAuthenticationMethod(elegAuthenticationMethodResolver.resolveAuthenticationMethod(samlCredential));
        } else {
            webCertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        }
    }

    private void decorateWithVardgivare(HoSPersonType hosPerson, WebCertUser webCertUser) {
        String id = hosPerson.getEnhet().getVardgivare().getVardgivareId().getExtension();
        String namn = hosPerson.getEnhet().getVardgivare().getVardgivarenamn();

        Vardgivare vardgivare = new Vardgivare(id, namn);

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
        for (LegitimeradYrkesgruppType ly : hosPerson.getLegitimeradYrkesgrupp()) {
            legitimeradeYrkesgrupper.add(ly.getNamn());
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



//    @Autowired
//    public void setPpService(PPService ppService) {
//        this.ppService = ppService;
//    }
//
//    @Autowired
//    public void setAvtalService(AvtalService avtalService) {
//        this.avtalService = avtalService;
//    }
//
//    @Autowired
//    public void setElegAuthenticationAttributeHelper(ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper) {
//        this.elegAuthenticationAttributeHelper = elegAuthenticationAttributeHelper;
//    }
//
//    @Autowired
//    public void setElegAuthenticationMethodResolver(ElegAuthenticationMethodResolver elegAuthenticationMethodResolver) {
//        this.elegAuthenticationMethodResolver = elegAuthenticationMethodResolver;
//    }
}
