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
import se.inera.intyg.webcert.infra.integration.hsatk.model.HospCredentialsForPerson;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceHospCredentialsForPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialsForPersonRequestDTO;

@Service
@RequiredArgsConstructor
public class GetHospCredentialsForPersonService {

  private final HsaIntygProxyServiceHospCredentialsForPersonClient credentialsForPersonClient;

  public HospCredentialsForPerson get(String personId) {
    validateRequest(personId);
    final var credentialsForPersonResponseDTO =
        credentialsForPersonClient.get(
            GetCredentialsForPersonRequestDTO.builder().personId(personId).build());
    return credentialsForPersonResponseDTO.getCredentials();
  }

  private void validateRequest(String personId) {
    if (personId == null || personId.isEmpty()) {
      throw new IllegalArgumentException("Missing required parameter 'personId'");
    }
  }
}
