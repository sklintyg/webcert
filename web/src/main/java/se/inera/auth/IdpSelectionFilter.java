package se.inera.auth;

import static se.inera.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;
import static se.inera.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import se.inera.auth.common.AuthConstants;
import se.inera.webcert.service.user.dto.WebCertUser;

/**
 * This class is used to make IDP selection automatic for uthoppslänkar and djupintegratinslänkar for non-authenticated
 * users.
 *
 * Created by eriklupander on 2015-10-12.
 */
@Component(value = "idpSelectionFilter")
public class IdpSelectionFilter extends OncePerRequestFilter {

    @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
    private String elegIdp;

    @Value("${sakerhetstjanst.saml.idp.metadata.url}")
    private String sithsIdp;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws ServletException, IOException {

        // Check if we're logged in, e.g. have a useful session
        HttpSession session = req.getSession(false);

        if (session != null && hasSessionWithSpringContext(session)) {

            // Get the principal, if a proper WebCertUser we're logged in and can continue down the filter chain
            Authentication authentication = extractAuthentication(session);
            if (isAuthenticatedInWebcert(authentication)) {
                doFilter(req, resp, filterChain);
                return;
            }
        }

        // If not logged in, we need to put the request URI into the savedrequests
        if (session != null) {
            session.setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, new DefaultSavedRequest(req, new PortResolverImpl()));
        }

        // Finally, send redirect to explict login path for the appropriate IDP depending on the requestURI
        String requestURI = req.getRequestURI();
        if (requestURI.contains("/webcert/web/user/certificate/")) {
             resp.sendRedirect("/saml/login/alias/" + AuthConstants.ALIAS_SITHS + "?idp=" + sithsIdp);
        }
        if (requestURI.contains("/webcert/web/user/pp-certificate/")) {
            resp.sendRedirect("/saml/login/alias/" + AuthConstants.ALIAS_ELEG + "?idp=" + elegIdp);
        }
        // We never continue down the filter chain if we've come this far...
   }

    private Authentication extractAuthentication(HttpSession session) {
        return ((SecurityContextImpl) session.getAttribute(SPRING_SECURITY_CONTEXT)).getAuthentication();
    }

    private boolean isAuthenticatedInWebcert(Authentication authentication) {
        return authentication.isAuthenticated() && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof WebCertUser;
    }

    private boolean hasSessionWithSpringContext(HttpSession session) {
        return session != null && session.getAttribute(SPRING_SECURITY_CONTEXT) != null;
    }


}
