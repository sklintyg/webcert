package se.inera.auth;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import se.inera.auth.exceptions.HsaServiceException;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.monitoring.MonitoringLogService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String COMMA = ", ";
    private static final String SPACE = " ";

    private static final String LAKARE = "Läkare";
    private static final String LAKARE_CODE = "204010";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private WebcertFeatureService webcertFeatureService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
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

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {

        WebCertUser webcertUser = new WebCertUser();

        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(compileName(assertion));
        webcertUser.setForskrivarkod(assertion.getForskrivarkod());
        webcertUser.setAuthenticationScheme(assertion.getAuthenticationScheme());

        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare(LAKARE.equals(assertion.getTitel()) || LAKARE_CODE.equals(assertion.getTitelKod()));

        decorateWebCertUserWithAdditionalInfo(webcertUser);

        decorateWebCertUserWithAvailableFeatures(webcertUser);

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

    private void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {

        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();

        webcertUser.setAktivaFunktioner(availableFeatures);
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

    private String compileName(SakerhetstjanstAssertion assertion) {

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(assertion.getFornamn())) {
            sb.append(assertion.getFornamn());
        }

        if (StringUtils.isNotBlank(assertion.getMellanOchEfternamn())) {
            if (sb.length() > 0) {
                sb.append(SPACE);
            }
            sb.append(assertion.getMellanOchEfternamn());
        }

        return sb.toString();
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
