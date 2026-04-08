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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.testcertificate.dto.TestCertificateEraseRequest;
import se.inera.intyg.webcert.infra.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.testcertificate.TestCertificateService;

/** Internal REST endpoint for managing test certificates. */
@RestController
@RequestMapping("/internalapi/testCertificate")
public class TestCertificateController {

  @Autowired private TestCertificateService testCertificateService;

  @PrometheusTimeMethod
  @PostMapping("/erase")
  @PerformanceLogging(
      eventAction = "test-certificate-erase",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<TestCertificateEraseResult> eraseTestCertificates(
      @RequestBody TestCertificateEraseRequest eraseRequest) {

    if (eraseRequest.getTo() == null) {
      throw new IllegalArgumentException("Missing date to");
    }

    if (eraseRequest.getFrom() != null && eraseRequest.getFrom().isAfter(eraseRequest.getTo())) {
      throw new IllegalArgumentException("From date is after to date");
    }

    final TestCertificateEraseResult eraseResult =
        testCertificateService.eraseTestCertificates(eraseRequest.getFrom(), eraseRequest.getTo());

    return ResponseEntity.ok(eraseResult);
  }
}
