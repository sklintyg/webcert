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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HospCredentialsForPerson;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceHospCredentialsForPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialsForPersonRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialsForPersonResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetHospCredentialsForPersonServiceTest {

  private static final String PERSON_ID = "personId";
  private static final GetCredentialsForPersonRequestDTO GET_CREDENTIALS_FOR_PERSON_REQUEST_DTO =
      GetCredentialsForPersonRequestDTO.builder().personId(PERSON_ID).build();
  private static final GetCredentialsForPersonResponseDTO GET_CREDENTIALS_FOR_PERSON_RESPONSE_DTO =
      GetCredentialsForPersonResponseDTO.builder()
          .credentials(new HospCredentialsForPerson())
          .build();
  @Mock private HsaIntygProxyServiceHospCredentialsForPersonClient credentialsForPersonClient;
  @InjectMocks private GetHospCredentialsForPersonService getHospCredentialsForPersonService;

  @Test
  void shouldThrowIfPersonIdIsNull() {
    assertThrows(
        IllegalArgumentException.class, () -> getHospCredentialsForPersonService.get(null));
  }

  @Test
  void shouldThrowIfPersonIdIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> getHospCredentialsForPersonService.get(""));
  }

  @Test
  void shouldReturnHospCredentialsForPerson() {
    when(credentialsForPersonClient.get(GET_CREDENTIALS_FOR_PERSON_REQUEST_DTO))
        .thenReturn(GET_CREDENTIALS_FOR_PERSON_RESPONSE_DTO);

    final var result = getHospCredentialsForPersonService.get(PERSON_ID);

    assertEquals(GET_CREDENTIALS_FOR_PERSON_RESPONSE_DTO.getCredentials(), result);
  }
}
