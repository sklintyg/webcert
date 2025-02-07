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

import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.SINGLE;

import com.google.common.collect.ImmutableList;
import java.time.temporal.ChronoUnit;
import org.springframework.data.util.Pair;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;

public class NotificationRedeliveryStrategySingle implements NotificationRedeliveryStrategy {

    private static final NotificationRedeliveryStrategyEnum STRATEGY_NAME = SINGLE;
    private static final int MAX_DELIVERIES = 1;
    private static final ImmutableList<Pair<ChronoUnit, Integer>> NOTIFICATION_REDELIVERY_SCHEDULE = ImmutableList.of(
        Pair.of(ChronoUnit.SECONDS, 10)
    );

    public NotificationRedeliveryStrategySingle() {
    }

    @Override
    public NotificationRedeliveryStrategyEnum getName() {
        return STRATEGY_NAME;
    }

    @Override
    public int getMaxDeliveries() {
        return MAX_DELIVERIES;
    }

    @Override
    public ChronoUnit getNextTimeUnit(int attemptedDeliveries) {

        int attemptedRedeliveries = calculateAttemptedRedeliveries(attemptedDeliveries);

        if (attemptedRedeliveries < NOTIFICATION_REDELIVERY_SCHEDULE.size()) {
            return NOTIFICATION_REDELIVERY_SCHEDULE.get(attemptedRedeliveries).getFirst();
        } else {
            return NOTIFICATION_REDELIVERY_SCHEDULE.get(NOTIFICATION_REDELIVERY_SCHEDULE.size() - 1).getFirst();
        }
    }

    @Override
    public int getNextTimeValue(int attemptedDeliveries) {

        int attemptedRedeliveries = calculateAttemptedRedeliveries(attemptedDeliveries);

        if (attemptedRedeliveries < NOTIFICATION_REDELIVERY_SCHEDULE.size()) {
            return NOTIFICATION_REDELIVERY_SCHEDULE.get(attemptedRedeliveries).getSecond();
        } else {
            return NOTIFICATION_REDELIVERY_SCHEDULE.get(NOTIFICATION_REDELIVERY_SCHEDULE.size() - 1).getSecond();
        }
    }

    private int calculateAttemptedRedeliveries(int attemptedDeliveries) {
        return attemptedDeliveries - 1;
    }
}
