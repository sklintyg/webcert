package se.inera.intyg.webcert.web.auth.eleg;

import org.springframework.security.saml.SAMLCredential;

/**
 * Extracts SAML attributes for SAML tickets.
 *
 * Created by eriklupander on 2015-08-24.
 */
public interface ElegAuthenticationAttributeHelper {
    String getAttribute(SAMLCredential samlCredential, String attributeName);
}
