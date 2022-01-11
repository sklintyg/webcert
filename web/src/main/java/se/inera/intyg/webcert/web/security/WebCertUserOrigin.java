/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.RedisSavedRequestCache;

/**
 * Created by Magnus Ekstrand on 25/11/15.
 */
@Component
public class WebCertUserOrigin implements UserOrigin {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserOrigin.class);

    @Resource
    private Environment environment;

    @Autowired
    private RedisSavedRequestCache redisSavedRequestCache;

    // ~ Static fields/initializers
    // =====================================================================================

    public static final String REGEXP_REQUESTURI_READONLY = "/visa/intyg/.+/readonly$";
    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "(/v\\d+)?/visa/intyg/.+$";
    public static final String REGEXP_REQUESTURI_UTHOPP = "/webcert/web/user/certificate/.+/questions$";
    private static final String FAKE = "/fake";
    private static final String USER_JSON_DISPLAY = "userJsonDisplay";
    private static final String ORIGIN = "origin";
    private static final String DEV = "dev";

    // ~ API
    // =====================================================================================
    @Override
    public String resolveOrigin(HttpServletRequest request) {
        Assert.notNull(request, "Request required");

        DefaultSavedRequest savedRequest = getSavedRequest(request);
        if (savedRequest == null) {
            // Try to get saved request directly from Redis
            savedRequest = (DefaultSavedRequest) redisSavedRequestCache.getRequest(request, null); // valueOps.get(requestedSessionId);
            if (savedRequest == null) {
                return UserOriginType.NORMAL.name();
            }
        }

        String uri = savedRequest.getRequestURI();

        if (uri.matches(REGEXP_REQUESTURI_READONLY)) {
            return UserOriginType.READONLY.name();
        } else if (uri.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
            return UserOriginType.DJUPINTEGRATION.name();
        } else if (uri.matches(REGEXP_REQUESTURI_UTHOPP)) {
            return UserOriginType.UTHOPP.name();
        } else if (uri.equals(FAKE)) {
            return extractOriginFromRequest(savedRequest);
        }

        return UserOriginType.NORMAL.name();
    }

    private String extractOriginFromRequest(DefaultSavedRequest savedRequest) {
        if (isDevProfileActive()) {
            final var mapper = new ObjectMapper();
            try {
                final var actualObj = mapper.readTree(String.valueOf(savedRequest.getParameterMap().get(USER_JSON_DISPLAY)[0]));
                return actualObj.get(ORIGIN).asText();
            } catch (Exception e) {
                LOG.error("Could not get origin from fake login request.", e);
            }
        }

        return UserOriginType.NORMAL.name();
    }

    private boolean isDevProfileActive() {
        return Arrays.asList(environment.getActiveProfiles()).contains(DEV);
    }

    // ~ Private
    // =====================================================================================

    private DefaultSavedRequest getSavedRequest(HttpServletRequest request) {
        return (DefaultSavedRequest) request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
    }
}
