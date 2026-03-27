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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceUnitClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetUnitServiceTest {

  private static final String HSA_ID = "hsaId";
  @Mock HsaIntygProxyServiceUnitClient hsaIntygProxyServiceUnitClient;
  @InjectMocks private GetUnitService getUnitService;

  @Test
  void shouldValidateRequest() {
    final var request = GetUnitRequestDTO.builder().build();
    assertThrows(IllegalArgumentException.class, () -> getUnitService.get(request));
  }

  @Test
  void shouldReturnNullIfNoUnitWasFound() {
    final var request = GetUnitRequestDTO.builder().hsaId(HSA_ID).build();

    final var response = GetUnitResponseDTO.builder().unit(null).build();

    when(hsaIntygProxyServiceUnitClient.getUnit(request)).thenReturn(response);

    final var result = getUnitService.get(request);
    assertNull(result);
  }

  @Test
  void shouldReturnUnit() {
    final var expectedUnit = new Unit();

    final var request = GetUnitRequestDTO.builder().hsaId(HSA_ID).build();

    final var response = GetUnitResponseDTO.builder().unit(expectedUnit).build();

    when(hsaIntygProxyServiceUnitClient.getUnit(request)).thenReturn(response);

    final var result = getUnitService.get(request);

    assertEquals(expectedUnit, result);
  }
}
