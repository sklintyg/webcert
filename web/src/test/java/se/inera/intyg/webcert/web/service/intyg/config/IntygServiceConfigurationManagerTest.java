/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

public class IntygServiceConfigurationManagerTest {

    private IntygServiceConfigurationManagerImpl configurationManager;

    @Before
    public void setup() {
        configurationManager = new IntygServiceConfigurationManagerImpl();
        configurationManager.setObjectMapper(new CustomObjectMapper());
    }

    @Test
    public void testUnmarshallForSendIntygWithConsent() {

        String configAsJson = "{\"recipient\":\"FK\"}";

        SendIntygConfiguration config = configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

        assertNotNull(config);
        assertEquals("FK", config.getRecipient());
        assertTrue(config.getPatientConsentMessage().contains("mottagare FK"));
    }

    @Test
    public void testUnmarshallForSendIntygWithoutConsentToTs() {

        String configAsJson = "{\"recipient\":\"TS\"}";

        SendIntygConfiguration config = configurationManager.unmarshallConfig(configAsJson, SendIntygConfiguration.class);

        assertNotNull(config);
        assertEquals("TS", config.getRecipient());
        assertTrue(config.getPatientConsentMessage().contains("mottagare TS"));
    }

}
