/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_ELEG;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_SITHS;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_SITHS_NORMAL;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {

    private static final Map<String, List<String>> ELEG_PATTERNS = Map.of(
        "/webcert/web/user/pp-certificate/**", List.of(HttpMethod.GET)
    );

    private static final Map<String, List<String>> SITHS_PATTERNS = Map.of(
        "/visa/intyg/*", List.of(HttpMethod.GET, HttpMethod.POST),
        "/v2/visa/intyg/*", List.of(HttpMethod.POST)
    );

    private static final Map<String, List<String>> SITHS_NORMAL_PATTERNS = Map.of(
        "/web/maillink/**", List.of(HttpMethod.GET),
        "/webcert/web/user/launch/**", List.of(HttpMethod.GET),
        "/webcert/web/user/certificate/**", List.of(HttpMethod.GET),
        "/webcert/web/user/basic-certificate/**", List.of(HttpMethod.GET)
    );

    private static final String SAML2_AUTHENTICATION_PATH = "/saml2/authenticate/";
    private static final String ACCESS_DENIED_REDIRECT_PATH = "/error?reason=auth-exception-resource";

    public static final RequestMatcher ELEG_REQUEST_MATCHER = addPatterns(ELEG_PATTERNS);
    public static final RequestMatcher SITHS_REQUEST_MATCHER = addPatterns(SITHS_PATTERNS);
    public static final RequestMatcher SITHS_NORMAL_REQUEST_MATCHER = addPatterns(SITHS_NORMAL_PATTERNS);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException {

        if (ELEG_REQUEST_MATCHER.matches(request)) {
            response.sendRedirect(SAML2_AUTHENTICATION_PATH + REGISTRATION_ID_ELEG);

        } else if (SITHS_REQUEST_MATCHER.matches(request)) {
            response.sendRedirect(SAML2_AUTHENTICATION_PATH + REGISTRATION_ID_SITHS);

        } else if (SITHS_NORMAL_REQUEST_MATCHER.matches(request)) {
            response.sendRedirect(SAML2_AUTHENTICATION_PATH + REGISTRATION_ID_SITHS_NORMAL);

        } else {
            log.warn("Unauthenticated user was denied access to secured location '{}'", request.getRequestURI());
            response.sendRedirect(ACCESS_DENIED_REDIRECT_PATH);
        }
    }

    private static RequestMatcher addPatterns(Map<String, List<String>> antPatterns) {
        final var antMatchers = antPatterns.entrySet().stream()
            .flatMap(entrySet -> entrySet.getValue().stream()
                .map(value -> (RequestMatcher) new AntPathRequestMatcher(entrySet.getKey(), value))).toList();
        return new OrRequestMatcher(antMatchers);
    }

}
