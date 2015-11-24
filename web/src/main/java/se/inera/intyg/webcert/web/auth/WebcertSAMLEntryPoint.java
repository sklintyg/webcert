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
 * Created by eriklupander on 2015-11-24.
 */
public class WebcertSAMLEntryPoint extends SAMLEntryPoint {

    /**
     * Method is supposed to populate preferences used to construct the SAML message. Method can be overridden to provide
     * logic appropriate for given application. In case defaultOptions object was set it will be used as basis for construction
     * and request specific values will be update (idp field).
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

            if (context.getLocalExtendedMetadata().getAlias().equals("eleg")) {
                ssoProfileOptions.setAuthnContexts(buildSoftwarePki());
            } else if (context.getLocalExtendedMetadata().getAlias().equals("defaultAlias")) {
                ssoProfileOptions.setAuthnContexts(buildTlsClient());
            }
        } else {
            ssoProfileOptions = new WebSSOProfileOptions();
        }

        return ssoProfileOptions;
    }

    private Collection<String> buildSoftwarePki() {
        Set<String> set = new HashSet<>();
        set.add(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI);
        return set;
    }

    private Collection<String> buildTlsClient() {
        Set<String> set = new HashSet<>();
        set.add(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT);
        return set;
    }
}
