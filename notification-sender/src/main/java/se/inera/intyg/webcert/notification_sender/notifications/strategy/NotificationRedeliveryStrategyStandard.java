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

import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import jakarta.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.util.Pair;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;


public class NotificationRedeliveryStrategyStandard implements NotificationRedeliveryStrategy {

    private static final NotificationRedeliveryStrategyEnum STRATEGY_NAME = STANDARD;
    private final String strategyTemplateString;
    private int maxDeliveries;
    private List<Pair<ChronoUnit, Integer>> notificationRedeliverySchedule;

    public NotificationRedeliveryStrategyStandard(@Nonnull String strategyTemplateString) {
        throwExceptionIfInvalidTemplate(strategyTemplateString);
        this.strategyTemplateString = strategyTemplateString;
    }

    @Override
    public NotificationRedeliveryStrategyEnum getName() {
        return STRATEGY_NAME;
    }

    @Override
    public int getMaxDeliveries() {
        assertHasNotificationStrategy();
        return maxDeliveries;
    }

    @Override
    public ChronoUnit getNextTimeUnit(int attemptedDeliveries) {
        assertHasNotificationStrategy();

        int attemptedRedeliveries = calculateAttemptedRedeliveries(attemptedDeliveries);

        if (attemptedRedeliveries < notificationRedeliverySchedule.size()) {
            return notificationRedeliverySchedule.get(attemptedRedeliveries).getFirst();
        } else {
            return notificationRedeliverySchedule.get(notificationRedeliverySchedule.size() - 1).getFirst();
        }
    }

    @Override
    public int getNextTimeValue(int attemptedDeliveries) {
        assertHasNotificationStrategy();

        int attemptedRedeliveries = calculateAttemptedRedeliveries(attemptedDeliveries);

        if (attemptedRedeliveries < notificationRedeliverySchedule.size()) {
            return notificationRedeliverySchedule.get(attemptedRedeliveries).getSecond();
        } else {
            return notificationRedeliverySchedule.get(notificationRedeliverySchedule.size() - 1).getSecond();
        }
    }

    private int calculateAttemptedRedeliveries(int attemptedDeliveries) {
        return attemptedDeliveries - 1;
    }

    private void throwExceptionIfInvalidTemplate(String strategyTemplateString) {
        if (!strategyTemplateString.matches(STRATEGY_TEMPLATE_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Strategy template string sent to NotificationRedeliveryStrategyStandard "
                + " does not match the required format.");
        }
    }

    private void assertHasNotificationStrategy() {
        if (notificationRedeliverySchedule == null) {
            buildRedeliveryStrategyFromTemplateString();
        }
    }

    private void buildRedeliveryStrategyFromTemplateString() {
        final var splitStrategyTemplate = strategyTemplateString.split("#");
        maxDeliveries = Integer.parseInt(splitStrategyTemplate[0]);
        notificationRedeliverySchedule = createRedliveryScheduleFromTemplate(splitStrategyTemplate[1]);
    }

    private List<Pair<ChronoUnit, Integer>> createRedliveryScheduleFromTemplate(String redeliveryScheduleTemplate) {
        final var notificationRedeliverySchedule = new ArrayList<Pair<ChronoUnit, Integer>>();
        final var splitRedeliveryScheduleTemplate = redeliveryScheduleTemplate.split(",");
        for (final var unitValuePair : splitRedeliveryScheduleTemplate) {
            String[] splitUnitValue = unitValuePair.split(":");
            Integer timeValue = Integer.parseInt(splitUnitValue[0]);
            ChronoUnit timeUnit = getTimeUnit(splitUnitValue[1]);

            notificationRedeliverySchedule.add(Pair.of(timeUnit, timeValue));
        }
        return notificationRedeliverySchedule;
    }

    private ChronoUnit getTimeUnit(String timeUnitString) {
        switch (timeUnitString) {
            case "s":
                return ChronoUnit.SECONDS;
            case "m":
                return ChronoUnit.MINUTES;
            case "h":
                return ChronoUnit.HOURS;
            default:
                return ChronoUnit.DAYS;
        }
    }
}
