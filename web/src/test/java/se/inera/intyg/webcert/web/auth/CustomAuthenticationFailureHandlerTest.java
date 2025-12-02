/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.infra.security.exception.MissingMedarbetaruppdragException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private RedirectStrategy redirectStrategy;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAuthenticationFailureHandler handler = new CustomAuthenticationFailureHandler("/ppRegistrationUrl");

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    private static final int ONE = 1;
    private static final BadCredentialsException BAD_CREDENTIALS = new BadCredentialsException("Bad credentials exception");
    private static final HsaServiceException HSA_SERVICE = new HsaServiceException("Hsa service exception", new Exception());
    private static final RememberMeAuthenticationException OTHER_AUTH_EXCEPTION = new RememberMeAuthenticationException("Other exception");
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
    void shouldRedirectToLoginFailed() throws IOException {
        handler.onAuthenticationFailure(request, response, BAD_CREDENTIALS);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.failed", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToHsaError() throws IOException {
        handler.onAuthenticationFailure(request, response, HSA_SERVICE);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.hsaerror", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToLoginMedarbetaruppdragError() throws IOException {
        handler.onAuthenticationFailure(request, response, MISSING_ASSIGNMENT);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.medarbetaruppdrag", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToSubscriptionError() throws IOException {
        handler.onAuthenticationFailure(request, response, MISSING_SUBSCRIPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=auth-exception-subscription", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToPPRegistration() throws IOException {
        handler.onAuthenticationFailure(request, response, PP_AUTHORIZATION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/ppRegistrationUrl", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToDefaultError() throws IOException {
        when(request.getSession(false)).thenReturn(new MockHttpSession());
        when(request.getSession()).thenReturn(new MockHttpSession());
        handler.onAuthenticationFailure(request, response, OTHER_AUTH_EXCEPTION);

        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=login.failed", urlCaptor.getValue());
    }

    @Test
    void shouldRedirectToMissingSubscriptionFromSamlException() throws IOException {
        handler.onAuthenticationFailure(request, response, new Saml2AuthenticationException(new Saml2Error("auth-exception-subscription", "Authentication error"), "Private practitioner was denied access to Webcert due to missing subscription.", MISSING_SUBSCRIPTION));
        verify(redirectStrategy, times(ONE)).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertEquals("/error?reason=auth-exception-subscription", urlCaptor.getValue());
    }

}
