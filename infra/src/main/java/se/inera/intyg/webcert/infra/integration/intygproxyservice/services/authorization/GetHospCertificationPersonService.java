/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Result;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceHospCertificationPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetHospCertificationPersonRequestDTO;

@Service
@RequiredArgsConstructor
public class GetHospCertificationPersonService {

  private final HsaIntygProxyServiceHospCertificationPersonClient hospCertificationPersonClient;

  public Result get(GetHospCertificationPersonRequestDTO hospCertificationPersonRequestDTO) {
    validateRequest(hospCertificationPersonRequestDTO);
    final var hospCertificationPersonResponseDTO =
        hospCertificationPersonClient.get(hospCertificationPersonRequestDTO);
    return hospCertificationPersonResponseDTO.getResult();
  }

  private void validateRequest(
      GetHospCertificationPersonRequestDTO hospCertificationPersonRequestDTO) {
    if (hospCertificationPersonRequestDTO.getPersonId() == null
        || hospCertificationPersonRequestDTO.getPersonId().isEmpty()) {
      throw new IllegalArgumentException("Missing required parameter 'personId'");
    }
    if (hospCertificationPersonRequestDTO.getOperation() == null
        || hospCertificationPersonRequestDTO.getOperation().isEmpty()) {
      throw new IllegalArgumentException("Missing required parameter 'operation'");
    }
  }
}
