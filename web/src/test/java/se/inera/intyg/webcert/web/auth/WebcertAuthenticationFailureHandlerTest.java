/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.infra.security.exception.MissingMedarbetaruppdragException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;

@ExtendWith(MockitoExtension.class)
class WebcertAuthenticationFailureHandlerTest {

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private WebcertAuthenticationFailureHandler handler = new WebcertAuthenticationFailureHandler("/ppRegistrationUrl");

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    private static final int ONE = 1;

    private static final String WC = "wc";
    private static final String WC2 = "wc2";
    private static final String REQUEST_URI = "requestUri";
    private static final String SERVLET_PATH = "servletPath";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";

    private static final Map<String, Map<String, String>> REDIRECTS = Map.of(
        "requestUri", Map.of("wc", "https://wc.localtest.me", "wc2", "https://wc2.wc.localtest.me/siths-wc2"),
        "servletPath", Map.of("wc", "https://wc.localtest.me", "wc2", "https://wc2.wc.localtest.me/siths-wc2"),
        "x-forwarded-host", Map.of("wc", "wc.localtest.me", "wc2", "wc2.wc.localtest.me"));

    private static final BadCredentialsException BAD_CREDENTIALS = new BadCredentialsException("Bad credentials exception");
    private static final HsaServiceException HSA_SERVICE = new HsaServiceException("Hsa service exception", new Exception());
    private static final RememberMeAuthenticationException OTHER_AUTH_EXCEPTION = new  RememberMeAuthenticationException("Other exception");
    private static final MissingMedarbetaruppdragException MISSING_ASSIGNMENT =
        new MissingMedarbetaruppdragException("Missing assignment exception");
    private static final MissingSubscriptionException MISSING_SUBSCRIPTION =
        new MissingSubscriptionException("Missing subscription exception");
    private static final PrivatePractitionerAuthorizationException PP_AUTHORIZATION =
        new PrivatePractitionerAuthorizationException("Private practitioner exception");

    @BeforeEach
    public void setup() {
        handler.init();
    }

    @Test
    public void shouldRedirectToAngularClientLoginFailed() throws IOException {
        final var request = getRequest(Collections.emptyMap());
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, BAD_CREDENTIALS);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error.jsp?reason=login.failed", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToAngularClientLoginHsaError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC, SERVLET_PATH, WC, X_FORWARDED_HOST, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, HSA_SERVICE);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error.jsp?reason=login.hsaerror", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToAngularClientLoginMedarbetaruppdragError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, MISSING_ASSIGNMENT);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error.jsp?reason=login.medarbetaruppdrag", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToAngularClientSubscriptionError() throws IOException {
        final var request = getRequest(Map.of(SERVLET_PATH, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, MISSING_SUBSCRIPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/new-error.jsp?reason=auth-exception-subscription", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToPPRegistrationWhenRequestFromAngular() throws IOException {
        final var request = getRequest(Map.of(X_FORWARDED_HOST, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, PP_AUTHORIZATION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/ppRegistrationUrl", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToAngularClientDefaultError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC, X_FORWARDED_HOST, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, OTHER_AUTH_EXCEPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error.jsp?reason=login.failed", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToReactClientLoginFailed() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC2));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, BAD_CREDENTIALS);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.failed", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToReactClientLoginHsaError() throws IOException {
        final var request = getRequest(Map.of(SERVLET_PATH, WC2));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, HSA_SERVICE);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.hsaerror", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToReactClientLoginMedarbetaruppdragError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC2, SERVLET_PATH, WC, X_FORWARDED_HOST, WC));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, MISSING_ASSIGNMENT);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.medarbetaruppdrag", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToReactClientSubscriptionError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC2, SERVLET_PATH, WC2, X_FORWARDED_HOST, WC2));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, MISSING_SUBSCRIPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=auth-exception-subscription", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToPPRegistrationWhenRequestFromReact() throws IOException {
        final var request = getRequest(Map.of(X_FORWARDED_HOST, WC2));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, PP_AUTHORIZATION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/ppRegistrationUrl", urlCaptor.getValue());
    }

    @Test
    public void shouldRedirectToReactClientDefaultError() throws IOException {
        final var request = getRequest(Map.of(REQUEST_URI, WC, X_FORWARDED_HOST, WC2));
        final var response = getResponse();

        handler.onAuthenticationFailure(request, response, OTHER_AUTH_EXCEPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.failed", urlCaptor.getValue());
    }

    private MockHttpServletRequest getRequest(Map<String, String> properties) {
        final var request = new MockHttpServletRequest();
        setProperties(request, properties);
        return request;
    }

    private HttpServletResponse getResponse() {
        return new MockHttpServletResponse();
    }

    private void setProperties(MockHttpServletRequest request, Map<String, String> properties) {
        for (final var entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case REQUEST_URI:
                    request.setRequestURI(REDIRECTS.get(REQUEST_URI).get(entry.getValue()));
                    break;
                case SERVLET_PATH:
                    request.setServletPath(REDIRECTS.get(SERVLET_PATH).get(entry.getValue()));
                    break;
                case X_FORWARDED_HOST:
                    request.addHeader(X_FORWARDED_HOST, REDIRECTS.get(X_FORWARDED_HOST).get(entry.getValue()));
                    break;
            }
        }
    }
}
