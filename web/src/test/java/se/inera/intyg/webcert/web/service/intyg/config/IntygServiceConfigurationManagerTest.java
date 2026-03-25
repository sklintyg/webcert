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
package se.inera.intyg.webcert.web.service.intyg.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

class IntygServiceConfigurationManagerTest {

  private IntygServiceConfigurationManagerImpl configurationManager;

  @BeforeEach
  void setup() {
    configurationManager = new IntygServiceConfigurationManagerImpl();
    configurationManager.setObjectMapper(new CustomObjectMapper());
  }

  @Test
  void testUnmarshallForSendIntygWithConsent() {

    String configAsJson = "{\"recipient\":\"FKASSA\"}";

    SendIntygConfiguration config =
        configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

    assertNotNull(config);
    assertEquals(config.getRecipient(), "FKASSA");
    assertTrue(config.getPatientConsentMessage().contains("mottagare FKASSA"));
  }

  @Test
  void testUnmarshallForSendIntygWithoutConsentToTs() {

    String configAsJson = "{\"recipient\":\"TRANSP\"}";

    SendIntygConfiguration config =
        configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

    assertNotNull(config);
    assertEquals(config.getRecipient(), "TRANSP");
    assertTrue(config.getPatientConsentMessage().contains("mottagare TRANSP"));
  }
}
