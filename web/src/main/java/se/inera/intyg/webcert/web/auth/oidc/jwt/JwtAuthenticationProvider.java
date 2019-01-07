/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * JWT based authentication provider. Given that the supplied {@link org.springframework.security.core.Authentication}
 * is supported, the standard Webcert UserDetailsService is used to check user authorization and build the user principal.
 *
 * @author eriklupander
 */
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
