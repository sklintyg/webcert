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
package se.inera.intyg.webcert.web.web.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.WebCertUserServiceImpl;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class UnitSelectedAssuranceFilterTest {

    private static final String IGNORED_URL = "/test";
    private static final String REQUEST_URL = "/api/certificate";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain filterChain;

    @Mock
    private WebCertUserServiceImpl webCertUserService;

    @InjectMocks
    private UnitSelectedAssuranceFilter filter = new UnitSelectedAssuranceFilter();

    @BeforeEach
    void setup() throws ServletException {
        filter.setIgnoredUrls(IGNORED_URL);
        filter.initFilterBean();
    }

    @Test
    void testInitiateFilterSettingIgnoredUrls() throws ServletException {
        filter.initFilterBean();
        assertNotNull(filter.getIgnoredUrls());
        assertEquals(IGNORED_URL, filter.getIgnoredUrls());
    }

    @Test
    void testUserWithSelectedUnitIsLoggedIn() throws ServletException, IOException {
        final var user = createUser();
        when(httpServletRequest.getRequestURI()).thenReturn(REQUEST_URL);
        when(webCertUserService.hasAuthenticationContext()).thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(user);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        assertNotNull(user);
        assertNotNull(user.getValdVardenhet());
        assertEquals(REQUEST_URL, httpServletRequest.getRequestURI());
        verify(httpServletResponse, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void testUserWithoutSelectedCareUnitIsLoggedIn() throws ServletException, IOException {
        final var user = new WebCertUser();
        when(httpServletRequest.getRequestURI()).thenReturn(REQUEST_URL);
        when(webCertUserService.hasAuthenticationContext()).thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(user);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(webCertUserService).hasAuthenticationContext();
        verify(webCertUserService).getUser();
        assertNotNull(user);
        assertNull(user.getValdVardenhet());
        assertNotEquals(IGNORED_URL, httpServletRequest.getRequestURI());
        verify(filterChain, never()).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void testIgnoredUrlsShouldNotGiveErrors() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn(IGNORED_URL);
        when(webCertUserService.hasAuthenticationContext()).thenReturn(false);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        assertNotNull(filter.getIgnoredUrls());
        verify(httpServletResponse, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldNotGiveErrorsIfUnauthorizedPrivatePractitioner() throws ServletException, IOException {
        final var user = mock(WebCertUser.class);
        when(httpServletRequest.getRequestURI()).thenReturn(REQUEST_URL);
        when(webCertUserService.hasAuthenticationContext()).thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(user);
        when(user.isUnauthorizedPrivatePractitioner()).thenReturn(true);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private WebCertUser createUser() {
        final var careGiver = new Vardgivare("cgId", "cgName");
        final var careUnit = new Vardenhet("cuId", "cuName");
        careGiver.setVardenheter(List.of(careUnit));

        final var user = new WebCertUser();
        user.setValdVardgivare(careGiver);
        user.setValdVardenhet(careUnit);

        return user;
    }
}
