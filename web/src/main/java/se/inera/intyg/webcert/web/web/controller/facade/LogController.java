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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.validation.constraints.NotNull;
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
import se.inera.intyg.webcert.web.service.facade.ErrorLogFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ErrorLogRequestDTO;

@RestController
@RequestMapping("/api/log")
public class LogController {

  private static final Logger LOG = LoggerFactory.getLogger(LogController.class);

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  @Autowired private ErrorLogFacadeService errorLogFacadeService;

  @PostMapping("/error")
  @PerformanceLogging(eventAction = "log-log-error", eventType = MdcLogConstants.EVENT_TYPE_ERROR)
  public ResponseEntity<Void> logError(@RequestBody @NotNull ErrorLogRequestDTO errorLogRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Logging error with id: '{}'", errorLogRequest.getErrorId());
    }

    errorLogFacadeService.log(errorLogRequest);
    return ResponseEntity.ok().build();
  }
}
