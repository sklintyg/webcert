package se.inera.auth;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author andreaskaltenbach
 */
@Service
public class WebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String LAKARE = "Läkare";
    private static final String LAKARE_CODE = "204010";

    private static final String SPRING_SECURITY_SAVED_REQUEST_ATTR = "SPRING_SECURITY_SAVED_REQUEST";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    private SakerhetstjanstAssertion assertion;


    // - - - - - Public scope - - - - -

    public Object loadUserBySAML(SAMLCredential credential) {

        LOG.info("User authentication was successful. SAML credential is: {}", credential);

        assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());

        try {
            DefaultSavedRequest savedRequest = getSavedRequest();

            // Create the user
            WebCertUser webCertUser = createUser(lookupUserRole(savedRequest));
            return webCertUser;

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", assertion.getHsaId(), e.getMessage());
            throw new RuntimeException(assertion.getHsaId(), e);
        }
    }

    // Magnus: Detta ser ut att funka! Refactor and then remove this comment... ;)
    private DefaultSavedRequest getSavedRequest() {
        HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) curRequest.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_ATTR);
        return savedRequest;
    }


    // - - - - - Protected scope - - - - -

    @Override
    protected WebCertUser createUser(String userRole) {

        List<Vardgivare> authorizedVardgivare = null;

        try {
            authorizedVardgivare = hsaOrganizationsService.getAuthorizedEnheterForHosPerson(assertion.getHsaId());

        } catch (Exception e) {
            LOG.error("Failed retrieving authorized units for user {}, error message {}", assertion.getHsaId(), e.getMessage());
            throw new HsaServiceException(assertion.getHsaId(), e);
        }

        try {
            assertMIU();
            assertAuthorizedVardgivare(authorizedVardgivare);

            WebCertUser webCertUser = createWebCertUser(userRole, authorizedVardgivare);
            return webCertUser;

        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(assertion.getHsaId());
            throw e;
        }

    }


    // - - - - - Private scope - - - - -

    private void assertAuthorizedVardgivare(List<Vardgivare> authorizedVardgivare) {
        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(assertion.getHsaId());
        }
    }

    private void assertMIU() {
        // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
        if (!VARD_OCH_BEHANDLING.equals(assertion.getMedarbetaruppdragType())) {
            throw new MissingMedarbetaruppdragException(assertion.getHsaId());
        }
    }

    private WebCertUser createWebCertUser(String userRole, List<Vardgivare> authorizedVardgivare) {
        return createWebCertUser(getRoleRepository().findByName(userRole), authorizedVardgivare);
    }

    private WebCertUser createWebCertUser(Role role, List<Vardgivare> authorizedVardgivare) {

        // Get user's privileges based on his/hers role
        final GrantedAuthority grantedRole = getRoleAuthority(role);
        final Collection<? extends GrantedAuthority> grantedPrivileges = getPrivilegeAuthorities(role);

        // Create the WebCert user object injection user's privileges
        WebCertUser webcertUser = new WebCertUser(grantedRole, grantedPrivileges);

        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(compileName(assertion.getFornamn(), assertion.getMellanOchEfternamn()));
        webcertUser.setVardgivare(authorizedVardgivare);

        // Förskrivarkod is sensitiv information, not allowed to store real value
        webcertUser.setForskrivarkod("0000000");

        // Set user's authentication scheme
        webcertUser.setAuthenticationScheme(assertion.getAuthenticationScheme());

        decorateWebCertUserWithAdditionalInfo(webcertUser);
        decorateWebCertUserWithAvailableFeatures(webcertUser);
        decorateWebCertUserWithAuthenticationMethod(webcertUser);
        decorateWebCertUserWithDefaultVardenhet(webcertUser);

        return webcertUser;
    }

    private void decorateWebCertUserWithAdditionalInfo(WebCertUser webcertUser) {

        String userHsaId = webcertUser.getHsaId();

        List<GetHsaPersonHsaUserType> hsaPersonInfo = hsaPersonService.getHsaPersonInfo(userHsaId);

        if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
            LOG.info("getHsaPersonInfo did not return any info for user '{}'", userHsaId);
            return;
        }

        List<String> specialiseringar = extractSpecialiseringar(hsaPersonInfo);
        webcertUser.setSpecialiseringar(specialiseringar);

        List<String> legitimeradeYrkesgrupper = extractLegitimeradeYrkesgrupper(hsaPersonInfo);
        webcertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);

        String titel = extractTitel(hsaPersonInfo);
        webcertUser.setTitel(titel);
    }

    private void decorateWebCertUserWithAuthenticationMethod(WebCertUser webcertUser) {
        String authenticationScheme = assertion.getAuthenticationScheme();

        if (authenticationScheme.endsWith(":fake")) {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        } else {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser user) {

        // Get HSA id for the selected MIU
        String medarbetaruppdragHsaId = assertion.getEnhetHsaId();

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

    private String lookupUserRole(DefaultSavedRequest savedRequest) {

        // Use the request URI to decide if this is a 'djupintegration' or 'uthopp' user
        if (savedRequest != null && savedRequest.getRequestURI() != null) {
            String uri = savedRequest.getRequestURI();

            if (uri.matches("/certificate/*/questions")) {
                return UserRole.ROLE_LAKARE_UTHOPP.name();
            }

            if (uri.matches("/intyg/*")) {
                return UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name();
            }
        }

        // lakare flag is calculated by checking for lakare profession in title and title code
        if (assertion.getTitel().contains(LAKARE) || assertion.getTitelKod().contains(LAKARE_CODE)) {
            return UserRole.ROLE_LAKARE.name();
        }

        return UserRole.ROLE_VARDADMINISTRATOR.name();
    }


    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }

}
