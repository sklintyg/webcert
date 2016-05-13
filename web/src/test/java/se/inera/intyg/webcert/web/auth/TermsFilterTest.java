/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import se.inera.intyg.common.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT;

@RunWith(MockitoJUnitRunner.class)
public class TermsFilterTest extends AuthoritiesConfigurationTestSetup {

    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private SecurityContextImpl securityContext;
    @Mock
    private HttpSession session;
    @Mock
    private Authentication authentication;
    @Mock
    private AvtalService avtalService;

    @InjectMocks
    private TermsFilter filter;


    @Test
    public void testDoFilterNoSessionDoesNothing() throws ServletException, IOException {

        when(request.getSession(false)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testDoFilterNotAuthenticatedSessionDoesNothing() throws ServletException, IOException {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(null);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testDoFilterAuthenticatedSessionNotPrivatePractitionerDoesNothing() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(buildWebCertUser(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testFilterRedirectsWhenAuthenticatedSessionPrivatePractitionerHasNotAcceptedTerms() throws ServletException, IOException {
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(false);
        when(authentication.getPrincipal())
                .thenReturn(buildWebCertUser(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        when(request.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));

        filter.doFilterInternal(request, response, filterChain);
        verify(response, times(1)).sendRedirect(anyString());
        verify(session, times(1)).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, false);
    }

    @Test
    public void testFilterSetsSessionAttributeWhenAuthenticatedSessionPrivatePractitionerHasAcceptedTerms() throws ServletException, IOException {
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(true);
        when(authentication.getPrincipal())
                .thenReturn(buildWebCertUser(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(response, never()).sendRedirect(anyString());
        verify(session, times(1)).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    private WebCertUser buildWebCertUser(String authScheme) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setRoles(AuthoritiesResolverUtil.toMap(role));
        webCertUser.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        webCertUser.setAuthenticationScheme(authScheme);

        return webCertUser;
    }

}
