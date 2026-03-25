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

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_CACHE_NAME;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceHealthCareUnitClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitRequestDTO;

@Service
@RequiredArgsConstructor
public class GetHealthCareUnitService {

  private final HsaIntygProxyServiceHealthCareUnitClient hsaIntygProxyServiceHealthCareUnitClient;

  @Cacheable(
      cacheNames = HEALTH_CARE_UNIT_CACHE_NAME,
      key = "#getHealthCareUnitRequestDTO.hsaId",
      unless = "#result == null")
  public HealthCareUnit get(GetHealthCareUnitRequestDTO getHealthCareUnitRequestDTO) {
    validateRequestParameters(getHealthCareUnitRequestDTO);
    final var healthCareUnit =
        hsaIntygProxyServiceHealthCareUnitClient.getHealthCareUnit(getHealthCareUnitRequestDTO);
    return healthCareUnit.getHealthCareUnit();
  }

  private void validateRequestParameters(GetHealthCareUnitRequestDTO getHealthCareUnitRequestDTO) {
    if (hsaIdIsNullOrEmpty(getHealthCareUnitRequestDTO)) {
      throw new IllegalArgumentException(
          "Missing required parameters. Must provide careUnitHsaId.");
    }
  }

  private static boolean hsaIdIsNullOrEmpty(
      GetHealthCareUnitRequestDTO getHealthCareUnitRequestDTO) {
    return getHealthCareUnitRequestDTO.getHsaId() == null
        || getHealthCareUnitRequestDTO.getHsaId().isEmpty();
  }
}
