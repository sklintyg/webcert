/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.infra.security.exception.MissingMedarbetaruppdragException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;


public class WebcertAuthenticationFailureHandler extends ExceptionMappingAuthenticationFailureHandler {

    private static final String BAD_CREDENTIALS = "badCredentials";
    private static final String HSA_SERVICE = "hsaService";
    private static final String MISSING_ASSIGNMENT = "missingAssignment";
    private static final String MISSING_SUBSCRIPTION = "missingSubscription";
    private static final String PRIVATE_PRACTITIONER_AUTH = "privatePractitionerAuthentication";

    private final Map<String, String> authExceptions = Map.of(
        BAD_CREDENTIALS, BadCredentialsException.class.getName(),
        HSA_SERVICE, HsaServiceException.class.getName(),
        MISSING_ASSIGNMENT, MissingMedarbetaruppdragException.class.getName(),
        MISSING_SUBSCRIPTION, MissingSubscriptionException.class.getName(),
        PRIVATE_PRACTITIONER_AUTH, PrivatePractitionerAuthorizationException.class.getName()
    );

    private static final String WC_IDENTIFIER = "wc";
    private static final String WC2_IDENTIFIER = "wc2";
    private static final String WC_DEFAULT_FAILURE_URL = "/error.jsp?reason=login.failed";
    private static final String WC2_DEFAULT_FAILURE_URL = "/error?reason=login.failed";

    private final String privatePractitionerPortalRegistrationUrl;
    private final Map<String, String> wcFailureUrls;
    private final Map<String, String> wc2FailureUrls;
    private final Map<String, Map<String, String>> failureUrlMap = new HashMap<>();

    @Autowired
    public WebcertAuthenticationFailureHandler(
        @Value("${privatepractitioner.portal.registration.url}") String privatePractitionerPortalRegistrationUrl) {
        this.privatePractitionerPortalRegistrationUrl = privatePractitionerPortalRegistrationUrl;
        this.wcFailureUrls = getWcFailureUrls();
        this.wc2FailureUrls = getWc2FailureUrls();
    }

    @PostConstruct
    public void init() {
        mapExceptions();
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException {
        final var failureUrlIdentifier = getFailureUrlIdentifier(request);
        final var exceptionName = exception.getClass().getName();

        String url;
        if (failureUrlMap.containsKey(exceptionName)) {
            url = failureUrlMap.get(exceptionName).get(failureUrlIdentifier);
        } else {
            saveException(request, exception);
            url = failureUrlIdentifier.equals(WC2_IDENTIFIER) ? WC2_DEFAULT_FAILURE_URL : WC_DEFAULT_FAILURE_URL;
        }

        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private void mapExceptions() {
        this.failureUrlMap.clear();
        for (Map.Entry<String, String> entry : authExceptions.entrySet()) {
            final var wcFailureUrl = wcFailureUrls.get(entry.getKey());
            final var wc2FailureUrl = wc2FailureUrls.get(entry.getKey());
            final var failureUrls = Map.of(
                WC_IDENTIFIER, wcFailureUrl != null ? wcFailureUrl : WC_DEFAULT_FAILURE_URL,
                WC2_IDENTIFIER, wc2FailureUrl != null ? wc2FailureUrl : WC2_DEFAULT_FAILURE_URL);
            this.failureUrlMap.put(entry.getValue(), failureUrls);
        }
    }

    private String getFailureUrlIdentifier(HttpServletRequest request) {
        final var requestUri = request.getRequestURI();
        final var servletPath = request.getServletPath();
        final var xForwardedHost = request.getHeader("x-forwarded-host");

        if ((requestUri != null && requestUri.endsWith("-wc2"))
            || (servletPath != null && servletPath.endsWith("-wc2"))
            || (xForwardedHost != null && xForwardedHost.startsWith("wc2."))) {
            return WC2_IDENTIFIER;
        }

        return WC_IDENTIFIER;
    }

    private Map<String, String> getWcFailureUrls() {
        return Map.of(
            BAD_CREDENTIALS, "/error.jsp?reason=login.failed",
            HSA_SERVICE, "/error.jsp?reason=login.hsaerror",
            MISSING_ASSIGNMENT, "/error.jsp?reason=login.medarbetaruppdrag",
            MISSING_SUBSCRIPTION, "/new-error.jsp?reason=auth-exception-subscription",
            PRIVATE_PRACTITIONER_AUTH, privatePractitionerPortalRegistrationUrl
        );
    }

    private Map<String, String> getWc2FailureUrls() {
        return Map.of(
            BAD_CREDENTIALS, "/error?reason=login.failed",
            HSA_SERVICE, "/error?reason=login.hsaerror",
            MISSING_ASSIGNMENT, "/error?reason=login.medarbetaruppdrag",
            MISSING_SUBSCRIPTION, "/error?reason=auth-exception-subscription",
            PRIVATE_PRACTITIONER_AUTH, privatePractitionerPortalRegistrationUrl
        );
    }
}
