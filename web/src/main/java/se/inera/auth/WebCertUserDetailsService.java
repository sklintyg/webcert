package se.inera.auth;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import se.inera.auth.common.BaseWebCertUserDetailsService;
import se.inera.auth.exceptions.HsaServiceException;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.webcert.common.model.UserRoles;
import se.inera.webcert.dto.WebCertUser;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.webcert.service.monitoring.MonitoringLogService;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private RoleRepository roleRepository;


    public Object loadUserBySAML(SAMLCredential credential) {

        LOG.info("User authentication was successful. SAML credential is: {}", credential);

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());
        try {

            WebCertUser webCertUser = createWebCertUser(assertion);

            // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
            if (!VARD_OCH_BEHANDLING.equals(assertion.getMedarbetaruppdragType())) {
                throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
            }

            List<Vardgivare> authorizedVardgivare = hsaOrganizationsService.getAuthorizedEnheterForHosPerson(webCertUser.getHsaId());

            // if user does not have access to any vardgivare, we have to reject authentication
            if (authorizedVardgivare.isEmpty()) {
                throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
            }

            webCertUser.setVardgivare(authorizedVardgivare);
            setDefaultSelectedVardenhetOnUser(webCertUser, assertion);

            return webCertUser;
        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(assertion.getHsaId());
            throw e;
        } catch (Exception e) {
            LOG.error("Error building user {}, failed with message {}", assertion.getHsaId(), e.getMessage());
            throw new HsaServiceException(assertion.getHsaId(), e);
        }
    }

    // UTIL

    public final Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {

        // Decide user's role(s)
        String userRole = lookupUserRole(assertion);

        // Get user's privileges based on his/hers role
        final Collection<? extends GrantedAuthority> authorities = getAuthorities(Arrays.asList(roleRepository.findByName(userRole)));

        // Create the WebCert user object injection user's privileges
        WebCertUser webcertUser = new WebCertUser(authorities);

        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(compileName(assertion.getFornamn(), assertion.getMellanOchEfternamn()));

        // Förskrivarkod is sensitiv information, not allowed to store real value
        webcertUser.setForskrivarkod("0000000");

        // Get
        webcertUser.setAuthenticationScheme(assertion.getAuthenticationScheme());

        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare(assertion.getTitel().contains(LAKARE) || assertion.getTitelKod().contains(LAKARE_CODE));

        decorateWebCertUserWithAdditionalInfo(webcertUser);

        decorateWebCertUserWithAvailableFeatures(webcertUser);

        decoreateWithAuthenticationMethod(webcertUser, assertion.getAuthenticationScheme());

        return webcertUser;
    }

    private String lookupUserRole(SakerhetstjanstAssertion assertion) {
        return UserRoles.ROLE_LAKARE.name();
    }

    private void decoreateWithAuthenticationMethod(WebCertUser webcertUser, String authenticationScheme) {
        if (authenticationScheme.endsWith(":fake")) {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        } else {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
        }
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

//    private void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
//
//        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();
//
//        webcertUser.setAktivaFunktioner(availableFeatures);
//    }

    private String extractTitel(List<GetHsaPersonHsaUserType> hsaUserTypes) {

        List<String> titlar = new ArrayList<String>();

        for (GetHsaPersonHsaUserType userType : hsaUserTypes) {
            if (StringUtils.isNotBlank(userType.getTitle())) {
                titlar.add(userType.getTitle());
            }
        }

        return StringUtils.join(titlar, COMMA);
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

//    private String compileName(SakerhetstjanstAssertion assertion) {
//
//        StringBuilder sb = new StringBuilder();
//
//        if (StringUtils.isNotBlank(assertion.getFornamn())) {
//            sb.append(assertion.getFornamn());
//        }
//
//        if (StringUtils.isNotBlank(assertion.getMellanOchEfternamn())) {
//            if (sb.length() > 0) {
//                sb.append(SPACE);
//            }
//            sb.append(assertion.getMellanOchEfternamn());
//        }
//
//        return sb.toString();
//    }
//

    private List<String> getPrivileges(final Collection<Role> roles) {
        final List<String> privileges = new ArrayList<String>();
        final List<Privilege> collection = new ArrayList<Privilege>();
        for (final Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (final String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    private void setDefaultSelectedVardenhetOnUser(WebCertUser user, SakerhetstjanstAssertion assertion) {

        // Get HSA id for the selected MIU
        String medarbetaruppdragHsaId = assertion.getEnhetHsaId();

        boolean changeSuccess;

        if (StringUtils.isNotBlank(medarbetaruppdragHsaId)) {
            changeSuccess = user.changeValdVardenhet(medarbetaruppdragHsaId);
        } else {
            LOG.error("Assertion did not contain a medarbetaruppdrag, defaulting to use one of the Vardenheter present in the user");
            changeSuccess = setFirstVardenhetOnFirstVardgivareAsDefault(user);
        }

        if (!changeSuccess) {
            LOG.error("When logging in user '{}', unit with HSA-id {} could not be found in users MIUs", user.getHsaId(), medarbetaruppdragHsaId);
            throw new MissingMedarbetaruppdragException(user.getHsaId());
        }

        LOG.debug("Setting care unit '{}' as default unit on user '{}'", user.getValdVardenhet().getId(), user.getHsaId());
    }

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }

}
