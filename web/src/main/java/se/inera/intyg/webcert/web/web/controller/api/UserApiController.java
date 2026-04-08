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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserPreferenceStorageRequest;

/**
 * Controller for accessing the users security context.
 *
 * @author npet
 */
@RestController
@RequestMapping("/api/anvandare")
public class UserApiController extends AbstractApiController {

  private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

  @PutMapping("/preferences")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-store-metadata-entry",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> storeUserMetdataEntry(
      @RequestBody WebUserPreferenceStorageRequest request) {
    LOG.debug("User stored user preference entry for key: " + request.getKey());
    getWebCertUserService().storeUserPreference(request.getKey(), request.getValue());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/preferences/{key}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-delete-user-preference-entry",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> deleteUserPreferenceEntry(@PathVariable("key") String prefKey) {
    LOG.debug("User deleted user preference entry for key: " + prefKey);
    getWebCertUserService().deleteUserPreference(prefKey);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/logout")
  @PrometheusTimeMethod
  @PerformanceLogging(eventAction = "user-logout", eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> logoutUserAfterTimeout(HttpServletRequest request) {
    HttpSession session = request.getSession();

    getWebCertUserService().scheduleSessionRemoval(session);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/logout/cancel")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-cancel-logout",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> cancelLogout(HttpServletRequest request) {
    HttpSession session = request.getSession();

    getWebCertUserService().cancelScheduledLogout(session);

    return ResponseEntity.ok().build();
  }
}
