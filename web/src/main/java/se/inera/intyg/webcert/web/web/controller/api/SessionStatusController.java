/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import se.inera.intyg.infra.security.filter.SessionTimeoutFilter;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.api.dto.GetSessionStatusResponse;

/**
 * Reports basic information about the current session status. This controller works in cooperation with SessionTimeoutFilter that makes
 * sure that requests to:
 * <ul>
 * <li>getSessionStatus does NOT extend the session</li>
 * <li>getExtendSession does extend the session.</li>
 * </ul>
 *
 * @see SessionTimeoutFilter
 * @see org.springframework.security.web.context.SecurityContextRepository SecurityContextRepository
 * @see HttpSessionSecurityContextRepository HttpSessionSecurityContextRepository
 */

@Path(SessionStatusController.SESSION_STATUS_REQUEST_MAPPING)
@Api(value = "session-auth-check", description = "REST API för sessionen", produces = MediaType.APPLICATION_JSON)
public class SessionStatusController {

    public static final String SESSION_STATUS_REQUEST_MAPPING = "/session-auth-check";
    public static final String SESSION_STATUS_PING = "/ping";
    public static final String SESSION_STATUS_EXTEND = "/extend";
    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    @GET
    @Path(SESSION_STATUS_PING)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "session-get-session-status", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getSessionStatus(@Context HttpServletRequest request) {
        return Response.ok().entity(createStatusResponse(request)).build();
    }

    @GET
    @Path(SESSION_STATUS_EXTEND)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "session-get-extend-session", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response getExtendSession(@Context HttpServletRequest request) {
        return Response.ok().entity(createStatusResponse(request)).build();
    }

    private GetSessionStatusResponse createStatusResponse(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        // The sessionTimeoutFilter should have put a secondsLeft attribute in the request for us to use.
        Long secondsLeft = (Long) request.getAttribute(SessionTimeoutFilter.SECONDS_UNTIL_SESSIONEXPIRE_ATTRIBUTE_KEY);

        return new GetSessionStatusResponse(session != null, hasAuthenticatedPrincipalSession(session),
            secondsLeft == null ? 0 : secondsLeft);
    }

    private boolean hasAuthenticatedPrincipalSession(HttpSession session) {
        if (session != null) {
            final Object context = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (context != null && context instanceof SecurityContext) {
                SecurityContext securityContext = (SecurityContext) context;
                return securityContext.getAuthentication() != null && securityContext.getAuthentication().getPrincipal() != null;
            }

        }
        return false;
    }

}
