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

import java.time.temporal.ChronoUnit;
import org.junit.Test;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;

public class NotificationRedeliveryStrategySingleTest {

    private static final NotificationRedeliveryStrategyEnum strategyName = NotificationRedeliveryStrategyEnum.SINGLE;

    @Test
    public void shouldReturnCorrectStrategyIdentifyingEnum() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var strategyNameEnum = standardStrategy.getName();

        assertEquals(strategyName, strategyNameEnum);
    }

    @Test
    public void shouldReturnCorrectValueMaxDeliveries() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var maxDeliveries = standardStrategy.getMaxDeliveries();

        assertEquals(1, maxDeliveries);
    }

    @Test
    public void shouldReturnCorrectTimeValuesForAttemptedDeliveriesLowerThanScheduleSize() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var timeValue1 = standardStrategy.getNextTimeValue(1);

        assertEquals(10, timeValue1);
    }

    @Test
    public void shouldReturnCorrectTimeUnitForAttemptedDeliveriesLowerThanScheduleSize() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var timeUnit1 = standardStrategy.getNextTimeUnit(1);

        assertEquals(ChronoUnit.SECONDS, timeUnit1);
    }

    @Test
    public void shouldReturnLastTimeValueForAttemptedDeliveriesHigherThanScheduleSize() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var timeValue2 = standardStrategy.getNextTimeValue(2);
        final var timeValue100 = standardStrategy.getNextTimeValue(100);

        assertEquals(10, timeValue2);
        assertEquals(10, timeValue100);
    }

    @Test
    public void shouldReturnCorrectTimeUnitForAttemptedDeliveriesHigherThanScheduleSize() {
        final NotificationRedeliveryStrategySingle standardStrategy = new NotificationRedeliveryStrategySingle();
        final var timeUnit5 = standardStrategy.getNextTimeUnit(5);
        final var timeUnit100 = standardStrategy.getNextTimeUnit(100);

        assertEquals(ChronoUnit.SECONDS, timeUnit5);
        assertEquals(ChronoUnit.SECONDS, timeUnit100);
    }
}
