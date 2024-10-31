/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.infra.security.exception.MissingMedarbetaruppdragException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;

@Service
public class CustomAuthenticationFailureHandler extends ExceptionMappingAuthenticationFailureHandler {

    private static final String WC_DEFAULT_FAILURE_URL = "/error?reason=login.failed";
    private final String privatePractitionerPortalRegistrationUrl;
    private  Map<String, String> failureUrlMap = new HashMap<>();

    public CustomAuthenticationFailureHandler(
        @Value("${privatepractitioner.portal.registration.url}") String privatePractitionerPortalRegistrationUrl) {
        this.privatePractitionerPortalRegistrationUrl = privatePractitionerPortalRegistrationUrl;
    }

    @PostConstruct
    public void init() {
        failureUrlMap = getFalureUrlMap();
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException {
        final var exceptionName = exception.getClass().getName();

        String url;
        if (failureUrlMap.containsKey(exceptionName)) {
            url = failureUrlMap.get(exceptionName);
        } else {
            saveException(request, exception);
            url = WC_DEFAULT_FAILURE_URL;
        }

        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private Map<String, String> getFalureUrlMap() {
        return Map.of(
            BadCredentialsException.class.getName(), "/error?reason=login.failed",
            HsaServiceException.class.getName(), "/error?reason=login.hsaerror",
            MissingMedarbetaruppdragException.class.getName(), "/error?reason=login.medarbetaruppdrag",
            MissingSubscriptionException.class.getName(), "/error?reason=auth-exception-subscription",
            PrivatePractitionerAuthorizationException.class.getName(), privatePractitionerPortalRegistrationUrl
        );
    }

}
