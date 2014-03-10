package se.inera.auth;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.webcert.hsa.model.Specialisering;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String LAKARE = "Läkare";
    private static final String LAKARE_CODE = "204010";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;
    
    @Autowired
    private HsaPersonService hsaPersonService;

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        LOG.info("User authentication was successful. SAML credential is " + credential);

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());

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
    }

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(assertion.getFornamn() + " " + assertion.getMellanOchEfternamn());
        webcertUser.setForskrivarkod(assertion.getForskrivarkod());
        webcertUser.setAuthenticationScheme(assertion.getAuthenticationScheme());
    
        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare(LAKARE.equals(assertion.getTitel()) || LAKARE_CODE.equals(assertion.getTitelKod()));
        
        List<Specialisering> specialities = hsaPersonService.getSpecialitiesForHsaPerson(assertion.getHsaId());
        webcertUser.setSpecialiseringar(specialities);
                
        return webcertUser;
    }

    private void setDefaultSelectedVardenhetOnUser(WebCertUser user, SakerhetstjanstAssertion assertion) {
        
        String medarbetaruppdragHsaId = assertion.getMedarbetaruppdragHsaId();
        
        boolean changeSuccess = false;
        
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
