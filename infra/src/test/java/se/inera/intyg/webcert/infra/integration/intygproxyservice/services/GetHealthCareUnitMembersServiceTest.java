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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceHealthCareUnitMembersClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersResponseDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetHealthCareUnitMembersService;

@ExtendWith(MockitoExtension.class)
class GetHealthCareUnitMembersServiceTest {

  @Mock private HsaIntygProxyServiceHealthCareUnitMembersClient healthCareUnitMembersClient;
  @InjectMocks private GetHealthCareUnitMembersService getHealthCareUnitMembersService;

  private static final String EMPTY = "";
  private static final String HSA_ID = "hsaId";

  @Test
  void shouldThrowIfProvidedHsaIdFromRequestIsNull() {
    final var request = GetHealthCareUnitMembersRequestDTO.builder().build();
    assertThrows(
        IllegalArgumentException.class, () -> getHealthCareUnitMembersService.get(request));
  }

  @Test
  void shouldThrowIfProvidedHsaIdFromRequestIsEmpty() {
    final var request = GetHealthCareUnitMembersRequestDTO.builder().hsaId(EMPTY).build();
    assertThrows(
        IllegalArgumentException.class, () -> getHealthCareUnitMembersService.get(request));
  }

  @Test
  void shouldReturnHealthCareUnitMembersFromClient() {
    final var request = GetHealthCareUnitMembersRequestDTO.builder().hsaId(HSA_ID).build();
    final var expectedResponse = new HealthCareUnitMembers();
    when(healthCareUnitMembersClient.get(request))
        .thenReturn(
            GetHealthCareUnitMembersResponseDTO.builder()
                .healthCareUnitMembers(expectedResponse)
                .build());
    final var result = getHealthCareUnitMembersService.get(request);
    assertEquals(expectedResponse, result);
  }
}
