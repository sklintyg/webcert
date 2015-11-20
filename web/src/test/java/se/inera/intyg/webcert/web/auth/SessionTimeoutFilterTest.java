package se.inera.intyg.webcert.web.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionTimeoutFilterTest {

    private static final int EXPIRED_SESSION = 1001;
    private static final int VALID_SESSION = 500;

    private SessionTimeoutFilter filter;

    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

    @Before
    public void setupFilter() {
        filter = new SessionTimeoutFilter();
        filter.setIgnoredUrl("/moduleapi/stat");
    }

    @Test
    public void testDoFilterInvalidSession() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/index.html");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SessionLastAccessTime")).thenReturn(System.currentTimeMillis() - EXPIRED_SESSION);
        when(session.getMaxInactiveInterval()).thenReturn(1);

        filter.doFilterInternal(request, null, filterChain);

        verify(session).invalidate();
        verify(session, never()).setAttribute(eq("SessionLastAccessTime"), anyString());
    }

    @Test
    public void testDoFilterInvalidSessionIgnoredUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/moduleapi/stat");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SessionLastAccessTime")).thenReturn(System.currentTimeMillis() - EXPIRED_SESSION);
        when(session.getMaxInactiveInterval()).thenReturn(1);

        filter.doFilterInternal(request, null, filterChain);

        verify(session).invalidate();
        verify(session, never()).setAttribute(eq("SessionLastAccessTime"), anyString());
    }

    @Test
    public void testDoFilterValidSession() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/index.html");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SessionLastAccessTime")).thenReturn(System.currentTimeMillis() - VALID_SESSION);
        when(session.getMaxInactiveInterval()).thenReturn(1);

        filter.doFilterInternal(request, null, filterChain);

        verify(session, never()).invalidate();
        verify(session).setAttribute(eq("SessionLastAccessTime"), anyString());
    }

    @Test
    public void testDoFilterValidSessionIgnoredUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/moduleapi/stat");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SessionLastAccessTime")).thenReturn(System.currentTimeMillis() - VALID_SESSION);
        when(session.getMaxInactiveInterval()).thenReturn(1);

        filter.doFilterInternal(request, null, filterChain);

        verify(session, never()).invalidate();
        verify(session, never()).setAttribute(eq("SessionLastAccessTime"), anyString());
    }
}
