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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@RunWith(MockitoJUnitRunner.class)
public class LaunchIdValidationFilterTest {

    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String NEW_LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f5";

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private FilterChain filterChain;
    @Mock
    private WebCertUserService webCertUserService;

    @Captor
    ArgumentCaptor<Map> mapArgumentCaptor;

    @InjectMocks
    private LaunchIdValidationFilter filter = new LaunchIdValidationFilter();

    @Test
    public void filterChainShouldContinueWhenRequestLaunchIdIsNull() throws ServletException, IOException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(null);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain, atLeastOnce()).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void filterChainShouldContinueWhenUserIsNull() throws ServletException, IOException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(LAUNCH_ID);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain, atLeastOnce()).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void filterChainShouldStopWhenLaunchIdDoesNotMatchAndGiveError() throws ServletException, IOException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(NEW_LAUNCH_ID);
        when(webCertUserService.getUser()).thenReturn(createUserWithIntegrationsParameters());
        when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain, never()).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void filterChainShouldContinueWhenUserDoesNotHaveIntegrationsParametersSet() throws ServletException, IOException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(LAUNCH_ID);
        when(webCertUserService.getUser()).thenReturn(createNormalUser());

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain, atLeastOnce()).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void filterShouldReturnMessageContainingErrorCodeInvalidLaunchId() throws IOException, ServletException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(NEW_LAUNCH_ID);
        when(webCertUserService.getUser()).thenReturn(createUserWithIntegrationsParameters());
        when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(mapper).writeValue(any(PrintWriter.class), mapArgumentCaptor.capture());
        assertEquals(WebCertServiceErrorCodeEnum.INVALID_LAUNCHID, mapArgumentCaptor.getValue().get("errorCode"));
    }

    @Test
    public void filterShouldReturnMessageContainingMessageInvalidLaunchId() throws IOException, ServletException {
        when(httpServletRequest.getHeader("launchId")).thenReturn(NEW_LAUNCH_ID);
        when(webCertUserService.getUser()).thenReturn(createUserWithIntegrationsParameters());
        when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);

        filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(mapper).writeValue(any(PrintWriter.class), mapArgumentCaptor.capture());
        assertEquals("Invalid launchId", mapArgumentCaptor.getValue().get("message"));
    }

    private WebCertUser createUserWithIntegrationsParameters() {
        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters("", "", "", "", "",
            "", "", "", "", true, false, false, true, LAUNCH_ID));
        return user;
    }

    private WebCertUser createNormalUser() {
        return new WebCertUser();
    }
}
