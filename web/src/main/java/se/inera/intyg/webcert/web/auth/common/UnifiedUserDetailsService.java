package se.inera.intyg.webcert.web.auth.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.web.security.WebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT;

/**
 * Created by eriklupander on 2015-08-12.
 *
 * Facade for our two different UserDetailsServices that looks up authenticated logins over either SITHS or e-leg.
 *
 * Checks the Authentication context class ref to determine method:
 *
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient - SITHS</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI - E-leg</li>
 */
@Service
public class UnifiedUserDetailsService implements SAMLUserDetailsService {

    /** User details service for e-leg authenticated private practitioners. */
    @Autowired
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    /** User details service for SITHS authenticated personnel. */
    @Autowired
    private WebCertUserDetailsService webCertUserDetailsService;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
        if (samlCredential.getAuthenticationAssertion() == null) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. Must contain an authenticationAssertion");
        }

        String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        if (authnContextClassRef == null || authnContextClassRef.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. "
                    + "AuthenticationContextClassRef was null or empty. Should be one of:\n"
                    + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT + "\n"
                    + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI);
        }

        switch (authnContextClassRef) {
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI:
                return elegWebCertUserDetailsService.loadUserBySAML(samlCredential);
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT:
                return webCertUserDetailsService.loadUserBySAML(samlCredential);
            default:
                throw new IllegalArgumentException("AuthorizationContextClassRef was " + authnContextClassRef + ", expected one of: "
                        + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT + "\n"
                        + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI);
        }

    }
}
