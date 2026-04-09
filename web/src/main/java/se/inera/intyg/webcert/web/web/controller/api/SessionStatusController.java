/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.security.filter.SessionTimeoutFilter;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.api.dto.GetSessionStatusResponse;

/**
 * Reports basic information about the current session status. This controller works in cooperation
 * with SessionTimeoutFilter that makes sure that requests to:
 *
 * <ul>
 *   <li>getSessionStatus does NOT extend the session
 *   <li>getExtendSession does extend the session.
 * </ul>
 *
 * @see SessionTimeoutFilter
 * @see org.springframework.security.web.context.SecurityContextRepository SecurityContextRepository
 * @see HttpSessionSecurityContextRepository HttpSessionSecurityContextRepository
 */
@RestController
@RequestMapping("/api/session-auth-check")
public class SessionStatusController {

  public static final String SESSION_STATUS_REQUEST_MAPPING = "/session-auth-check";
  public static final String SESSION_STATUS_PING = "/ping";
  public static final String SESSION_STATUS_EXTEND = "/extend";
  protected static final String UTF_8_CHARSET = ";charset=utf-8";

  @GetMapping(SESSION_STATUS_PING)
  @PerformanceLogging(
      eventAction = "session-get-session-status",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<GetSessionStatusResponse> getSessionStatus(HttpServletRequest request) {
    return ResponseEntity.ok(createStatusResponse(request));
  }

  @GetMapping(SESSION_STATUS_EXTEND)
  @PerformanceLogging(
      eventAction = "session-get-extend-session",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<GetSessionStatusResponse> getExtendSession(HttpServletRequest request) {
    return ResponseEntity.ok(createStatusResponse(request));
  }

  private GetSessionStatusResponse createStatusResponse(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    // The sessionTimeoutFilter should have put a secondsLeft attribute in the request for us to
    // use.
    Long secondsLeft =
        (Long) request.getAttribute(SessionTimeoutFilter.SECONDS_UNTIL_SESSIONEXPIRE_ATTRIBUTE_KEY);

    return new GetSessionStatusResponse(
        session != null,
        hasAuthenticatedPrincipalSession(session),
        secondsLeft == null ? 0 : secondsLeft);
  }

  private boolean hasAuthenticatedPrincipalSession(HttpSession session) {
    if (session != null) {
      final Object context =
          session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
      if (context != null && context instanceof SecurityContext) {
        SecurityContext securityContext = (SecurityContext) context;
        return securityContext.getAuthentication() != null
            && securityContext.getAuthentication().getPrincipal() != null;
      }
    }
    return false;
  }
}
