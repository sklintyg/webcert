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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice.Local;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegratedUnitNotificationEvaluatorTest {

    private static final String CARE_PROVIDER_ID = "careProviderId";
    private static final String ISSUED_ON_UNIT_ID = "issuedOnUnitId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String REGION = "region";
    private static final String NOT_MATCHING_ID = "notMatchingId";
    @Mock
    private GetUnitNotificationConfig getUnitNotificationConfig;
    @InjectMocks
    private IntegratedUnitNotificationEvaluator integratedUnitNotificationEvaluator;

    @Test
    void shallReturnFalseIfNotificationConfigIsEmpty() {
        doReturn(Collections.emptyList()).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now());
        assertFalse(result);
    }

    @Test
    void shallReturnFalseIfNotificationConfigDoesNotContainAnyMatchingUnitOrCareUnitId() {
        final var regionNotificationConfig = RegionNotificationConfig.builder()
            .region(REGION)
            .configuration(
                List.of(
                    IntegratedUnitNotificationConfig.builder()
                        .careProviders(List.of(NOT_MATCHING_ID))
                        .issuedOnUnit(List.of(NOT_MATCHING_ID))
                        .build()
                ))
            .build();

        doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now());
        assertFalse(result);
    }

    @Test
    void shallReturnFalseIfIssuingDateIsBeforeConfigurationDateButActivationDateIsNotPassed() {
        final var regionNotificationConfig = RegionNotificationConfig.builder()
            .region(REGION)
            .configuration(
                List.of(
                    IntegratedUnitNotificationConfig.builder()
                        .careProviders(List.of(CARE_PROVIDER_ID))
                        .issuedOnUnit(List.of(ISSUED_ON_UNIT_ID))
                        .datetime(LocalDateTime.now().plusDays(1))
                        .build()
                ))
            .build();

        doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now().minusDays(10));
        assertFalse(result);
    }

    @Test
    void shallReturnTrueIfIssuingDateIsBeforeConfigurationDateAndActivationDateIsPassed() {
        final var regionNotificationConfig = RegionNotificationConfig.builder()
            .region(REGION)
            .configuration(
                List.of(
                    IntegratedUnitNotificationConfig.builder()
                        .careProviders(List.of(CARE_PROVIDER_ID))
                        .issuedOnUnit(List.of(ISSUED_ON_UNIT_ID))
                        .datetime(LocalDateTime.now().minusDays(1))
                        .build()
                ))
            .build();

        doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now().minusDays(2));
        assertTrue(result);
    }

    @Test
    void shallReturnFalseIfIssuingDateIsBeforeConfigurationDateButDoesNotMatchAnyUnitId() {
        final var regionNotificationConfig = RegionNotificationConfig.builder()
            .region(REGION)
            .configuration(
                List.of(
                    IntegratedUnitNotificationConfig.builder()
                        .careProviders(List.of(NOT_MATCHING_ID))
                        .issuedOnUnit(List.of(NOT_MATCHING_ID))
                        .datetime(LocalDateTime.now().minusDays(1))
                        .build()
                ))
            .build();

        doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now().minusDays(10));
        assertFalse(result);
    }

    @Test
    void shallReturnTrueIfIssuingDateIsBeforeConfigurationDateAndActivationDateIsPassedOnSeconds() {
        final var regionNotificationConfig = RegionNotificationConfig.builder()
            .region(REGION)
            .configuration(
                List.of(
                    IntegratedUnitNotificationConfig.builder()
                        .careProviders(List.of(CARE_PROVIDER_ID))
                        .issuedOnUnit(List.of(ISSUED_ON_UNIT_ID))
                        .datetime(LocalDateTime.now().minusSeconds(1))
                        .build()
                ))
            .build();

        doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
        final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
            LocalDateTime.now().minusSeconds(10));
        assertTrue(result);
    }

    @Nested
    class NullValueScenarios {

        @Test
        void shallHandleMatchingValuesWithNullValuesForCareProviderIds() {
            final var regionNotificationConfig = RegionNotificationConfig.builder()
                .region(REGION)
                .configuration(
                    List.of(
                        IntegratedUnitNotificationConfig.builder()
                            .issuedOnUnit(List.of(ISSUED_ON_UNIT_ID))
                            .datetime(LocalDateTime.now().minusDays(1))
                            .build()
                    ))
                .build();

            doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
            final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
                LocalDateTime.now().minusDays(10));
            assertTrue(result);
        }

        @Test
        void shallHandleMatchingValuesWithNullValuesForIssuedOnUnitId() {
            final var regionNotificationConfig = RegionNotificationConfig.builder()
                .region(REGION)
                .configuration(
                    List.of(
                        IntegratedUnitNotificationConfig.builder()
                            .careProviders(List.of(CARE_PROVIDER_ID))
                            .datetime(LocalDateTime.now().minusDays(1))
                            .build()
                    ))
                .build();

            doReturn(List.of(regionNotificationConfig)).when(getUnitNotificationConfig).get();
            final var result = integratedUnitNotificationEvaluator.mailNotification(CARE_PROVIDER_ID, ISSUED_ON_UNIT_ID, CERTIFICATE_ID,
                LocalDateTime.now().minusDays(10));
            assertTrue(result);
        }
    }
}
