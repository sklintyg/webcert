package se.inera.intyg.webcert.web.auth.oidc.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.ArrayList;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private WebcertUserDetailsService webcertUserDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Object principal = webcertUserDetailsService.loadUserByHsaId(jwtAuthenticationToken.getUserHsaId());
        if (principal != null) {
            WebCertUser webCertUser = new WebCertUser((IntygUser) principal);

            // Force origin DJUPINTEGRATION always
            webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());

            ExpiringUsernameAuthenticationToken result = new ExpiringUsernameAuthenticationToken(null, webCertUser, jwtAuthenticationToken,
                    new ArrayList<>());
            result.setDetails(webCertUser);
            return result;
        }
        throw new AuthenticationServiceException("User principal returned from UserDetailsService was not of type WebCertUser,"
                + " throwing exception.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Autowired
    public void setWebcertUserDetailsService(WebcertUserDetailsService webcertUserDetailsService) {
        this.webcertUserDetailsService = webcertUserDetailsService;
    }
}
