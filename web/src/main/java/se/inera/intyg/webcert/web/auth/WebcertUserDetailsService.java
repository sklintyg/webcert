package se.inera.intyg.webcert.web.auth;

import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.IntygUser;
import se.inera.intyg.common.security.siths.BaseUserDetailsService;
import se.inera.intyg.common.security.siths.DefaultUserDetailsHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * As each application shall implement its own UserDetailsService, we simply extend the base one and implement all the
 * abstract methods.
 *
 * Please note that we typically can use the {@link DefaultUserDetailsHelper} for getting access to standard implementations.
 *
 * Created by eriklupander on 2016-05-17.
 */
@Service(value = "webcertUserDetailsService")
public class WebcertUserDetailsService extends BaseUserDetailsService {

    @Override
    protected IntygUser createUser(SAMLCredential credential) {
        IntygUser user = super.createUser(credential);
        return new WebCertUser(user);
    }

    @Override
    protected String getDefaultRole() {
        return AuthoritiesConstants.ROLE_ADMIN;
    }
}
