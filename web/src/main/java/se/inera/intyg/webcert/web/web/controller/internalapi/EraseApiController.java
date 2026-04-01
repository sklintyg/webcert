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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.erase.EraseService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@RestController
@RequestMapping("/internalapi/v1/certificates")
public class EraseApiController extends AbstractApiController {

  @Value("${erase.certificates.page.size:1000}")
  private int eraseCertificatesPageSize;

  @Autowired private EraseService eraseService;

  @DeleteMapping("/{id}")
  @PerformanceLogging(
      eventAction = "erase-erase-data-for-care-provider",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<Void> eraseDataForCareProvider(@PathVariable("id") String careProviderId) {
    eraseService.eraseCertificates(careProviderId, eraseCertificatesPageSize);
    return ResponseEntity.noContent().build();
  }
}
