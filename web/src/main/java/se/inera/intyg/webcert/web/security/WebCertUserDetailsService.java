package se.inera.intyg.webcert.web.security;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;
import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
import se.inera.intyg.webcert.web.auth.common.BaseWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.persistence.roles.model.Role;
import se.inera.intyg.webcert.persistence.roles.model.TitleCode;
import se.inera.intyg.webcert.persistence.roles.repository.TitleCodeRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "/visa/intyg/.+";
    public static final String REGEXP_REQUESTURI_UTHOPP = "/webcert/web/user/certificate/.+/questions";

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    // Titles, a.k.a 'legitimerad yrkesgrupp', has a coding system governing these titles. See:
    // HSA Innehåll Legitimerad yrkesgrupp
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    private static final String TITLE_LAKARE = "Läkare";
    private static final String TITLE_TANDLAKARE = "Tandläkare";

    // Titl codes, a.k.a 'befattningskod', has a coding system governing these codes. See:
    // HSA Innehåll Befattning
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    private static final String TITLECODE_AT_LAKARE = "204010";


    // ~ Instance fields
    // =====================================================================================

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private TitleCodeRepository titleCodeRepository;


    // - - - - - Public scope - - - - -

    @Override
    public Object loadUserBySAML(SAMLCredential credential) {

        if (credential == null) {
            throw new RuntimeException("SAMLCredential has not been set.");
        }

        LOG.info("Start user authentication...");

        if (LOG.isDebugEnabled()) {
            // I dont want to read this object every time.
            String str = ToStringBuilder.reflectionToString(credential);
            LOG.debug("SAML credential is:\n{}", str);
        }

        try {
            // Create the user
            WebCertUser webCertUser = createUser(credential);

            LOG.info("End user authentication...SUCCESS");
            return webCertUser;

        } catch (Exception e) {
            LOG.error("End user authentication...FAIL");
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", getAssertion(credential).getHsaId(), e.getMessage());
            throw new RuntimeException(getAssertion(credential).getHsaId(), e);
        }
    }


    // - - - - - Protected scope - - - - -

    protected SakerhetstjanstAssertion getAssertion(SAMLCredential credential) {
        return getAssertion(credential.getAuthenticationAssertion());
    }

    protected List<Vardgivare> getAuthorizedVardgivare(String hsaId) {
        LOG.debug("Retrieving authorized units from HSA...");

        try {
            return hsaOrganizationsService.getAuthorizedEnheterForHosPerson(hsaId);

        } catch (Exception e) {
            LOG.error("Failed retrieving authorized units from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }
    }

    protected List<GetHsaPersonHsaUserType> getPersonInfo(String hsaId) {
        LOG.debug("Retrieving user information from HSA...");

        List<GetHsaPersonHsaUserType> hsaPersonInfo;
        try {
            hsaPersonInfo = hsaPersonService.getHsaPersonInfo(hsaId);
            if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
                LOG.info("Call to web service getHsaPersonInfo did not return any info for user '{}'", hsaId);
            }

        } catch (Exception e) {
            LOG.error("Failed retrieving user information from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }

        return hsaPersonInfo;
    }


    protected DefaultSavedRequest getRequest() {
        HttpServletRequest curRequest = getCurrentRequest();
        return (DefaultSavedRequest) curRequest.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
    }

    // - - - - - Package scope - - - - -

    WebCertUser createUser(SAMLCredential credential) {
        LOG.debug("Creating Webcert user object...");

        String hsaId = getAssertion(credential).getHsaId();
        List<GetHsaPersonHsaUserType> personInfo = getPersonInfo(hsaId);
        List<Vardgivare> authorizedVardgivare = getAuthorizedVardgivare(hsaId);

        try {
            assertMIU(credential);
            assertAuthorizedVardgivare(hsaId, authorizedVardgivare);

            String userRole = lookupUserRole(credential, personInfo);
            LOG.debug("User role is set to {}", userRole);

            return createWebCertUser(userRole, credential, authorizedVardgivare, personInfo);

        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(getAssertion(credential).getHsaId());
            throw e;
        }

    }

    String lookupUserRole(SAMLCredential credential, List<GetHsaPersonHsaUserType> personInfo) {
        LOG.debug("Looking up user role by:");

        UserRole userRole = lookupUserRole(getAssertion(credential), personInfo);

        // Vi har en användarroll men kontroller också ifall användaren
        // kommer in via djupintegration eller uthoppslänk.
        userRole = lookupUserRoleByRequestURI(userRole);

        return userRole.name();
    }

    UserRole lookupUserRole(SakerhetstjanstAssertion sa, List<GetHsaPersonHsaUserType> personInfo) {
        UserRole userRole;

        // 1. Bestäm användarens roll utefter titel som kommer från SAML.
        //    Titel ska vara detsamma som legitimerade yrkesgrupper.
        userRole = lookupUserRoleByLegitimeradeYrkesgrupper(sa.getTitel());
        if (userRole != null) {
            return userRole;
        }

        // 2. Bestäm användarens roll utefter legitimerade yrkesgrupper som hämtas från HSA.
        userRole = lookupUserRoleByLegitimeradeYrkesgrupper(extractLegitimeradeYrkesgrupper(personInfo));
        if (userRole != null) {
            return userRole;
        }

        // 3. Bestäm användarens roll utefter befattningskod som kommer från SAML.
        userRole = lookupUserRoleByBefattningskod(sa.getTitelKod());
        if (userRole != null) {
            return userRole;
        }

        // 4. Bestäm användarens roll utefter kombinationen befattningskod och gruppförskrivarkod
        userRole = lookupUserRoleByBefattningskodAndGruppforskrivarkod(sa.getTitelKod(), sa.getForskrivarkod());
        if (userRole != null) {
            return userRole;
        }

        // 6. Användaren är en vårdadministratör inom landstinget
        return UserRole.ROLE_VARDADMINISTRATOR;
    }

    /** Lookup user role by looking into 'legitimerade yrkesgrupper'.
     * Currently there are only two 'yrkesgrupper' to look for:
     * <ul>
     * <li>Läkare</li>
     * <li>Tandläkare</li>
     * </ul>
     *
     * @param legitimeradeYrkesgrupper string array with 'legitimerade yrkesgrupper'
     * @return a user role if valid 'yrkesgrupper', otherwise null
     */
    UserRole lookupUserRoleByLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        LOG.debug("  * legitimerade yrkesgrupper");
        if (legitimeradeYrkesgrupper == null || legitimeradeYrkesgrupper.size() == 0) {
            return null;
        }

        if (legitimeradeYrkesgrupper.contains(TITLE_LAKARE)) {
            return UserRole.ROLE_LAKARE;
        }

        if (legitimeradeYrkesgrupper.contains(TITLE_TANDLAKARE)) {
            return UserRole.ROLE_TANDLAKARE;
        }

        return null;
    }

    private UserRole lookupUserRoleByRequestURI(final UserRole userRole) {
        LOG.debug("  * request URI");

        LOG.debug("    getting current request from session...");
        DefaultSavedRequest savedRequest = getRequest();

        if (savedRequest != null && savedRequest.getRequestURI() != null) {
            String uri = savedRequest.getRequestURI();

            if (uri.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
                if (userRole.equals(UserRole.ROLE_LAKARE)) {
                    // Användaren är läkare som använder Webcert via djupintegration
                    return UserRole.ROLE_LAKARE_DJUPINTEGRERAD;
                } else if (userRole.equals(UserRole.ROLE_TANDLAKARE)) {
                    // Användaren är tandläkare som använder Webcert via djupintegration
                    return UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD;
                } else {
                    // Användaren är vårdadministratör som använder Webcert via djupintegration
                    return UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD;
                }
            }

            if (uri.matches(REGEXP_REQUESTURI_UTHOPP)) {
                if (userRole.equals(UserRole.ROLE_LAKARE)) {
                    // Användaren är läkare som använder Webcert via uthoppslänk.
                    return UserRole.ROLE_LAKARE_UTHOPP;
                } else if (userRole.equals(UserRole.ROLE_TANDLAKARE)) {
                    // Användaren är tandläkare som använder Webcert via uthoppslänk
                    return UserRole.ROLE_TANDLAKARE_UTHOPP;
                } else {
                    // Användaren är våradministratör som använder Webcert via uthoppslänk.
                    return UserRole.ROLE_VARDADMINISTRATOR_UTHOPP;
                }
            }
        }

        return userRole;
    }

    UserRole lookupUserRoleByBefattningskod(List<String> befattningsKoder) {
        LOG.debug("  * befattningskod");

        if (befattningsKoder == null || befattningsKoder.size() == 0) {
            return null;
        }

        if (befattningsKoder.contains(TITLECODE_AT_LAKARE)) {
            return UserRole.ROLE_LAKARE;
        }

        return null;
    }

    UserRole lookupUserRoleByBefattningskodAndGruppforskrivarkod(List<String> befattningsKoder, List<String> gruppforskrivarKoder) {
        // Create matrix
        for (String befattningskod : befattningsKoder) {
            for (String gruppforskrivarKod : gruppforskrivarKoder) {
                UserRole userRole = lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningskod, gruppforskrivarKod);
                if (userRole != null) {
                    return userRole;
                }
            }
        }

        return null;
    }

    UserRole lookupUserRoleByBefattningskodAndGruppforskrivarkod(String befattningsKod, String gruppforskrivarKod) {
        LOG.debug("  * befattningskod i kombination med gruppförskrivarkod");
        LOG.debug("    befattningskod = {}, gruppförskrivarkod = {}", befattningsKod, gruppforskrivarKod);

        if (befattningsKod == null || gruppforskrivarKod == null) {
            return null;
        }

        TitleCode titleCode = titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(befattningsKod, gruppforskrivarKod);
        if (titleCode == null) {
            LOG.debug("    kombinationen befattningskod and gruppförskrivarkod finns inte i databasen");
            return null;
        }

        Role role = titleCode.getRole();
        if (role == null) {
            throw new RuntimeException("titleCode.getRole() returnerade 'null' vilket indikerar att tabellen BEFATTNINGSKODER_ROLL i databasen har felaktig data");
        }

        return UserRole.valueOf(role.getName());
    }

    SakerhetstjanstAssertion getAssertion(Assertion assertion) {
        if (assertion == null) {
            throw new IllegalArgumentException("Assertion parameter cannot be null");
        }

        return new SakerhetstjanstAssertion(assertion);
    }

    HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }


    // - - - - - Private scope - - - - -

    private void assertAuthorizedVardgivare(String hsaId, List<Vardgivare> authorizedVardgivare) {
        LOG.debug("Assert user has authorization to one or more 'vårdenheter'");

        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare == null || authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(hsaId);
        }
    }

    private void assertMIU(SAMLCredential credential) {
        LOG.debug("Assert 'medarbetaruppdrag (MIU)'");

        // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
        if (!VARD_OCH_BEHANDLING.equals(getAssertion(credential).getMedarbetaruppdragType())) {
            throw new MissingMedarbetaruppdragException(getAssertion(credential).getHsaId());
        }
    }

    private WebCertUser createWebCertUser(String userRole, SAMLCredential credential, List<Vardgivare> authorizedVardgivare, List<GetHsaPersonHsaUserType> personInfo) {
        Role role = getRoleRepository().findByName(userRole);
        return createWebCertUser(role, credential,  authorizedVardgivare, personInfo);
    }

    private WebCertUser createWebCertUser(Role role, SAMLCredential credential, List<Vardgivare> authorizedVardgivare, List<GetHsaPersonHsaUserType> personInfo) {
        LOG.debug("Decorate/populate user object with additional information");

        SakerhetstjanstAssertion sa = getAssertion(credential);

        // Get user's privileges based on his/hers role
        final Map<String, UserRole> grantedRoles = roleToMap(getRoleAuthority(role));
        final Map<String, UserPrivilege> grantedPrivileges = getPrivilegeAuthorities(role);

        // Create the WebCert user object injection user's privileges
        WebCertUser webcertUser = new WebCertUser();

        webcertUser.setRoles(grantedRoles);
        webcertUser.setAuthorities(grantedPrivileges);

        webcertUser.setHsaId(sa.getHsaId());
        webcertUser.setNamn(compileName(sa.getFornamn(), sa.getMellanOchEfternamn()));
        webcertUser.setVardgivare(authorizedVardgivare);

        // Förskrivarkod is sensitiv information, not allowed to store real value
        webcertUser.setForskrivarkod("0000000");

        // Set user's authentication scheme
        webcertUser.setAuthenticationScheme(sa.getAuthenticationScheme());

        decorateWebCertUserWithAdditionalInfo(webcertUser, credential, personInfo);
        decorateWebCertUserWithAvailableFeatures(webcertUser);
        decorateWebCertUserWithAuthenticationMethod(webcertUser, credential);
        decorateWebCertUserWithDefaultVardenhet(webcertUser, credential);

        return webcertUser;
    }

    private void decorateWebCertUserWithAdditionalInfo(WebCertUser webcertUser, SAMLCredential credential, List<GetHsaPersonHsaUserType> hsaPersonInfo) {

        List<String> specialiseringar = extractSpecialiseringar(hsaPersonInfo);
        List<String> legitimeradeYrkesgrupper = extractLegitimeradeYrkesgrupper(hsaPersonInfo);
        String titel = extractTitel(hsaPersonInfo);

        webcertUser.setSpecialiseringar(specialiseringar);
        webcertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
        webcertUser.setTitel(titel);
    }

    private void decorateWebCertUserWithAuthenticationMethod(WebCertUser webcertUser, SAMLCredential credential) {
        String authenticationScheme = getAssertion(credential).getAuthenticationScheme();

        if (authenticationScheme.endsWith(":fake")) {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        } else {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser user, SAMLCredential credential) {

        // Get HSA id for the selected MIU
        String medarbetaruppdragHsaId = getAssertion(credential).getEnhetHsaId();

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

        return new ArrayList<>(lygSet);
    }

    private List<String> extractSpecialiseringar(List<GetHsaPersonHsaUserType> hsaUserTypes) {
        Set<String> specSet = new TreeSet<>();

        for (GetHsaPersonHsaUserType userType : hsaUserTypes) {
            if (userType.getSpecialityNames() != null) {
                List<String> specialityNames = userType.getSpecialityNames().getSpecialityName();
                specSet.addAll(specialityNames);
            }
        }

        return new ArrayList<>(specSet);
    }

    private String extractTitel(List<GetHsaPersonHsaUserType> hsaUserTypes) {
        List<String> titlar = new ArrayList<>();

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
