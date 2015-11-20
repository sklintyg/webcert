package se.inera.intyg.webcert.web.auth.eleg;

import org.springframework.security.saml.SAMLCredential;
import se.inera.webcert.hsa.model.AuthenticationMethod;

/**
 * Resolves {@link se.inera.webcert.hsa.model.AuthenticationMethod} used for a e-leg authentication.
 *
 * E.g. NetID, BankID or Mobilt BankID.
 *
 * Created by eriklupander on 2015-08-24.
 */
public interface ElegAuthenticationMethodResolver {

    AuthenticationMethod resolveAuthenticationMethod(SAMLCredential samlCredential);

}
