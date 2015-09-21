package se.inera.auth;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import se.inera.auth.common.UnifiedUserDetailsService;
import se.inera.webcert.common.security.authority.SimpleGrantedAuthority;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.inera.webcert.service.user.dto.WebCertUser;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    AvtalService avtalService;

    @InjectMocks
    private TermsFilter filter;

//    @Before
//    public void setupFilter() {
//        filter = new TermsFilter();
//    }

    @Test
    public void testDoFilterNoSessionDoesNothing() throws ServletException, IOException {

        when(request.getSession(false)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testDoFilterNotAuthenticatedSessionDoesNothing() throws ServletException, IOException {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(TermsFilter.SPRING_SECURITY_CONTEXT)).thenReturn(null);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testDoFilterAuthenticatedSessionNotPrivatePractitionerDoesNothing() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(buildWebCertUser(UnifiedUserDetailsService.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(TermsFilter.SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(session, never()).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    @Test
    public void testFilterRedirectsWhenAuthenticatedSessionPrivatePractitionerHasNotAcceptedTerms() throws ServletException, IOException {
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(buildWebCertUser(UnifiedUserDetailsService.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(TermsFilter.SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);
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
        when(authentication.getPrincipal()).thenReturn(buildWebCertUser(UnifiedUserDetailsService.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(TermsFilter.SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);

        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED)).thenReturn(false);
        when(session.getAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_INPROGRESS)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);
        verify(response, never()).sendRedirect(anyString());
        verify(session, times(1)).setAttribute(TermsFilter.PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
    }

    private WebCertUser buildWebCertUser(String authScheme) {
        WebCertUser webCertUser = new WebCertUser(getGrantedRole(), getGrantedPrivileges());
        webCertUser.setAuthenticationScheme(authScheme);

        return webCertUser;
    }

    private GrantedAuthority getGrantedRole() {
        return new SimpleGrantedAuthority(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE.toString());
    }

    private Collection<? extends GrantedAuthority> getGrantedPrivileges() {
        Set<SimpleGrantedAuthority> privileges = new HashSet<SimpleGrantedAuthority>();
        for (UserPrivilege userPrivilege : UserPrivilege.values()) {
            privileges.add(new SimpleGrantedAuthority(userPrivilege.name(), userPrivilege.toString()));
        }
        return privileges;
    }


}
