package se.inera.intyg.webcert.web.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import se.inera.intyg.webcert.web.auth.common.AuthConstants;

/**
 * Custom SAMLEntryPoint for Webcert that overrides the generation of AuthnContexts based on metadata alias:
 *
 * For SITHS (defaultAlias), we only supply the {@link AuthConstants#URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT}
 *
 * For ELEG  (eleg), we do not specify anything. This is due to SSO problems at CGIs end when a previously authenticated
 * identity is validated against the IdP to access another system in the same federation.
 *
 * Created by eriklupander on 2015-11-24.
 */
public class WebcertSAMLEntryPoint extends SAMLEntryPoint {

    /**
     * Override from superclass, see class comment for details.
     *
     * @param context   containing local entity
     * @param exception exception causing invocation of this entry point (can be null)
     * @return populated webSSOprofile
     * @throws MetadataProviderException in case metadata loading fails
     */
    protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) throws MetadataProviderException {

        WebSSOProfileOptions ssoProfileOptions;
        if (defaultOptions != null) {
            ssoProfileOptions = defaultOptions.clone();

            if (context.getLocalExtendedMetadata().getAlias().equals(AuthConstants.ALIAS_ELEG)) {
                ssoProfileOptions.setAuthnContexts(new HashSet<>());
            } else if (context.getLocalExtendedMetadata().getAlias().equals(AuthConstants.ALIAS_SITHS)) {
                ssoProfileOptions.setAuthnContexts(buildTlsClientAuthContexts());
            }
        } else {
            ssoProfileOptions = new WebSSOProfileOptions();
        }

        return ssoProfileOptions;
    }

    private Collection<String> buildTlsClientAuthContexts() {
        Set<String> set = new HashSet<>();
        set.add(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT);
        return set;
    }
}
