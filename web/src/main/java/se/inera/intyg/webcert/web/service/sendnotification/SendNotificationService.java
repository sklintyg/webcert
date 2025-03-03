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
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class SendNotificationService {

    private final NotificationRedeliveryRepository notificationRedeliveryRepository;
    private final SendNotificationRequestValidator sendNotificationRequestValidator;
    private final SendNotificationCountValidator sendNotificationCountValidator;
    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationService.class);

    @Transactional
    public SendNotificationResponseDTO send(String notificationId) {
        LOG.info("Attempting to resend status updates. Using parameters: notificationId '{}'", notificationId);

        final var sanitizedNotificationId = SendNotificationRequestSanitizer.sanitize(notificationId);

        sendNotificationRequestValidator.validateId(sanitizedNotificationId);
        sendNotificationCountValidator.notification(sanitizedNotificationId);

        final var response = notificationRedeliveryRepository.sendNotification(sanitizedNotificationId);

        LOG.info("Successfully resent status updates. Number of updates: '{}'. Using parameters: notificationId '{}'", response,
            notificationId);

        return SendNotificationResponseDTO.builder()
            .count(response)
            .build();
    }
}
