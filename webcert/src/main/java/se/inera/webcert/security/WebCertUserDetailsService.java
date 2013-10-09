package se.inera.webcert.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        LOG.info("User authentication was successful. SAML credential is " + credential);

        //TODO - fetch medarbetaruppdrag from HSA to create WebCertUser

        return new WebCertUser();
    }
}
