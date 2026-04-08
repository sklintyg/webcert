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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareProvider;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareProviderRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;

@ExtendWith(MockitoExtension.class)
class HsaIntegrationOrganizationServiceTest {

  private static final String UNIT_HSA_ID = "unitHsaId";
  private static final String HSA_ID = "hsaId";

  @Mock private GetUnitService getUnitService;

  @Mock private GetHealthCareUnitMembersService getHealthCareUnitMembersService;

  @Mock private GetHealthCareUnitService getHealthCareUnitService;

  @Mock private GetHealthCareProviderService getHealthCareProviderService;

  @InjectMocks private HsaIntegrationOrganizationService hsaIntegrationOrganizationService;

  @Nested
  class GetHealthCareUnit {

    @Test
    void shouldReturnHealthCareUnit() {
      final var expectedUnit = new HealthCareUnit();
      expectedUnit.setHealthCareUnitHsaId(HSA_ID);
      when(getHealthCareUnitService.get(any(GetHealthCareUnitRequestDTO.class)))
          .thenReturn(expectedUnit);

      final var result = hsaIntegrationOrganizationService.getHealthCareUnit(UNIT_HSA_ID);

      assertEquals(expectedUnit, result);
    }

    @Test
    void shouldReturnNullIfHealthCareUnitIsNull() {
      final var healthCareUnit = new HealthCareUnit();
      when(getHealthCareUnitService.get(any(GetHealthCareUnitRequestDTO.class)))
          .thenReturn(healthCareUnit);

      final var result = hsaIntegrationOrganizationService.getHealthCareUnit(UNIT_HSA_ID);

      assertNull(result);
    }
  }

  @Nested
  class GetUnit {

    @Test
    void shouldReturnNullIfUnitIsNull() {
      when(getUnitService.get(any(GetUnitRequestDTO.class))).thenReturn(null);

      final var result = hsaIntegrationOrganizationService.getUnit(UNIT_HSA_ID, null);

      assertNull(result);
    }

    @Test
    void shouldReturnResponseFromService() {
      final var expected = new Unit();
      when(getUnitService.get(any(GetUnitRequestDTO.class))).thenReturn(expected);

      final var result = hsaIntegrationOrganizationService.getUnit(UNIT_HSA_ID, null);

      assertEquals(expected, result);
    }
  }

  @Nested
  class GetHealthCareUnitMembers {

    @Test
    void shouldReturnResponseFromService() {
      final var expected = new HealthCareUnitMembers();
      when(getHealthCareUnitMembersService.get(any(GetHealthCareUnitMembersRequestDTO.class)))
          .thenReturn(expected);

      final var result = hsaIntegrationOrganizationService.getHealthCareUnitMembers(UNIT_HSA_ID);

      assertEquals(expected, result);
    }
  }

  @Nested
  class GetHealthCareProviders {

    @Test
    void shouldReturnResponseFromService() {
      final var expected = List.of(new HealthCareProvider());
      when(getHealthCareProviderService.get(any(GetHealthCareProviderRequestDTO.class)))
          .thenReturn(expected);

      final var result =
          hsaIntegrationOrganizationService.getHealthCareProvider("HSA_ID", "ORG_NO");

      assertEquals(expected, result);
    }
  }
}
