package se.inera.intyg.webcert.web.auth.eleg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Component;

import se.inera.intyg.webcert.web.auth.common.BaseWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.persistence.roles.model.Role;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
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
    private AvtalService avtalService;

    @Autowired
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Autowired
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {

        try {
            return createUser(samlCredential);
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", e.getMessage());
            throw new HsaServiceException("privatlakare, ej hsa", e);
        }
    }


    // - - - - - Default scope - - - - -

    protected WebCertUser createUser(SAMLCredential samlCredential) {

        String personId = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.PERSON_ID_ATTRIBUTE);

        assertHosPersonIsAuthorized(personId);

        HoSPersonType hosPerson = getHosPerson(personId);
        if (hosPerson == null) {
            throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
        }

        // Lookup user's role
        String userRole = lookupUserRole();

        WebCertUser webCertUser = createWebCertUser(hosPerson, userRole, samlCredential);
        return webCertUser;
    }


    // - - - - - Private scope - - - - -

    private void assertHosPersonIsAuthorized(String personId) {
        boolean authorized = ppService.validatePrivatePractitioner(logicalAddress, null, personId);
        if (!authorized) {
            // Throw exception that spring-security can pick up and redirect user to privatläkarportalen
            throw new PrivatePractitionerAuthorizationException("User is not authorized to access webcert according to private practitioner portal");
        }
    }

    private WebCertUser createWebCertUser(HoSPersonType hosPerson, String userRole, SAMLCredential samlCredential) {
        return createWebCertUser(hosPerson, getRoleRepository().findByName(userRole), samlCredential);
    }

    private WebCertUser createWebCertUser(HoSPersonType hosPerson, Role role, SAMLCredential samlCredential) {

        // Get user's privileges based on his/hers role
        final Map<String, UserRole> grantedRoles = roleToMap(getRoleAuthority(role));
        final Map<String, UserPrivilege> grantedPrivileges = getPrivilegeAuthorities(role);

        // Create the WebCert user object injection user's privileges
        WebCertUser user = new WebCertUser();

        user.setRoles(grantedRoles);
        user.setAuthorities(grantedPrivileges);

        user.setPrivatLakareAvtalGodkand(avtalService.userHasApprovedLatestAvtal(hosPerson.getHsaId().getExtension()));
        user.setHsaId(hosPerson.getHsaId().getExtension());
        user.setPersonId(hosPerson.getPersonId().getExtension());
        user.setNamn(hosPerson.getFullstandigtNamn());

        // Forskrivarkod should be always be seven zeros
        user.setForskrivarkod("0000000");

        decorateWebCertUserWithAuthenticationScheme(samlCredential, user);
        decorateWebCertUserWithAuthenticationMethod(samlCredential, user);
        decorateWebCertUserWithAvailableFeatures(user);
        decorateWebCertUserWithLegitimeradeYrkesgrupper(hosPerson, user);
        decorateWebCertUserWithSpecialiceringar(hosPerson, user);
        decorateWebCertUserWithVardgivare(hosPerson, user);
        decorateWebCertUserWithDefaultVardenhet(user);

        return user;
    }

    private void decorateWebCertUserWithAuthenticationMethod(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (!webCertUser.getAuthenticationScheme().endsWith(":fake")) {
            webCertUser.setAuthenticationMethod(elegAuthenticationMethodResolver.resolveAuthenticationMethod(samlCredential));
        } else {
            webCertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        }
    }

    private void decorateWebCertUserWithAuthenticationScheme(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (samlCredential.getAuthenticationAssertion() != null) {
            String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
            webCertUser.setAuthenticationScheme(authnContextClassRef);
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser webCertUser) {
        setFirstVardenhetOnFirstVardgivareAsDefault(webCertUser);
    }

    private void decorateWebCertUserWithVardgivare(HoSPersonType hosPerson, WebCertUser webCertUser) {
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

    private void decorateWebCertUserWithLegitimeradeYrkesgrupper(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> legitimeradeYrkesgrupper = new ArrayList<>();
        for (LegitimeradYrkesgruppType ly : hosPerson.getLegitimeradYrkesgrupp()) {
            legitimeradeYrkesgrupper.add(ly.getNamn());
        }
        webCertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    }

    private void decorateWebCertUserWithSpecialiceringar(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> specialiteter = new ArrayList<>();
        for (SpecialitetType st : hosPerson.getSpecialitet()) {
            specialiteter.add(st.getNamn());
        }
        webCertUser.setSpecialiseringar(specialiteter);
    }

    private HoSPersonType getHosPerson(String personId) {
        return ppService.getPrivatePractitioner(logicalAddress, null, personId);
    }

    /*
     * This method only handles privatläkare for now.
     * In a future there might be more logic here to decide user role.
     */
    private String lookupUserRole() {
        return UserRole.ROLE_PRIVATLAKARE.name();
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

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }

}
