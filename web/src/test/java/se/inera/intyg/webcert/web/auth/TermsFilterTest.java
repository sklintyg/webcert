package se.inera.intyg.webcert.web.auth;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
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
import org.springframework.security.core.context.SecurityContextImpl;

import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class TermsFilterTest {

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
        WebCertUser webCertUser = new WebCertUser();

        webCertUser.setRoles(getGrantedRole());
        webCertUser.setAuthorities(getGrantedPrivileges());
        webCertUser.setAuthenticationScheme(authScheme);

        return webCertUser;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
