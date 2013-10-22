package se.inera.auth;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String LAKARE = "Läkare";
    private static final String LAKARE_CODE = "204010";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        LOG.info("User authentication was successful. SAML credential is " + credential);

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());

        WebCertUser webCertUser = createWebCertUser(assertion);

        List<Vardgivare> authorizedVardgivare = hsaOrganizationsService.getAuthorizedEnheterForHosPerson(webCertUser.getHsaId());

        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
        }

        webCertUser.setVardgivare(authorizedVardgivare);
        return webCertUser;
    }

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(assertion.getFornamn() + " " + assertion.getMellanOchEfternamn());
        webcertUser.setForskrivarkod(assertion.getForskrivarkod());

        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare(LAKARE.equals(assertion.getTitel()) || LAKARE_CODE.equals(assertion.getTitelKod()));

        return webcertUser;
    }
}
