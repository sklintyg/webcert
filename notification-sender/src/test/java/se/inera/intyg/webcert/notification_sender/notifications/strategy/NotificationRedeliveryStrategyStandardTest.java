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
package se.inera.intyg.webcert.notification_sender.notifications.strategy;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.temporal.ChronoUnit;
import org.junit.Test;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;

public class NotificationRedeliveryStrategyStandardTest {

    private static final String INVALID_STRATEGY_TEMPLATE = "30#1:m,";
    private static final String VALID_STRATEGY_TEMPLATE = "5#11:s,22:m,33:h,44:d";
    private static final NotificationRedeliveryStrategyEnum strategyName = NotificationRedeliveryStrategyEnum.STANDARD;

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenInitializedWithInvalidTemplate() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRedeliveryStrategyStandard(INVALID_STRATEGY_TEMPLATE));
    }

    @Test
    public void shouldReturnCorrectStrategyIdentifyingEnum() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);
        final var strategyNameEnum = standardStrategy.getName();

        assertEquals(strategyName, strategyNameEnum);
    }

    @Test
    public void shouldReturnCorrectValueMaxDeliveries() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);
        final var maxDeliveries = standardStrategy.getMaxDeliveries();

        assertEquals(5, maxDeliveries);
    }

    @Test
    public void shouldReturnCorrectTimeValuesForAttemptedDeliveriesLowerThanScheduleSize() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);

        final var timeValue1 = standardStrategy.getNextTimeValue(1);
        final var timeValue2 = standardStrategy.getNextTimeValue(2);
        final var timeValue3 = standardStrategy.getNextTimeValue(3);
        final var timeValue4 = standardStrategy.getNextTimeValue(4);

        assertEquals(11, timeValue1);
        assertEquals(22, timeValue2);
        assertEquals(33, timeValue3);
        assertEquals(44, timeValue4);
    }

    @Test
    public void shouldReturnCorrectTimeUnitForAttemptedDeliveriesLowerThanScheduleSize() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);

        final var timeUnit1 = standardStrategy.getNextTimeUnit(1);
        final var timeUnit2 = standardStrategy.getNextTimeUnit(2);
        final var timeUnit3 = standardStrategy.getNextTimeUnit(3);
        final var timeUnit4 = standardStrategy.getNextTimeUnit(4);

        assertEquals(ChronoUnit.SECONDS, timeUnit1);
        assertEquals(ChronoUnit.MINUTES, timeUnit2);
        assertEquals(ChronoUnit.HOURS, timeUnit3);
        assertEquals(ChronoUnit.DAYS, timeUnit4);
    }

    @Test
    public void shouldReturnLastTimeValueForAttemptedDeliveriesHigherThanScheduleSize() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);

        final var timeValue5 = standardStrategy.getNextTimeValue(5);
        final var timeValue100 = standardStrategy.getNextTimeValue(100);

        assertEquals(44, timeValue5);
        assertEquals(44, timeValue100);
    }

    @Test
    public void shouldReturnCorrectTimeUnitForAttemptedDeliveriesHigherThanScheduleSize() {
        final NotificationRedeliveryStrategyStandard standardStrategy = new NotificationRedeliveryStrategyStandard(VALID_STRATEGY_TEMPLATE);

        final var timeUnit5 = standardStrategy.getNextTimeUnit(5);
        final var timeUnit100 = standardStrategy.getNextTimeUnit(100);

        assertEquals(ChronoUnit.DAYS, timeUnit5);
        assertEquals(ChronoUnit.DAYS, timeUnit100);
    }
}
