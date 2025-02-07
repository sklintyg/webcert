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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.WebCertUserServiceImpl;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class UnitSelectedAssuranceFilterTest {

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

    @Before
    public void setup() throws ServletException {
        filter.setIgnoredUrls(IGNORED_URL);
        filter.initFilterBean();
    }

    @Test
    public void testInitiateFilterSettingIgnoredUrls() throws ServletException {
        filter.initFilterBean();
        assertNotNull(filter.getIgnoredUrls());
        assertEquals(IGNORED_URL, filter.getIgnoredUrls());
    }

    @Test
    public void testUserWithSelectedUnitIsLoggedIn() throws ServletException, IOException {
        WebCertUser user = createUser();
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
    public void testUserWithoutSelectedCareUnitIsLoggedIn() throws ServletException, IOException {
        WebCertUser user = new WebCertUser();
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
    public void testIgnoredUrlsShouldNotGiveErrors() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn(IGNORED_URL);
        when(webCertUserService.hasAuthenticationContext()).thenReturn(false);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        assertNotNull(filter.getIgnoredUrls());
        verify(httpServletResponse, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }


    private WebCertUser createUser() {
        Vardgivare careGiver = new Vardgivare("cgId", "cgName");
        Vardenhet careUnit = new Vardenhet("cuId", "cuName");
        careGiver.setVardenheter(Arrays.asList(careUnit));

        WebCertUser user = new WebCertUser();
        user.setValdVardgivare(careGiver);
        user.setValdVardenhet(careUnit);

        return user;
    }
}
