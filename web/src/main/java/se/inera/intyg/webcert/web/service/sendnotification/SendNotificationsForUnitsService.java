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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForUnitsRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Service
@RequiredArgsConstructor
public class SendNotificationsForUnitsService {

    private final NotificationRedeliveryRepository notificationRedeliveryRepository;
    private final HandelseRepository handelseRepository;
    private final SendNotificationRequestValidator sendNotificationRequestValidator;
    private final SendNotificationCountValidator sendNotificationCountValidator;
    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationsForUnitsService.class);

    @Value("${timeinterval.maxdays.unit:7}")
    private int maxTimeInterval;

    @Value("${timelimit.daysback.start:365}")
    private int maxDaysBackStartDate;

    @Value("${max.allowed.notification.send}")
    private int maxAllowedNotificationSend;

    @Transactional
    public SendNotificationResponseDTO send(SendNotificationsForUnitsRequestDTO request) {
        LOG.info(
            "Attempting to resend status updates. Using parameters: unitId '{}', statuses '{}', start '{}', end '{}' activationTime '{}' ",
            request.getUnitIds(), request.getStatuses(), request.getStart(), request.getEnd(), request.getActivationTime()
        );

        final var sanitizedRequest = SendNotificationRequestSanitizer.sanitize(request);
        final var stringStatuses = SendNotificationRequestSanitizer.getStatusesAsString(sanitizedRequest.getStatuses());

        sendNotificationRequestValidator.validateIds(sanitizedRequest.getUnitIds());
        sendNotificationRequestValidator.validateDate(sanitizedRequest.getStart(), sanitizedRequest.getEnd(), maxTimeInterval,
            maxDaysBackStartDate);

        sendNotificationCountValidator.units(sanitizedRequest);
        final var response = notificationRedeliveryRepository.sendNotificationsForUnits(
            sanitizedRequest.getUnitIds(),
            stringStatuses,
            sanitizedRequest.getStart(),
            sanitizedRequest.getEnd(),
            sanitizedRequest.getActivationTime()
        );

        LOG.info(
            "Successfully resent status updates. Number of updates: '{}'. Using parameters: unitId '{}', statuses '{}', start '{}', end '{}' activationTime '{}' ",
            response, request.getUnitIds(), request.getStatuses(), request.getStart(), request.getEnd(), request.getActivationTime()
        );

        return SendNotificationResponseDTO.builder()
            .count(response)
            .build();
    }

    public CountNotificationResponseDTO count(CountNotificationsForUnitsRequestDTO request) {
        LOG.info(
            "Attempting to count Unit status updates. Using parameters: unitIds '{}', statuses '{}'",
            request.getUnitIds(), request.getStatuses()
        );


        final var sanitizedIds = SendNotificationRequestSanitizer.sanitize(request.getUnitIds());
        final var sanitizedRequest = SendNotificationRequestSanitizer.sanitize(request);
        final var stringStatuses = SendNotificationRequestSanitizer.getStatusesAsString(sanitizedRequest.getStatuses());


        sendNotificationRequestValidator.validateIds(sanitizedIds);
        sendNotificationRequestValidator.validateDate(sanitizedRequest.getStart(), sanitizedRequest.getEnd(), maxTimeInterval,
            maxDaysBackStartDate);

        final var response = handelseRepository.countNotificationsForUnits(
            sanitizedIds,
            stringStatuses,
            sanitizedRequest.getStart(),
            sanitizedRequest.getEnd()
        );

        LOG.info(
            "Successfully counted Unit status updates. Number of updates: '{}'. Using parameters: unitIds '{}', statuses '{}'",
            response, request.getUnitIds(), request.getStatuses()
        );

        return CountNotificationResponseDTO.builder()
            .count(response)
            .max(maxAllowedNotificationSend)
            .build();
    }
}
