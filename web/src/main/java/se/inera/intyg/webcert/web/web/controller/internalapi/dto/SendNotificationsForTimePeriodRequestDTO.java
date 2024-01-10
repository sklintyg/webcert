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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

public class SendNotificationsForTimePeriodRequestDTO {

    private List<NotificationDeliveryStatusEnum> statuses;
    private LocalDateTime activationTime;
    private LocalDateTime start;
    private LocalDateTime end;

    public static SendNotificationsForTimePeriodRequestDTO create(List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime activationTime, LocalDateTime start, LocalDateTime end) {
        final var request = new SendNotificationsForTimePeriodRequestDTO();
        request.activationTime = activationTime;
        request.statuses = statuses;
        request.start = start;
        request.end = end;

        return request;
    }

    public List<NotificationDeliveryStatusEnum> getStatuses() {
        return statuses;
    }

    public LocalDateTime getActivationTime() {
        return activationTime;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
