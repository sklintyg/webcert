/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy;

import com.google.common.collect.ImmutableList;
import java.time.temporal.ChronoUnit;
import org.springframework.data.util.Pair;


public class NotificationRedeliveryStrategyStandard implements NotificationRedeliveryStrategy {

    private static final String STRATEGY_NAME = "STANDARD";
    private static final int MAX_REDELIVERIES = 5;
    private static final ImmutableList<Pair<ChronoUnit, Integer>> NOTIFICATION_REDELIVERY_SCHEME = ImmutableList.of(
        Pair.of(ChronoUnit.MINUTES, 1),
        Pair.of(ChronoUnit.MINUTES, 1),
        Pair.of(ChronoUnit.MINUTES, 1)
        // Pair.of(ChronoUnit.MINUTES, 1),
        // Pair.of(ChronoUnit.MINUTES, 10),
        // Pair.of(ChronoUnit.HOURS, 1),
        // Pair.of(ChronoUnit.HOURS, 4),
        // Pair.of(ChronoUnit.HOURS, 12),
        // Pair.of(ChronoUnit.HOURS, 24),
        // Pair.of(ChronoUnit.WEEKS, 1)
    );

    public NotificationRedeliveryStrategyStandard() { }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public int getMaxRedeliveries() {
        return MAX_REDELIVERIES;
    }

    @Override
    public ChronoUnit getNextTimeUnit(int attemptedRedeliveries) {
        if (attemptedRedeliveries < NOTIFICATION_REDELIVERY_SCHEME.size()) {
            return NOTIFICATION_REDELIVERY_SCHEME.get(attemptedRedeliveries).getFirst();
        } else {
            return NOTIFICATION_REDELIVERY_SCHEME.get(NOTIFICATION_REDELIVERY_SCHEME.size() - 1).getFirst();
        }
    }

    @Override
    public int getNextTimeValue(int attemptedRedeliveries) {
        if (attemptedRedeliveries < NOTIFICATION_REDELIVERY_SCHEME.size()) {
            return NOTIFICATION_REDELIVERY_SCHEME.get(attemptedRedeliveries).getSecond();
        } else {
            return NOTIFICATION_REDELIVERY_SCHEME.get(NOTIFICATION_REDELIVERY_SCHEME.size() - 1).getSecond();
        }
    }
}