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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareProvider;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceHealthCareProviderClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareProviderRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareProviderResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetHealthCareProviderServiceTest {

  private static final String HSA_ID = "HSA_ID";
  private static final String ORG_NO = "ORG_NO";

  @Mock HsaIntygProxyServiceHealthCareProviderClient hsaIntygProxyServiceHealthCareProviderClient;

  @InjectMocks private GetHealthCareProviderService getHealthCareProviderService;

  @Test
  void shouldThrowErrorIfRequestHasBothParametersAsNull() {
    final var request = GetHealthCareProviderRequestDTO.builder().build();
    assertThrows(IllegalArgumentException.class, () -> getHealthCareProviderService.get(request));
  }

  @Test
  void shouldThrowErrorIfRequestHasBothParametersAsEmpty() {
    final var request =
        GetHealthCareProviderRequestDTO.builder().hsaId("").organizationNumber("").build();
    assertThrows(IllegalArgumentException.class, () -> getHealthCareProviderService.get(request));
  }

  @Test
  void shouldThrowErrorIfRequestHasBothParametersAsDefined() {
    final var request =
        GetHealthCareProviderRequestDTO.builder().hsaId(HSA_ID).organizationNumber(ORG_NO).build();

    assertThrows(IllegalArgumentException.class, () -> getHealthCareProviderService.get(request));
  }

  @Test
  void shouldReturnEmptyListIfNull() {
    final var request = GetHealthCareProviderRequestDTO.builder().hsaId(HSA_ID).build();

    final var result = getHealthCareProviderService.get(request);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void shouldReturnEmptyListIfCareProvidersIsNull() {
    final var request = GetHealthCareProviderRequestDTO.builder().hsaId(HSA_ID).build();
    when(hsaIntygProxyServiceHealthCareProviderClient.get(request))
        .thenReturn(GetHealthCareProviderResponseDTO.builder().build());

    final var result = getHealthCareProviderService.get(request);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void shouldReturnEmptyListIfCareProvidersIsEmpty() {
    final var request = GetHealthCareProviderRequestDTO.builder().hsaId(HSA_ID).build();
    when(hsaIntygProxyServiceHealthCareProviderClient.get(request))
        .thenReturn(
            GetHealthCareProviderResponseDTO.builder()
                .healthCareProviders(Collections.emptyList())
                .build());

    final var result = getHealthCareProviderService.get(request);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void shouldReturnProvidersFromClient() {
    final var expected = List.of(new HealthCareProvider());
    final var request = GetHealthCareProviderRequestDTO.builder().hsaId(HSA_ID).build();
    final var response =
        GetHealthCareProviderResponseDTO.builder().healthCareProviders(expected).build();
    when(hsaIntygProxyServiceHealthCareProviderClient.get(request)).thenReturn(response);

    final var result = getHealthCareProviderService.get(request);

    assertEquals(expected, result);
  }
}
