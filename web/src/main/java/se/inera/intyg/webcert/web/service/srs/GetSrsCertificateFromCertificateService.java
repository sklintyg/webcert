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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalResponseDTO;

@Slf4j
@RequiredArgsConstructor
@Service("GetSrsCertificateFromCS")
public class GetSrsCertificateFromCertificateService implements GetSrsCertificate {

  private final CSIntegrationService csIntegrationService;

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    final var sickLeaveCertificate = csIntegrationService.getSickLeaveCertificate(certificateId);
    if (isSickLeaveCertificatePresent(sickLeaveCertificate)) {
      return new SrsCertificate(
          sickLeaveCertificate.get().getSickLeaveCertificate().getId(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getDiagnoseCode(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getSigningDateTime() != null
              ? sickLeaveCertificate
                  .get()
                  .getSickLeaveCertificate()
                  .getSigningDateTime()
                  .toLocalDate()
              : null,
          sickLeaveCertificate.get().getSickLeaveCertificate().getExtendsCertificateId());
    }
    return null;
  }

  private static boolean isSickLeaveCertificatePresent(
      Optional<GetSickLeaveCertificateInternalResponseDTO> sickLeaveCertificate) {
    return sickLeaveCertificate.isPresent() && sickLeaveCertificate.get().isAvailable();
  }
}
