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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareProvider;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareProviderRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsaIntegrationOrganizationService implements HsatkOrganizationService {

  private final GetUnitService getUnitService;

  private final GetHealthCareUnitService getHealthCareUnitService;

  private final GetHealthCareUnitMembersService getHealthCareUnitMembersService;

  private final GetHealthCareProviderService getHealthCareProviderService;

  @Override
  public List<HealthCareProvider> getHealthCareProvider(
      String healthCareProviderHsaId, String healthCareProviderOrgNo) {
    return getHealthCareProviderService.get(
        GetHealthCareProviderRequestDTO.builder()
            .hsaId(healthCareProviderHsaId)
            .organizationNumber(healthCareProviderOrgNo)
            .build());
  }

  @Override
  public HealthCareUnit getHealthCareUnit(String healthCareUnitMemberHsaId) {
    final var healthCareUnit =
        getHealthCareUnitService.get(
            GetHealthCareUnitRequestDTO.builder().hsaId(healthCareUnitMemberHsaId).build());
    if (healthCareUnit.getHealthCareUnitHsaId() == null) {
      log.warn("Failed to get HealthCareUnit from HSA with hsaId: {}", healthCareUnitMemberHsaId);
      return null;
    }
    return healthCareUnit;
  }

  @Override
  public HealthCareUnitMembers getHealthCareUnitMembers(String healthCareUnitHsaId) {
    final var healthCareUnitMembers =
        getHealthCareUnitMembersService.get(
            GetHealthCareUnitMembersRequestDTO.builder().hsaId(healthCareUnitHsaId).build());

    if (healthCareUnitMembers.getHealthCareUnitHsaId() == null) {
      log.warn("Unable to find healthCareUnitMembers with hsaId '{}'", healthCareUnitHsaId);
    }
    return healthCareUnitMembers;
  }

  @Override
  public Unit getUnit(String unitHsaId, String profile) {
    return getUnitService.get(GetUnitRequestDTO.builder().hsaId(unitHsaId).build());
  }
}
