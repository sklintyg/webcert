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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceCredentialInformationForPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetCredentialInformationForPersonServiceTest {

  private static final String PERSON_HSA_ID = "personHsaId";
  private static final List<CredentialInformation> CREDENTIAL_INFORMATIONS =
      List.of(new CredentialInformation());

  @Mock
  private HsaIntygProxyServiceCredentialInformationForPersonClient
      credentialInformationForPersonClient;

  @InjectMocks
  private GetCredentialInformationForPersonService getCredentialInformationForPersonService;

  @Test
  void shouldValidateRequest() {
    final var request = GetCredentialInformationRequestDTO.builder().build();

    assertThrows(
        IllegalArgumentException.class,
        () -> getCredentialInformationForPersonService.get(request));
  }

  @Test
  void shouldReturnListOfCredentialInformationForPerson() {
    final var request =
        GetCredentialInformationRequestDTO.builder().personHsaId(PERSON_HSA_ID).build();

    final var expectedResponse =
        GetCredentialInformationResponseDTO.builder()
            .credentialInformation(CREDENTIAL_INFORMATIONS)
            .build();

    when(credentialInformationForPersonClient.get(request)).thenReturn(expectedResponse);

    final var result = getCredentialInformationForPersonService.get(request);

    assertEquals(expectedResponse.getCredentialInformation(), result);
  }
}
