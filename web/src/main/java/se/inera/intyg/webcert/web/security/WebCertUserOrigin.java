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
package se.inera.intyg.webcert.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.RedisSavedRequestCache;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebCertUserOrigin implements UserOrigin {

//    @Resource
//    private Environment environment;

    private final RedisSavedRequestCache redisSavedRequestCache;

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "(/v\\d+)?/visa/intyg/.+$";
    private static final String FAKE = "/fake";
    private static final String USER_JSON_DISPLAY = "userJsonDisplay";
    private static final String ORIGIN = "origin";

    @Override
    public String resolveOrigin(HttpServletRequest request) {
        //Assert.notNull(request, "Request required");

//        DefaultSavedRequest savedRequest = getSavedRequest(request);
//        if (savedRequest == null) {
//            // Try to get saved request directly from Redis
//            savedRequest = (DefaultSavedRequest) redisSavedRequestCache.getRequest(request, null); // valueOps.get(requestedSessionId);
//            if (savedRequest == null) {
//                return UserOriginType.NORMAL.name();
//            }
//        }

        if (request == null) {
            throw new IllegalStateException("Failure resolving user origin. HttpServletRequest was null");
        }

        final var savedRequest = (DefaultSavedRequest) redisSavedRequestCache.getRequest(request, null);
        return savedRequest == null ? UserOriginType.NORMAL.name() : getUserOrigin(savedRequest);
    }

    private String getUserOrigin(DefaultSavedRequest savedRequest) {
        final var requestUrl = savedRequest.getRequestURI();
        if (requestUrl.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
            return UserOriginType.DJUPINTEGRATION.name();
        }
        if (requestUrl.equals(FAKE)) {
            return extractOriginFromRequest(savedRequest);
        }
        return UserOriginType.NORMAL.name();
    }

    private String extractOriginFromRequest(DefaultSavedRequest savedRequest) {
        final var mapper = new ObjectMapper();
        try {
            final var content = String.valueOf(savedRequest.getParameterMap().get(USER_JSON_DISPLAY)[0]);
            final var actualObj = mapper.readTree(content);
            return actualObj.get(ORIGIN).asText();

        } catch (Exception e) {
            log.warn("Could not get origin from fake login request.");
            return UserOriginType.NORMAL.name();
        }
    }

//    private DefaultSavedRequest getSavedRequest(HttpServletRequest request) {
//        return (DefaultSavedRequest) request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
//    }
}
