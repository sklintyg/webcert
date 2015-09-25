package se.inera.webcert.security;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.auth.common.BaseWebCertUserDetailsService;
import se.inera.auth.exceptions.HsaServiceException;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.user.dto.WebCertUser;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author andreaskaltenbach
 */
@Service
public class WebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    // ~ Static fields/initializers
    // =====================================================================================

    public static final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "/visa/intyg/.+/.*";
    public static final String REGEXP_REQUESTURI_UTHOPP = "/webcert/web/user/certificate/.+/questions";

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String LAKARE = "Läkare";
    private static final String TANDLAKARE = "Tandläkare";

    private static final String LAKARE_KOD_204010 = "204010";
    private static final String LAKARE_KOD_203090 = "203090";
    private static final String LAKARE_KOD_204090 = "204090";


    // ~ Instance fields
    // =====================================================================================

    private SAMLCredential credential;

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    // - - - - - Public scope - - - - -

    public Object loadUserBySAML(SAMLCredential credential) {

        LOG.info("User authentication was successful. SAML credential is: {}", credential);

        this.credential = credential;

        try {
            DefaultSavedRequest savedRequest = getCurrentRequest();

            // Create the user
            WebCertUser webCertUser = createUser(savedRequest, getAssertion());
            return webCertUser;

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", getAssertion().getHsaId(), e.getMessage());
            throw new RuntimeException(getAssertion().getHsaId(), e);
        }
    }

    public SakerhetstjanstAssertion getAssertion() {
        if (credential == null) {
            throw new RuntimeException("No SAMLCredential has been set.");
        }

        return getAssertion(credential.getAuthenticationAssertion());
    }

    // - - - - - Package scope - - - - -

    WebCertUser createUser(DefaultSavedRequest savedRequest, SakerhetstjanstAssertion assertion) {

        List<Vardgivare> authorizedVardgivare = getAuthorizedVardgivare(assertion.getHsaId());
        List<GetHsaPersonHsaUserType> hsaPersonInfo = getPersonInfo(assertion.getHsaId());

        try {
            assertMIU();
            assertAuthorizedVardgivare(authorizedVardgivare);

            // Decide user's role
            String userRole = lookupUserRole(savedRequest, getAssertion(), hsaPersonInfo);

            WebCertUser webCertUser = createWebCertUser(userRole, authorizedVardgivare, hsaPersonInfo);
            return webCertUser;

        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(getAssertion().getHsaId());
            throw e;
        }

    }

    private List<Vardgivare> getAuthorizedVardgivare(String hsaId) {
        try {
            return hsaOrganizationsService.getAuthorizedEnheterForHosPerson(getAssertion().getHsaId());

        } catch (Exception e) {
            LOG.error("Failed retrieving authorized units from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }
    }

    private List<GetHsaPersonHsaUserType> getPersonInfo(String hsaId) {
        try {
            List<GetHsaPersonHsaUserType> hsaPersonInfo = hsaPersonService.getHsaPersonInfo(hsaId);
            if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
                LOG.info("getHsaPersonInfo did not return any info for user '{}'", hsaId);
            }
            return hsaPersonInfo;

        } catch (Exception e) {
            LOG.error("Failed retrieving user information from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }
    }

    String lookupUserRole(DefaultSavedRequest savedRequest, SakerhetstjanstAssertion assertion, List<GetHsaPersonHsaUserType> hsaPersonInfo) {

        // 1. Kolla yrkesgrupper och se vilken roll som användaren ska ha.
        //    Görs mha hsaPersonInfo
        // ?? vad händer om det är mer än en yrkersgrupp

        UserRole userRole = lookupUserRoleByLegitimeradeYrkesgrupper(extractLegitimeradeYrkesgrupper(hsaPersonInfo));

        // If user has the role 'Tandläkare' then return
        if (UserRole.ROLE_TANDLAKARE.equals(userRole)) {
            return userRole.name();
        }


        // 2. Kan inte rollen bestämmas via 1 så kombinera befattningskod och gruppförskrivarkod
        //    för att bestämma användarens roll. Detta ska gå att hämta ur SAML-biljetten
        //
        //    Gruppförskrivarkoden också kommer i personalPrescriptionCode och då måste man också hålla reda på beffattningskoden.
        //
        //    Ett problem som vi har i Pascal är att om en användare har dubbla legitimationer (vilket förekommer), t ex SSK och AT-läkare. Då kommer det 2 befattningskoder i biljetten, men då litar vi inte på biljetten utan gör en ny slagning mot HSA.
        if (userRole == null) {
            userRole = lookupUserRoleByBefattningskod(assertion);
        }

        boolean doctor = isDoctor(assertion);

        // Use the request URI to decide if this is a 'djupintegration' or 'uthopp' user
        if (savedRequest != null && savedRequest.getRequestURI() != null) {
            String uri = savedRequest.getRequestURI();

            if (uri.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
                if (doctor) {
                    return UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name();
                } else {
                    return UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name();
                }
            }

            if (uri.matches(REGEXP_REQUESTURI_UTHOPP)) {
                if (doctor) {
                    return UserRole.ROLE_LAKARE_UTHOPP.name();
                } else {
                    return UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name();
                }
            }
        }

        //
        // This user is a regular WebCert user
        //

        if (doctor) {
            return UserRole.ROLE_LAKARE.name();
        }

        // Default to the 'Vårdadministratör' role
        return UserRole.ROLE_VARDADMINISTRATOR.name();
    }

    private UserRole lookupUserRoleByBefattningskod(SakerhetstjanstAssertion assertion) {

        List<String> befattningsKoder = assertion.getTitelKod();

        if (befattningsKoder.size() > 1) {
            // Ett problem som vi har i Pascal är att om en användare har dubbla legitimationer
            // (vilket förekommer), t ex SSK och AT-läkare. Då kommer det 2 befattningskoder
            // i biljetten, men då litar vi inte på biljetten utan gör en ny slagning mot HSA.

            // TODO call hsa and return correct UserRole
            return null;

        } else if (befattningsKoder.contains(LAKARE_KOD_204010)) {
            return UserRole.ROLE_LAKARE;
        } else {
            // Make a lookup for gruppförskrivarkod

            // TODO return lookupUserRoleByGruppforskrivarkod(assertion);
            LOG.debug("");
        }

        return null;
    }

    UserRole lookupUserRoleByLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        if (legitimeradeYrkesgrupper.contains(LAKARE)) {
            return UserRole.ROLE_LAKARE;
        }
        if (legitimeradeYrkesgrupper.contains(TANDLAKARE)) {
            return UserRole.ROLE_TANDLAKARE;
        }

        return null;
    }

    SakerhetstjanstAssertion getAssertion(Assertion assertion) {
        if (assertion == null) {
            throw new IllegalArgumentException("Assertion parameter cannot be null");
        }

        return new SakerhetstjanstAssertion(assertion);
    }

    DefaultSavedRequest getCurrentRequest() {
        HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) curRequest.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
        return savedRequest;
    }


    // - - - - - Private scope - - - - -

    private boolean isDoctor(SakerhetstjanstAssertion assertion) {
        // 'Lakare' flag is calculated by checking for lakare profession in title and title code
        if (assertion.getTitel().contains(LAKARE) || assertion.getTitelKod().contains(LAKARE_KOD_204010)) {
            return true;
        }

        return false;
    }

    private void assertAuthorizedVardgivare(List<Vardgivare> authorizedVardgivare) {
        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(getAssertion().getHsaId());
        }
    }

    private void assertMIU() {
        // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
        if (!VARD_OCH_BEHANDLING.equals(getAssertion().getMedarbetaruppdragType())) {
            throw new MissingMedarbetaruppdragException(getAssertion().getHsaId());
        }
    }

    private WebCertUser createWebCertUser(String userRole, List<Vardgivare> authorizedVardgivare, List<GetHsaPersonHsaUserType> hsaPersonInfo) {
        Role role = getRoleRepository().findByName(userRole);
        return createWebCertUser(role, authorizedVardgivare, hsaPersonInfo);
    }

    private WebCertUser createWebCertUser(Role role, List<Vardgivare> authorizedVardgivare, List<GetHsaPersonHsaUserType> hsaPersonInfo) {

        // Get user's privileges based on his/hers role
        final Map<String, UserRole> grantedRoles = roleToMap(getRoleAuthority(role));
        final Map<String, UserPrivilege> grantedPrivileges = getPrivilegeAuthorities(role);

        // Create the WebCert user object injection user's privileges
        WebCertUser webcertUser = new WebCertUser();

        webcertUser.setRoles(grantedRoles);
        webcertUser.setAuthorities(grantedPrivileges);

        webcertUser.setHsaId(getAssertion().getHsaId());
        webcertUser.setNamn(compileName(getAssertion().getFornamn(), getAssertion().getMellanOchEfternamn()));
        webcertUser.setVardgivare(authorizedVardgivare);

        // Förskrivarkod is sensitiv information, not allowed to store real value
        webcertUser.setForskrivarkod("0000000");

        // Set user's authentication scheme
        webcertUser.setAuthenticationScheme(getAssertion().getAuthenticationScheme());

        decorateWebCertUserWithAdditionalInfo(webcertUser, hsaPersonInfo);
        decorateWebCertUserWithAvailableFeatures(webcertUser);
        decorateWebCertUserWithAuthenticationMethod(webcertUser);
        decorateWebCertUserWithDefaultVardenhet(webcertUser);

        return webcertUser;
    }

    private void decorateWebCertUserWithAdditionalInfo(WebCertUser webcertUser, List<GetHsaPersonHsaUserType> hsaPersonInfo) {

        List<String> specialiseringar = extractSpecialiseringar(hsaPersonInfo);
        webcertUser.setSpecialiseringar(specialiseringar);

        List<String> legitimeradeYrkesgrupper = extractLegitimeradeYrkesgrupper(hsaPersonInfo);
        webcertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);

        String titel = extractTitel(hsaPersonInfo);
        webcertUser.setTitel(titel);
    }

    private void decorateWebCertUserWithAuthenticationMethod(WebCertUser webcertUser) {
        String authenticationScheme = getAssertion().getAuthenticationScheme();

        if (authenticationScheme.endsWith(":fake")) {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        } else {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser user) {

        // Get HSA id for the selected MIU
        String medarbetaruppdragHsaId = getAssertion().getEnhetHsaId();

        boolean changeSuccess;

        if (StringUtils.isNotBlank(medarbetaruppdragHsaId)) {
            changeSuccess = user.changeValdVardenhet(medarbetaruppdragHsaId);
        } else {
            LOG.error("Assertion did not contain any 'medarbetaruppdrag', defaulting to use one of the Vardenheter present in the user");
            changeSuccess = setFirstVardenhetOnFirstVardgivareAsDefault(user);
        }

        if (!changeSuccess) {
            LOG.error("When logging in user '{}', unit with HSA-id {} could not be found in users MIUs", user.getHsaId(), medarbetaruppdragHsaId);
            throw new MissingMedarbetaruppdragException(user.getHsaId());
        }

        LOG.debug("Setting care unit '{}' as default unit on user '{}'", user.getValdVardenhet().getId(), user.getHsaId());
    }

    private List<String> extractLegitimeradeYrkesgrupper(List<GetHsaPersonHsaUserType> hsaUserTypes) {

        Set<String> lygSet = new TreeSet<>();

        for (GetHsaPersonHsaUserType userType : hsaUserTypes) {
            if (userType.getHsaTitles() != null) {
                List<String> hsaTitles = userType.getHsaTitles().getHsaTitle();
                lygSet.addAll(hsaTitles);
            }
        }

        List<String> list = new ArrayList<String>(lygSet);
        return list;
    }

    private List<String> extractSpecialiseringar(List<GetHsaPersonHsaUserType> hsaUserTypes) {

        Set<String> specSet = new TreeSet<>();

        for (GetHsaPersonHsaUserType userType : hsaUserTypes) {
            if (userType.getSpecialityNames() != null) {
                List<String> specialityNames = userType.getSpecialityNames().getSpecialityName();
                specSet.addAll(specialityNames);
            }
        }

        List<String> list = new ArrayList<String>(specSet);
        return list;
    }

    private String extractTitel(List<GetHsaPersonHsaUserType> hsaUserTypes) {

        List<String> titlar = new ArrayList<String>();

        for (GetHsaPersonHsaUserType userType : hsaUserTypes) {
            if (StringUtils.isNotBlank(userType.getTitle())) {
                titlar.add(userType.getTitle());
            }
        }

        return StringUtils.join(titlar, COMMA);
    }

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }

}
