/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Created by eriklupander on 2015-10-14.
 */
@RunWith(MockitoJUnitRunner.class)
public class IdpSelectionFilterTest {

    private static final String DEFAULT_QA_PATH = "/webcert/web/user/certificate/id/questions";
    private static final String PP_QA_PATH = "/webcert/web/user/pp-certificate/id/questions";


    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private HttpSession httpSession;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SavedRequestFactory savedRequestFactory;

    @Mock
    private DefaultSavedRequest defaultSavedRequest;

    @InjectMocks
    private IdpSelectionFilter testee = new IdpSelectionFilter();

    private void initMocksForAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new WebCertUser());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(httpSession.getAttribute(AuthConstants.SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);
    }

    private void initMocksForUnauthenticated() {
        when(httpSession.getAttribute(AuthConstants.SPRING_SECURITY_CONTEXT)).thenReturn(null);
        when(savedRequestFactory.buildSavedRequest(any(HttpServletRequest.class))).thenReturn(defaultSavedRequest);
    }


    @Test
    public void testAlreadyAuthenticatedDoesNotRedirect() throws ServletException, IOException {
        initMocksForAuthenticated();

        when(httpServletRequest.getRequestURI()).thenReturn(DEFAULT_QA_PATH);
        when(httpServletRequest.getSession(true)).thenReturn(httpSession);
        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse, times(0)).sendRedirect(anyString());
    }

    @Test
    public void testNonAuthenticatedSithsIsRedirected() throws ServletException, IOException {
        initMocksForUnauthenticated();
        when(httpServletRequest.getRequestURI()).thenReturn(DEFAULT_QA_PATH);
        when(httpServletRequest.getSession(true)).thenReturn(httpSession);
        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse, times(1)).sendRedirect(contains("/saml/login/alias/" + AuthConstants.ALIAS_SITHS + "?idp="));
    }

    @Test
    public void testNonAuthenticatedPPIsRedirected() throws ServletException, IOException {
        initMocksForUnauthenticated();
        when(httpServletRequest.getRequestURI()).thenReturn(PP_QA_PATH);
        when(httpServletRequest.getSession(true)).thenReturn(httpSession);
        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse, times(1)).sendRedirect(contains("/saml/login/alias/" + AuthConstants.ALIAS_ELEG + "?idp="));
    }



}
