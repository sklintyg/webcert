package se.inera.intyg.webcert.web.auth;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionTimeoutFilter extends OncePerRequestFilter {

    private static final String SESSION_LAST_ACCESS_TIME = "SessionLastAccessTime";
    private static final int MILLISECONDS_PER_SECONDS = 1000;

    private String ignoredUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Long lastAccess = (Long) session.getAttribute(SESSION_LAST_ACCESS_TIME);

            long inactiveTime = (lastAccess == null) ? 0 : (System.currentTimeMillis() - lastAccess);
            long maxInactiveTime = session.getMaxInactiveInterval() * MILLISECONDS_PER_SECONDS;

            if (inactiveTime > maxInactiveTime) {
                session.invalidate();

            } else if (!request.getRequestURI().contains(ignoredUrl)) {
                session.setAttribute(SESSION_LAST_ACCESS_TIME, System.currentTimeMillis());
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setIgnoredUrl(String ignoredUrl) {
        this.ignoredUrl = ignoredUrl;
    }
}
