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

public class SendNotificationsForCertificatesRequestDTO {

    private List<String> certificateIds;
    private List<NotificationDeliveryStatusEnum> statuses;
    private LocalDateTime activationTime;
    private LocalDateTime start;
    private LocalDateTime to;

    public static SendNotificationsForCertificatesRequestDTO create(List<String> certificateIds,
        List<NotificationDeliveryStatusEnum> statuses, LocalDateTime activationTime, LocalDateTime start, LocalDateTime to) {
        final var request = new SendNotificationsForCertificatesRequestDTO();
        request.activationTime = activationTime;
        request.certificateIds = certificateIds;
        request.statuses = statuses;
        request.start = start;
        request.to = to;

        return request;
    }

    public List<String> getCertificateIds() {
        return certificateIds;
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

    public LocalDateTime getTo() {
        return to;
    }
}
