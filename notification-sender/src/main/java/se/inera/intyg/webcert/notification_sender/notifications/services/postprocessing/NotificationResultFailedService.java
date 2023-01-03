/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Service
public class NotificationResultFailedService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultFailedService.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Transactional
    public void process(@NonNull NotificationResultMessage resultMessage) {
        var event = resultMessage.getEvent();
        final var existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());

        if (existingRedelivery.isEmpty()) {
            event = createEvent(event);
        } else {
            event = updateEvent(existingRedelivery.get().getEventId());
            deleteNotificationRedelivery(existingRedelivery.get());
        }

        monitorLogFailure(event, resultMessage, existingRedelivery);
    }

    private Optional<NotificationRedelivery> getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId);
    }

    private Handelse createEvent(Handelse event) {
        final var createdEvent = handelseRepo.save(event);
        LOG.debug("Creating notification event {} with delivery status {}", createdEvent.getId(), createdEvent.getDeliveryStatus());
        return createdEvent;
    }

    private Handelse updateEvent(Long eventId) {
        final var event = handelseRepo.findById(eventId).orElseThrow();
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.FAILURE);
        LOG.debug("Setting Delivery Status {} for event with id {}.", event.getDeliveryStatus(), event.getId());
        return handelseRepo.save(event);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery redelivery) {
        LOG.debug("Deleting Notification Redelivery for event with id {}.", redelivery.getEventId());
        notificationRedeliveryRepo.delete(redelivery);
    }

    private void monitorLogFailure(Handelse event, NotificationResultMessage resultMessage, Optional<NotificationRedelivery> redelivery) {
        final var resultType = resultMessage.getResultType();
        final var errorId = resultType.getNotificationErrorType() != null ? resultType.getNotificationErrorType().name() : null;
        final var currentSendAttempt = redelivery.map(notificationRedelivery -> attemptedRedeliveries(notificationRedelivery) + 1)
            .orElse(1);

        logService.logStatusUpdateForCareStatusFailure(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(), errorId, resultType.getNotificationResultText(), currentSendAttempt);
    }

    private int attemptedRedeliveries(NotificationRedelivery redelivery) {
        return redelivery.getAttemptedDeliveries() != null ? redelivery.getAttemptedDeliveries() : 0;
    }
}
