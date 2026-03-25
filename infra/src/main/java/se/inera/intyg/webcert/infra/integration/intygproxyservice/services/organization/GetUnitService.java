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

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.UNIT_CACHE_NAME;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceUnitClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUnitService {

  private final HsaIntygProxyServiceUnitClient hsaIntygProxyServiceUnitClient;

  @Cacheable(
      cacheNames = UNIT_CACHE_NAME,
      key = "#getUnitRequestDTO.hsaId",
      unless = "#result == null")
  public Unit get(GetUnitRequestDTO getUnitRequestDTO) {
    validateRequest(getUnitRequestDTO);
    final var getUnitResponseDTO = hsaIntygProxyServiceUnitClient.getUnit(getUnitRequestDTO);
    if (invalidResponseOrNoUnitFound(getUnitResponseDTO)) {
      log.warn("No unit was found with hsaId '{}', returning null", getUnitRequestDTO.getHsaId());
      return null;
    }
    return getUnitResponseDTO.getUnit();
  }

  private void validateRequest(GetUnitRequestDTO getUnitRequestDTO) {
    if (getUnitRequestDTO.getHsaId() == null || getUnitRequestDTO.getHsaId().isEmpty()) {
      throw new IllegalArgumentException("hsaId is a required field");
    }
  }

  private boolean invalidResponseOrNoUnitFound(GetUnitResponseDTO getUnitResponseDTO) {
    return getUnitResponseDTO == null || getUnitResponseDTO.getUnit() == null;
  }
}
