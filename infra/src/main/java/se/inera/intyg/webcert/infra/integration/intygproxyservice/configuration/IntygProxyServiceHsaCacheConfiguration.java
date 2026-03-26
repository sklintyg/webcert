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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.configuration;

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.EMPLOYEE_CACHE_NAME;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_PROVIDER_CACHE_NAME;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_CACHE_NAME;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.UNIT_CACHE_NAME;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.infra.rediscache.core.RedisCacheOptionsSetter;

@Configuration
@RequiredArgsConstructor
public class IntygProxyServiceHsaCacheConfiguration {

  private final RedisCacheOptionsSetter redisCacheOptionsSetter;

  @Value("${hsa.intygproxyservice.getemployee.cache.expiry:60}")
  private String hsaEmployeeCacheExpirySeconds;

  @Value("${hsa.intygproxyservice.gethealthcareunit.cache.expiry:60}")
  private String hsaHealthCareUnitCacheExpirySeconds;

  @Value("${hsa.intygproxyservice.gethealthcareunitmembers.cache.expiry:60}")
  private String hsaHealthCareUnitMembersCacheExpirySeconds;

  @Value("${hsa.intygproxyservice.getunit.cache.expiry:60}")
  private String hsaUnitCacheExpirySeconds;

  @Value("${hsa.intygproxyservice.gethealthcareprovider.cache.expiry:60}")
  private String hsaHealthCareProviderCacheExpirySeconds;

  @Bean(name = EMPLOYEE_CACHE_NAME)
  public Cache hsaIntygProxyServiceEmployeeCache() {
    return redisCacheOptionsSetter.createCache(EMPLOYEE_CACHE_NAME, hsaEmployeeCacheExpirySeconds);
  }

  @Bean(name = HEALTH_CARE_UNIT_CACHE_NAME)
  public Cache hsaIntygProxyServiceHealthCareUnitCache() {
    return redisCacheOptionsSetter.createCache(
        HEALTH_CARE_UNIT_CACHE_NAME, hsaHealthCareUnitCacheExpirySeconds);
  }

  @Bean(name = HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME)
  public Cache hsaIntygProxyServiceHealthCareUnitMembersCache() {
    return redisCacheOptionsSetter.createCache(
        HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME, hsaHealthCareUnitMembersCacheExpirySeconds);
  }

  @Bean(name = UNIT_CACHE_NAME)
  public Cache hsaIntygProxyServiceUnitCache() {
    return redisCacheOptionsSetter.createCache(UNIT_CACHE_NAME, hsaUnitCacheExpirySeconds);
  }

  @Bean(name = HEALTH_CARE_PROVIDER_CACHE_NAME)
  public Cache hsaHealthCareProviderCache() {
    return redisCacheOptionsSetter.createCache(
        HEALTH_CARE_PROVIDER_CACHE_NAME, hsaHealthCareProviderCacheExpirySeconds);
  }
}
