package se.inera.auth.eleg;

import org.springframework.security.saml.SAMLCredential;
import se.inera.webcert.hsa.model.AuthenticationMethod;

/**
 * Created by eriklupander on 2015-08-24.
 */
public interface ElegAuthenticationMethodResolver {

    AuthenticationMethod resolveAuthenticationMethod(SAMLCredential samlCredential);

}
