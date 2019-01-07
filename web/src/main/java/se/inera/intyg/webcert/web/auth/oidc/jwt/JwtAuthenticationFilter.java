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
package se.inera.intyg.webcert.web.auth.oidc.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MissingClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import se.inera.intyg.webcert.web.service.jwt.JwtIntrospectionService;
import se.inera.intyg.webcert.web.service.jwt.JwtValidationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * Custom authentication filter that supports extraction of JWT tokens from either an Authorization: Bearer: token
 * HTTP header or from x-www-form-urlencoded POST form data.
 *
 * @author eriklupander
 */
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtValidationService jwtValidationService;

    @Autowired
    private JwtIntrospectionService jwtIntrospectionService;

    protected JwtAuthenticationFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
        LOG.error("JWT Authentication enabled. DO NOT USE IN PRODUCTION UNLESS YOU KNOW WHAT YOU ARE DOING!!!");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String jwsToken = extractAccessToken(request);
        return authenticate(jwsToken);
    }

    private Authentication authenticate(String jwsToken) {
        // Validate JWS token signature
        Jws<Claims> jwt = jwtValidationService.validateJwsToken(jwsToken);

        // If the JWT has a valid signature, call the introspection service to validate it.
        jwtIntrospectionService.validateToken(jwsToken);

        // If both signature and introspection is OK, extract the employeeHsaId and initiate authorization.
        Object hsaIdObj = jwt.getBody().get("employeeHsaId");
        if (hsaIdObj == null) {
            throw new MissingClaimException(jwt.getHeader(), jwt.getBody(), "Could find claim for employeeHsaId");
        }

        String employeeHsaId = null;
        if (hsaIdObj instanceof String) {
            employeeHsaId = (String) hsaIdObj;
        } else if (hsaIdObj instanceof ArrayList) {
            ArrayList<String> parts = (ArrayList) hsaIdObj;
            if (parts != null && parts.size() > 0) {
                employeeHsaId = parts.get(0);
            } else {
                throw new IncorrectClaimException(jwt.getHeader(), jwt.getBody(),
                        "Could not extract claim for employeeHsaId, array type contained zero elements");
            }
        } else {
            throw new IncorrectClaimException(jwt.getHeader(), jwt.getBody(),
                    "Could not extract claim for employeeHsaId, claim was neither of class String nor ArrayList");
        }
        if (Strings.isNullOrEmpty(employeeHsaId)) {
            throw new MissingClaimException(jwt.getHeader(), jwt.getBody(), "Could extract claim for employeeHsaId");
        }

        // Build authentication token and proceed with authorization.
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(employeeHsaId);
        return getAuthenticationManager().authenticate(jwtAuthenticationToken);
    }

    private String extractAccessToken(HttpServletRequest request) {

        if (!request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Only HTTP POST is supported.");
        }

        // Check form parameter
        String accessToken = request.getParameter("access_token");

        if (!Strings.isNullOrEmpty(accessToken)) {
            return accessToken;
        }

        // Otherwise, check for authorization bearer header
        String authHeaderValue = request.getHeader("Authorization");

        if (!Strings.isNullOrEmpty(authHeaderValue) && authHeaderValue.startsWith("Bearer: ")) {
            return authHeaderValue.substring("Bearer: ".length());
        }

        // If neither worked, throw an exception.
        throw new AuthenticationServiceException("Request contained no 'Authorization: Bearer: <JWS token>' header or POST body");
    }
}
