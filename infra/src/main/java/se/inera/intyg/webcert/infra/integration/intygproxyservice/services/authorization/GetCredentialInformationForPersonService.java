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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceCredentialInformationForPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationRequestDTO;

@Service
@RequiredArgsConstructor
public class GetCredentialInformationForPersonService {

  private final HsaIntygProxyServiceCredentialInformationForPersonClient
      credentialInformationForPersonClient;

  public List<CredentialInformation> get(
      GetCredentialInformationRequestDTO credentialInformationRequestDTO) {
    validateRequest(credentialInformationRequestDTO);
    final var credentialInformationResponseDTO =
        credentialInformationForPersonClient.get(credentialInformationRequestDTO);
    return credentialInformationResponseDTO.getCredentialInformation();
  }

  private void validateRequest(GetCredentialInformationRequestDTO credentialInformationRequestDTO) {
    if (credentialInformationRequestDTO.getPersonHsaId() == null
        || credentialInformationRequestDTO.getPersonHsaId().isEmpty()) {
      throw new IllegalArgumentException("PersonHsaId is a required field");
    }
  }
}
