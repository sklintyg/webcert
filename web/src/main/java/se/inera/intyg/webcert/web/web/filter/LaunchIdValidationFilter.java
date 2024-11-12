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
package se.inera.intyg.webcert.web.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.handlers.WebcertRestExceptionResponse;

@Component(value = "launchIdValidationFilter")
public class LaunchIdValidationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LaunchIdValidationFilter.class);
    private static RequestMatcher requestMatcher;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper mapper;

    @Autowired
    private WebCertUserService webCertUserService;

    static {
        final String[] publicEndPoints = {"/api/configuration/**", "/api/log/**"};
        ignorePatterns(publicEndPoints);
    }

    private static void ignorePatterns(String... antPatterns) {
        List<RequestMatcher> matchers = new ArrayList<>();
        for (String pattern : antPatterns) {
            matchers.add(new AntPathRequestMatcher(pattern, null));
        }
        requestMatcher = new OrRequestMatcher(matchers);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return requestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (continueFilterChain(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        final var user = webCertUserService.getUser();
        final var launchId = request.getHeader("launchId");
        if (user.getParameters().getLaunchId().equals(launchId)) {
            filterChain.doFilter(request, response);
            return;
        }
        final var errorDetails = new HashMap<>();
        final var webcertRestExceptionResponse = getWebcertRestExceptionResponse();
        errorDetails.put("errorCode", webcertRestExceptionResponse.getErrorCode());
        errorDetails.put("message", webcertRestExceptionResponse.getMessage());
        LOG.info("Provided launchId: '{}'- does not match with current session launchId: '{}' - session will be invalidated.", launchId,
            user.getParameters().getLaunchId());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorDetails);
    }

    private boolean continueFilterChain(HttpServletRequest request) {
        return request.getHeader("launchId") == null || webCertUserService == null || webCertUserService.getUser() == null
            || webCertUserService.getUser().getParameters() == null;
    }

    private WebcertRestExceptionResponse getWebcertRestExceptionResponse() {
        return new WebcertRestExceptionResponse(WebCertServiceErrorCodeEnum.INVALID_LAUNCHID, "Invalid launchId");
    }
}
