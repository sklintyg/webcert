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

public class SendNotificationsForUnitsRequestDTO {

    private List<String> unitIds;
    private List<NotificationDeliveryStatusEnum> statuses;
    private LocalDateTime activationTime;

    public static SendNotificationsForUnitsRequestDTO create(List<String> certificateIds, List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime activationTime) {
        final var request = new SendNotificationsForUnitsRequestDTO();
        request.activationTime = activationTime;
        request.unitIds = certificateIds;
        request.statuses = statuses;

        return request;
    }

    public List<String> getUnitIds() {
        return unitIds;
    }

    public List<NotificationDeliveryStatusEnum> getStatuses() {
        return statuses;
    }

    public LocalDateTime getActivationTime() {
        return activationTime;
    }
}
