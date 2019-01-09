/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

/**
 * This class is used to make IDP selection automatic for uthoppslänkar and djupintegratinslänkar for non-authenticated
 * users.
 *
 * Requests from authenticated users are just passed down the filter chain.
 *
 * Created by eriklupander on 2015-10-12.
 */
@Component(value = "idpSelectionFilter")
public class IdpSelectionFilter extends OncePerRequestFilter {

    @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
    private String elegIdp;

    @Value("${sakerhetstjanst.saml.idp.metadata.url}")
    private String sithsIdp;

    @Autowired
    private SavedRequestFactory savedRequestFactory;

    @Autowired
    private RedisSavedRequestCache redisSavedRequestCache;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if we're logged in, e.g. have a useful session
        HttpSession session = req.getSession(true);

        if (session != null && hasSessionWithSpringContext(session)) {

            // Get the principal, if a proper WebCertUser we're logged in and can continue down the filter chain
            Authentication authentication = extractAuthentication(session);
            if (isAuthenticatedInWebcert(authentication)) {
                filterChain.doFilter(req, resp);
                return;
            }
        }

        // If not logged in, we need to put the request URI into the savedrequests
        if (session != null) {
            SavedRequest savedRequest = savedRequestFactory.buildSavedRequest(req);
            // First, save to the session... which should be unnecessary.
            session.setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);

            // Next, save to Redis.
            redisSavedRequestCache.saveRequest(req, resp);
        }

        // Finally, send redirect to explicit login path for the appropriate IDP depending on the requestURI
        String requestURI = req.getRequestURI();
        if (isAuthenticateWithSiths(requestURI)) {
            resp.sendRedirect("/saml/login/alias/" + AuthConstants.ALIAS_SITHS + "?idp=" + sithsIdp);
        }
        if (isAuthenticateWithEleg(requestURI)) {
            resp.sendRedirect("/saml/login/alias/" + AuthConstants.ALIAS_ELEG + "?idp=" + elegIdp);
        }
        // We never continue down the filter chain if we've come this far...
    }

    private boolean isAuthenticateWithEleg(String requestURI) {
        return requestURI.contains("/webcert/web/user/pp-certificate/");
    }

    private boolean isAuthenticateWithSiths(String requestURI) {
        return requestURI.contains("/webcert/web/user/certificate/") || requestURI.contains("/webcert/web/user/basic-certificate/");
    }

    private Authentication extractAuthentication(HttpSession session) {
        return ((SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT)).getAuthentication();
    }

    private boolean isAuthenticatedInWebcert(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && authentication.getPrincipal() instanceof WebCertUser;
    }

    private boolean hasSessionWithSpringContext(HttpSession session) {
        return session != null && session.getAttribute(SPRING_SECURITY_CONTEXT) != null;
    }

    // Setter for the savedRequestFactory
    public void setSavedRequestFactory(SavedRequestFactory savedRequestFactory) {
        this.savedRequestFactory = savedRequestFactory;
    }
}
