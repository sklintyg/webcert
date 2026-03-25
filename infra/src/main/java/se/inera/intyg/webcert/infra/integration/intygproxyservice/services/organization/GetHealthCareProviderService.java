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

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_PROVIDER_CACHE_NAME;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareProvider;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization.HsaIntygProxyServiceHealthCareProviderClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareProviderRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetHealthCareProviderService {

  private final HsaIntygProxyServiceHealthCareProviderClient
      hsaIntygProxyServiceHealthCareProviderClient;

  @Cacheable(
      cacheNames = HEALTH_CARE_PROVIDER_CACHE_NAME,
      key = "#request.hsaId",
      unless = "#result == null")
  public List<HealthCareProvider> get(GetHealthCareProviderRequestDTO request) {
    validateRequest(request);
    final var response = hsaIntygProxyServiceHealthCareProviderClient.get(request);
    if (response == null
        || response.getHealthCareProviders() == null
        || response.getHealthCareProviders().isEmpty()) {
      log.warn(
          "No health care providers were found for hsaId '{}' or organizationNumber '{}', returning empty list",
          request.getHsaId(),
          request.getOrganizationNumber());
      return Collections.emptyList();
    }
    return response.getHealthCareProviders();
  }

  private void validateRequest(GetHealthCareProviderRequestDTO request) {
    if (isParameterDefined(request.getHsaId())
        && isParameterDefined(request.getOrganizationNumber())) {
      throw new IllegalArgumentException("Both hsaId and organizationNumber cannot be defined");
    }

    if (!isParameterDefined(request.getHsaId())
        && !isParameterDefined(request.getOrganizationNumber())) {
      throw new IllegalArgumentException("One of hsaId or organizationNumber has to be defined");
    }
  }

  private boolean isParameterDefined(String value) {
    return value != null && !value.isEmpty();
  }
}
