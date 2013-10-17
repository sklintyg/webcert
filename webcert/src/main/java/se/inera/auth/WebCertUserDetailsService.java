package se.inera.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        LOG.info("User authentication was successful. SAML credential is " + credential);

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());

        WebCertUser webCertUser = createWebCertUser(assertion);

        webCertUser.setVardgivare(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(webCertUser.getHsaId()));

        return webCertUser;
    }

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(assertion.getFornamn() + assertion.getMellanOchEfternamn());
        webcertUser.setForskrivarkod(assertion.getForskrivarkod());

        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare("Läkare".equals(assertion.getTitel()) || "204010".equals(assertion.getTitelKod()));

        return webcertUser;
    }
}
