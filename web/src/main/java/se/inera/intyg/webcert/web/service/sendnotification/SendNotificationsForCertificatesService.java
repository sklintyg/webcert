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
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;

@Service
@RequiredArgsConstructor
public class SendNotificationsForCertificatesService {

    private final NotificationRedeliveryRepository notificationRedeliveryRepository;
    private final SendNotificationRequestValidator sendNotificationRequestValidator;
    private final SendNotificationCountValidator sendNotificationCountValidator;

    @Value("${timelimit.daysback.start:365}")
    private int maxDaysBackStartDate;

    public SendNotificationResponseDTO send(SendNotificationsForCertificatesRequestDTO request) {
        sendNotificationRequestValidator.validateIds(request.getCertificateIds());
        sendNotificationRequestValidator.validateDate(request.getStart(), request.getEnd(), maxDaysBackStartDate);

        sendNotificationCountValidator.certiticates(request);
        final var response = notificationRedeliveryRepository.sendNotificationsForCertificates(
            request.getCertificateIds(),
            request.getStatuses(),
            request.getStart(),
            request.getEnd(),
            request.getActivationTime()
        );

        return SendNotificationResponseDTO.builder()
            .count(response)
            .build();
    }
}