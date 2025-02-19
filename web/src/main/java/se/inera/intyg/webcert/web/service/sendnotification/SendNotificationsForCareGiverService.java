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

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;

@Service
@RequiredArgsConstructor
public class SendNotificationsForCareGiverService {

    private final NotificationRedeliveryRepository notificationRedeliveryRepository;
    private final SendNotificationRequestValidator sendNotificationRequestValidator;
    private final SendNotificationCountValidator sendNotificationCountValidator;

    @Value("${timeinterval.maxdays.caregiver:1}")
    private int maxTimeInterval;

    @Value("${timelimit.daysback.start:365}")
    private int maxDaysBackStartDate;

    public SendNotificationResponseDTO send(String careGiverId,
        SendNotificationsForCareGiverRequestDTO request) {
        final var sanitizedId = SendNotificationRequestSanitizer.sanitize(careGiverId);

        sendNotificationRequestValidator.validateId(sanitizedId);
        sendNotificationRequestValidator.validateDate(request.getStart(), request.getEnd(),
            maxTimeInterval, maxDaysBackStartDate);

        sendNotificationCountValidator.careGiver(sanitizedId, request);
        final var response = notificationRedeliveryRepository.sendNotificationsForCareGiver(
            sanitizedId,
            request.getStatuses(),
            request.getStart(),
            request.getEnd(),
            request.getActivationTime()
        );

        return SendNotificationResponseDTO.builder()
            .count(response)
            .build();
    }

    public SendNotificationResponseDTO count(String careGiverId,
        CountNotificationsForCareGiverRequestDTO request) {
        final var sanitizedId = SendNotificationRequestSanitizer.sanitize(careGiverId);

        sendNotificationRequestValidator.validateId(sanitizedId);
        sendNotificationRequestValidator.validateDate(request.getStart(), request.getEnd(),
            maxTimeInterval, maxDaysBackStartDate);

        final var response = notificationRedeliveryRepository.countNotificationsForCareGiver(
            sanitizedId,
            request.getStatuses(),
            request.getStart(),
            request.getEnd()
        );

        return SendNotificationResponseDTO.builder()
            .count(response)
            .build();
    }
}
