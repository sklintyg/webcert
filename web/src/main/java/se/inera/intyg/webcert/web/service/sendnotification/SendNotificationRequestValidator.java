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

package se.inera.intyg.webcert.web.service.sendnotification;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SendNotificationRequestValidator {

    public void validateId(String id) {
        if (id == null || id.isBlank() || id.isEmpty()) {
            throw new IllegalArgumentException(
                "Id is empty, cannot send notifications"
            );
        }
    }

    public void validateIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(
                "Ids are empty, cannot send notifications"
            );
        }
    }

    public void validateDate(LocalDateTime start, LocalDateTime end, Integer maxDaysBackSinceStart) {
        validateDate(start, end, null, maxDaysBackSinceStart);
    }

    public void validateDate(LocalDateTime start, LocalDateTime end, Integer maxDaysTimeInterval, Integer maxDaysBackSinceStart) {
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Time period is invalid, end is before start");
        }

        if (maxDaysTimeInterval != null && !isTimePeriodValid(start, end, maxDaysTimeInterval)) {
            throw new IllegalArgumentException(
                String.format("Time period is larger than allowed '%s' days, cannot send notifications", maxDaysTimeInterval)
            );
        }

        if (!isStartDateValid(start, maxDaysBackSinceStart)) {
            throw new IllegalArgumentException(
                String.format("Start date is larger than allowed '%s' days, cannot send notifications", maxDaysBackSinceStart)
            );
        }
    }

    private boolean isTimePeriodValid(LocalDateTime start, LocalDateTime end, int maxDaysBetween) {
        final var daysBetween = ChronoUnit.DAYS.between(start, end != null ? end : LocalDateTime.now());
        return daysBetween <= maxDaysBetween;
    }

    private boolean isStartDateValid(LocalDateTime start, int maxDaysBack) {
        return isTimePeriodValid(start, LocalDateTime.now(), maxDaysBack);
    }

}
