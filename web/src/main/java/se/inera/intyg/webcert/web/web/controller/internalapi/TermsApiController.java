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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;

@RestController
@RequestMapping("/internalapi/terms")
@Api(value = "/internalapi/terms/approved", produces = "application/json")
public class TermsApiController {

  @Autowired private AvtalService avtalService;

  @GetMapping("/approved/{hsaId}")
  @PerformanceLogging(
      eventAction = "terms-get-webcert-approved-terms",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public boolean getWebcertTermsApproved(@PathVariable("hsaId") String hsaId) {
    return avtalService.userHasApprovedLatestAvtal(hsaId);
  }
}
