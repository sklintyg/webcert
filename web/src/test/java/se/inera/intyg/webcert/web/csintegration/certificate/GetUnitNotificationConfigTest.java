/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetUnitNotificationConfigTest {

    private static final String NOTIFICATIONS_WITH_VALUE_PATH = Paths.get("src", "test", "resources", "UnitNotificationConfig",
        "unit-notification-config-with-value.json").toString();
    private static final String INVALID_PATH = "InvalidPath";
    private static final String NOTIFICATIONS_WITH_VALUES_PATH = Paths.get("src", "test", "resources", "UnitNotificationConfig",
        "unit-notification-config-with-values.json").toString();
    private static final String NOTIFICATIONS_WITH_VALUE_AND_MULTIPLE_CONFIGURATIONS_PATH =
        Paths.get("src", "test", "resources", "UnitNotificationConfig",
            "unit-notification-config-with-value-with-multiple-configurations.json").toString();
    @InjectMocks
    private GetUnitNotificationConfig getUnitNotificationConfig;


    @Test
    void shallReturnEmptyListIfPathIsInvalid() {
        ReflectionTestUtils.setField(getUnitNotificationConfig, "unitNotificationConfigPath", INVALID_PATH);
        final var result = getUnitNotificationConfig.get();
        assertTrue(result.isEmpty());
    }

    @Test
    void shallReturnListOfIntegratedUnitNotificationConfig() {
        ReflectionTestUtils.setField(getUnitNotificationConfig, "unitNotificationConfigPath", NOTIFICATIONS_WITH_VALUE_PATH);
        final var result = getUnitNotificationConfig.get();
        assertFalse(result.isEmpty());
    }

    @Test
    void shallReturnListOfIntegratedUnitNotificationConfigWithMultipleConfigurations() {
        ReflectionTestUtils.setField(getUnitNotificationConfig, "unitNotificationConfigPath",
            NOTIFICATIONS_WITH_VALUE_AND_MULTIPLE_CONFIGURATIONS_PATH);
        final var result = getUnitNotificationConfig.get();
        assertEquals(2, result.get(0).getConfiguration().size());
    }

    @Test
    void shallReturnListOfIntegratedUnitNotificationConfigWithMultipleRegions() {
        ReflectionTestUtils.setField(getUnitNotificationConfig, "unitNotificationConfigPath", NOTIFICATIONS_WITH_VALUES_PATH);
        final var result = getUnitNotificationConfig.get();
        assertEquals(2, result.size());
    }
}
