package se.inera.auth.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import se.inera.webcert.security.WebCertUserDetailsService;
import se.inera.auth.eleg.ElegWebCertUserDetailsService;

/**
 * Created by eriklupander on 2015-08-12.
 *
 * Facade for our two different UserDetailsServices that looks up authenticated logins over either SITHS or ELEG.
 */
@Service
public class UnifiedUserDetailsService implements SAMLUserDetailsService {

    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT = "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";

    /** User details service for e-leg authenticated private practitioners */
    @Autowired
    ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    /** User details service for SITHS authenticated personnel */
    @Autowired
    WebCertUserDetailsService webCertUserDetailsService;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
        if (samlCredential.getAuthenticationAssertion() == null) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. Must contain an authenticationAssertion");
        }

        String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        if (authnContextClassRef == null || authnContextClassRef.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. " +
                    "AuthenticationContextClassRef was null or empty. Should be one of:\n"+
                    URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT + "\n" +
                    URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI);
        }

        if (authnContextClassRef.equals(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI)) {
            return elegWebCertUserDetailsService.loadUserBySAML(samlCredential);
        } else if(authnContextClassRef.equals(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT)) {
            return webCertUserDetailsService.loadUserBySAML(samlCredential);
        } else {
            throw new IllegalArgumentException("AuthorizationContextClassRef was " + authnContextClassRef + ", expected one of: " +
                    URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT + "\n" +
                    URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI);
        }

    }
}
