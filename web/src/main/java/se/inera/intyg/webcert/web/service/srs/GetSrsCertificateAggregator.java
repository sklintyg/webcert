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
package se.inera.intyg.webcert.web.service.srs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@Service("getSrsCertificateAggregator")
public class GetSrsCertificateAggregator implements GetSrsCertificate {

  private final CSIntegrationService csIntegrationService;
  private final GetSrsCertificate getSrsCertificateFromWC;
  private final GetSrsCertificate getSrsCertificateFromCS;

  public GetSrsCertificateAggregator(
      CSIntegrationService csIntegrationService,
      @Qualifier("GetSrsCertificateFromWC") GetSrsCertificate getSrsCertificateFromWC,
      @Qualifier("GetSrsCertificateFromCS") GetSrsCertificate getSrsCertificateFromCS) {
    this.csIntegrationService = csIntegrationService;
    this.getSrsCertificateFromWC = getSrsCertificateFromWC;
    this.getSrsCertificateFromCS = getSrsCertificateFromCS;
  }

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    if (Boolean.TRUE.equals(csIntegrationService.certificateExists(certificateId))) {
      return getSrsCertificateFromCS.getSrsCertificate(certificateId);
    }
    return getSrsCertificateFromWC.getSrsCertificate(certificateId);
  }
}
