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
package se.inera.intyg.webcert.infra.security.authorities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;

class CommonAuthoritiesResolverTest {

  private static final String authoritiesConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
  private static final String featuresConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/features-test.yaml";
  private static final Integer defaultMaxAliasesForCollections = 300;

  private static final SecurityConfigurationLoader configurationLoader =
      new SecurityConfigurationLoader(
          authoritiesConfigurationLocation,
          featuresConfigurationLocation,
          defaultMaxAliasesForCollections);

  private static final CommonAuthoritiesResolver commonAuthoritiesResolver =
      new CommonAuthoritiesResolver();

  @BeforeAll
  static void setupAuthoritiesConfiguration() {
    // Load configuration
    configurationLoader.afterPropertiesSet();

    // Setup resolver class
    commonAuthoritiesResolver.setConfigurationLoader(configurationLoader);
  }

  @Test
  void testFeaturesDisabled() {
    final var availableFeatures = commonAuthoritiesResolver.getFeatures(Collections.emptyList());
    assertFalse(
        availableFeatures
            .get(AuthoritiesConstants.FEATURE_ENABLE_WARNING_ORIGIN_NORMAL)
            .getGlobal());
    assertFalse(
        availableFeatures.get(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL).getGlobal());
  }

  @Test
  void testFeaturesEnabled() {
    final var availableFeatures =
        commonAuthoritiesResolver.getFeatures(Collections.singletonList("TSTNMT2321000156-1077"));
    assertTrue(
        availableFeatures
            .get(AuthoritiesConstants.FEATURE_ENABLE_WARNING_ORIGIN_NORMAL)
            .getGlobal());
    assertTrue(
        availableFeatures.get(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL).getGlobal());
  }
}
