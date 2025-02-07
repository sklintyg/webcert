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
package se.inera.intyg.webcert.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.RequestCache;

@ExtendWith(MockitoExtension.class)
class WebCertUserOriginTest {

    @Mock
    private RequestCache requestCache;

    @InjectMocks
    private WebCertUserOrigin webcertUserOrigin;

    private static final String NORMAL = "NORMAL";
    private static final String DJUPINTEGRATION = "DJUPINTEGRATION";

    @Test
    void shouldMatchTheDeepIntegrationRegex() {
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSavedRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> webcertUserOrigin.resolveOrigin(null));
    }

    @Test
    void shouldReturnOriginNormalWhenNonMatchingUrl() {
        final var res = webcertUserOrigin.resolveOrigin(buildRequest("/non/matching/url"));
        assertEquals(NORMAL, res);
    }

    @Test
    void shouldReturnOriginNormalWhenRequestUrlIsNull() {
        final var res = webcertUserOrigin.resolveOrigin(buildRequest(null));
        assertEquals(NORMAL, res);
    }

    @Test
    void shouldReturnOriginDjupintegrationWhenMatchingRegex() {
        final var res = webcertUserOrigin.resolveOrigin(buildRequest("/visa/intyg/luse/99aaa4f1-d862-4750-a628-f7dcb9c8bac0"));
        assertEquals(DJUPINTEGRATION, res);
    }

    private HttpServletRequest buildRequest(String uri) {
        final var request = mock(HttpServletRequest.class);
        final var defaultSavedRequest = mock(DefaultSavedRequest.class);
        when(defaultSavedRequest.getRequestURI()).thenReturn(uri);
        when(requestCache.getRequest(any(HttpServletRequest.class), isNull())).thenReturn(defaultSavedRequest);
        return request;
    }

}
