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
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebCertUserOrigin implements UserOrigin {

    private final RequestCache requestCache;

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "(/v\\d+)?/visa/intyg/.+$";
    private static final String USER_JSON_DISPLAY = "userJsonDisplay";
    private static final String ORIGIN = "origin";
    private static final String FAKE = "/fake";

    @Override
    public String resolveOrigin(HttpServletRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Failure resolving user origin. HttpServletRequest was null");
        }

        final var savedRequest = requestCache.getRequest(request, null);
        return savedRequest == null ? UserOriginType.NORMAL.name() : getUserOrigin(savedRequest);
    }

    private String getUserOrigin(SavedRequest savedRequest) {
        final var requestUrl = ((DefaultSavedRequest) savedRequest).getRequestURI();
        if (requestUrl == null) {
            return UserOriginType.NORMAL.name();
        }
        if (requestUrl.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
            return UserOriginType.DJUPINTEGRATION.name();
        }
        if (requestUrl.equals(FAKE)) {
            return extractOriginFromRequest(savedRequest);
        }
        return UserOriginType.NORMAL.name();
    }

    private String extractOriginFromRequest(SavedRequest savedRequest) {
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

}
