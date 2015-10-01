package se.inera.webcert.security;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

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
import se.inera.webcert.persistence.roles.model.TitleCode;
import se.inera.webcert.persistence.roles.repository.TitleCodeRepository;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.user.dto.WebCertUser;

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

    private static final String IMAGINARY_GROUPPRESCRIPTIONCODE = "0000000";


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

    // private SAMLCredential credential;
    // private SakerhetstjanstAssertion assertion;
    // private List<Vardgivare> authorizedVardgivare;
    // private List<GetHsaPersonHsaUserType> hsaPersonInfo;
    // private DefaultSavedRequest savedRequest;

    // - - - - - Public scope - - - - -

    public Object loadUserBySAML(SAMLCredential credential) {

        if (credential == null) {
            throw new RuntimeException("SAMLCredential has not been set.");
        }

        LOG.info("Loading user...");
        LOG.info("SAML credential is: {}", credential);


        try {
            // Get the current request
            //savedRequest = getCurrentRequest();

            //assertion = getAssertion(credential);
            //authorizedVardgivare = getAuthorizedVardgivare(getAssertion(credential).getHsaId());
            //hsaPersonInfo = getPersonInfo(getAssertion(credential).getHsaId());

            // Create the user
            WebCertUser webCertUser = createUser(credential);

            LOG.info("User authentication was successful");
            return webCertUser;

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", getAssertion(credential).getHsaId(), e.getMessage());
            throw new RuntimeException(getAssertion(credential).getHsaId(), e);
        }
    }

    // - - - - - Protected scope - - - - -

    protected SakerhetstjanstAssertion getAssertion(SAMLCredential credential) {
        //if (assertion == null) {
            return getAssertion(credential.getAuthenticationAssertion());
       // }

       // return assertion;
    }

    protected List<Vardgivare> getAuthorizedVardgivare(String hsaId) {

        try {
            return hsaOrganizationsService.getAuthorizedEnheterForHosPerson(hsaId);

        } catch (Exception e) {
            LOG.error("Failed retrieving authorized units from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }
    }

    protected List<GetHsaPersonHsaUserType> getPersonInfo(String hsaId) {
        List<GetHsaPersonHsaUserType> hsaPersonInfo = null;
            try {
                hsaPersonInfo = hsaPersonService.getHsaPersonInfo(hsaId);
                if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
                    LOG.info("getHsaPersonInfo did not return any info for user '{}'", hsaId);
                }

            } catch (Exception e) {
                LOG.error("Failed retrieving user information from HSA for user {}, error message {}", hsaId, e.getMessage());
                throw new HsaServiceException(hsaId, e);
            }


        return hsaPersonInfo;
    }


    // - - - - - Package scope - - - - -

    WebCertUser createUser(SAMLCredential credential) {
        String hsaId = getAssertion(credential).getHsaId();
        List<GetHsaPersonHsaUserType> personInfo = getPersonInfo(hsaId);
        List<Vardgivare> authorizedVardgivare = getAuthorizedVardgivare(hsaId);
        try {
            assertMIU(credential);
            assertAuthorizedVardgivare(hsaId, authorizedVardgivare);

            WebCertUser webCertUser = createWebCertUser(lookupUserRole(credential, personInfo), credential, authorizedVardgivare, personInfo);
            return webCertUser;

        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(getAssertion(credential).getHsaId());
            throw e;
        }

    }

    String getGroupPrescriptionCode() {
        // TODO create some intelligent logic to get user's group prescription code
        return "9300005";
    }

    String lookupUserRole(SAMLCredential credential, List<GetHsaPersonHsaUserType> personInfo) {
        SakerhetstjanstAssertion sa = getAssertion(credential);

        // 1. Kolla yrkesgrupper och se vilken roll som användaren ska ha.
        //    Görs mha hsaPersonInfo
        // ?? vad händer om det är mer än en yrkersgrupp
        List<String> legitimeradeYrkesgrupper = extractLegitimeradeYrkesgrupper(personInfo);
        UserRole userRole = lookupUserRoleByLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);

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
            userRole = lookupUserRoleByBefattningskod(sa.getTitelKod());
        }

        // Lookup user role by doctors title
        if (userRole == null) {
            userRole = lookupUserRoleByTitel(sa.getTitel());
        }

        boolean doctor = UserRole.ROLE_LAKARE.equals(userRole);

        // Use the request URI to decide if this is a 'djupintegration' or 'uthopp' user
        DefaultSavedRequest savedRequest = getCurrentRequest();
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

        if (doctor) {
            return UserRole.ROLE_LAKARE.name();
        }

        // Default to the 'Vårdadministratör' role
        return UserRole.ROLE_VARDADMINISTRATOR.name();
    }

    UserRole lookupUserRoleByTitel(List<String> titel) {
        if (titel == null || titel.size() == 0) {
            return null;
        }

        if (titel.contains(LAKARE)) {
            return UserRole.ROLE_LAKARE;
        }

        return null;
    }

    UserRole lookupUserRoleByBefattningskod(List<String> befattningsKoder) {
        String befattningsKod = null;

        if (befattningsKoder == null || befattningsKoder.size() == 0) {
            return null;
        }

        if (befattningsKoder.size() > 1) {
            // Ett problem som vi har i Pascal är att om en användare har dubbla legitimationer
            // (vilket förekommer), t ex SSK och AT-läkare. Då kommer det 2 befattningskoder
            // i biljetten, men då litar vi inte på biljetten utan gör en ny slagning mot HSA.

            //befattningsKod = LAKARE_KOD_203090;  // This is just for test

            // For now, just return the first title code
            // TODO Call HSA to decide users title code
            //befattningsKod = befattningsKoder.get(0);
            return lookupUserRoleByBefattningskod(befattningsKoder.get(0));

        } else {
            return lookupUserRoleByBefattningskod(befattningsKoder.get(0));
        }

    }

    UserRole lookupUserRoleByBefattningskod(String befattningsKod) {
        if (befattningsKod == null || befattningsKod.equals("")) {
            return null;
        }

        if (befattningsKod.equals(LAKARE_KOD_204010)) {
            return UserRole.ROLE_LAKARE;
        }

        // We cannot decide user's role yet, create another lookup
        // based on title code and group prescription code
        String gruppforskrivarKod = getGroupPrescriptionCode();
        return lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningsKod, gruppforskrivarKod);
    }

    UserRole lookupUserRoleByBefattningskodAndGruppforskrivarkod(String befattningsKod, String gruppforskrivarKod) {
        if (befattningsKod == null || gruppforskrivarKod == null) {
            return null;
        }

        TitleCode titleCode = titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(befattningsKod, gruppforskrivarKod);
        if (titleCode != null) {
            Role role = titleCode.getRole();
            return UserRole.valueOf(role.getName());
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
            return (DefaultSavedRequest) curRequest.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);

    }


    // - - - - - Private scope - - - - -

    private void assertAuthorizedVardgivare(String hsaId, List<Vardgivare> authorizedVardgivare) {

        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(hsaId);
        }
    }

    private void assertMIU(SAMLCredential credential) {
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
