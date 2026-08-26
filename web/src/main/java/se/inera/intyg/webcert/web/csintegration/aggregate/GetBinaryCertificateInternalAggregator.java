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
package se.inera.intyg.webcert.web.csintegration.aggregate;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetBinaryCertificate;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetBinaryCertificateResponseDTO;

@Service("getBinaryCertificateInternalAggregator")
public class GetBinaryCertificateInternalAggregator implements GetBinaryCertificate {

  private final GetBinaryCertificate getBinaryCertificateFromCS;
  private final GetBinaryCertificate getBinaryCertificateFromWC;

  public GetBinaryCertificateInternalAggregator(
      GetBinaryCertificate getBinaryCertificateFromCS,
      GetBinaryCertificate getBinaryCertificateFromWC) {
    this.getBinaryCertificateFromCS = getBinaryCertificateFromCS;
    this.getBinaryCertificateFromWC = getBinaryCertificateFromWC;
  }

  @Override
  public GetBinaryCertificateResponseDTO get(String certificateId) {
    final var responseFromCS = getBinaryCertificateFromCS.get(certificateId);
    return responseFromCS == null ? getBinaryCertificateFromWC.get(certificateId) : responseFromCS;
  }
}
