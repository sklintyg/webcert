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
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceHospLastUpdateClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetHospLastUpdateResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetHospLastUpdateServiceTest {

  @Mock private HsaIntygProxyServiceHospLastUpdateClient hospLastUpdateClient;

  @InjectMocks private GetHospLastUpdateService getHospLastUpdateService;

  @Test
  void shouldReturnHospLastUpdate() {
    final var expectedResponse =
        GetHospLastUpdateResponseDTO.builder().lastUpdate(LocalDateTime.now()).build();

    when(hospLastUpdateClient.get()).thenReturn(expectedResponse);

    final var result = getHospLastUpdateService.get();

    assertEquals(expectedResponse.getLastUpdate(), result);
  }
}
