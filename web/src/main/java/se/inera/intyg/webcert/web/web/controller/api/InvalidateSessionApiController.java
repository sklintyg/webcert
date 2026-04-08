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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.launchid.InvalidateSessionService;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

@RestController
@RequestMapping("/api/v1/session")
public class InvalidateSessionApiController {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidateSessionApiController.class);
  public static final String SESSION_STATUS_REQUEST_MAPPING = "/v1/session";
  public static final String INVALIDATE_ENDPOINT = "/invalidate";
  protected static final String UTF_8_CHARSET = ";charset=utf-8";
  @Autowired private InvalidateSessionService invalidateSessionService;

  @PostMapping(INVALIDATE_ENDPOINT)
  @PerformanceLogging(
      eventAction = "invalidate-session-invalidate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<Void> invalidateSession(@RequestBody InvalidateRequest invalidateRequest) {
    if (invalidateRequest.formatIsWrong()) {
      LOG.info(
          String.format(
              "launchId: %s OR userHsaId: %s - is wrong format. request will not be handled any further",
              invalidateRequest.getLaunchId(), invalidateRequest.getUserHsaId()));
      return ResponseEntity.noContent().build();
    }
    try {
      invalidateSessionService.invalidateSessionIfActive(invalidateRequest);
    } catch (Exception exception) {
      LOG.error("Invalidate session failed. launchId: %s - userHsaId: %s", exception);
    }
    return ResponseEntity.noContent().build();
  }
}
