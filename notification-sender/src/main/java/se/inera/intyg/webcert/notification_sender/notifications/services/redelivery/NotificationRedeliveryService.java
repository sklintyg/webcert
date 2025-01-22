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
package se.inera.intyg.webcert.notification_sender.notifications.services.redelivery;

import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Service
public class NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryService.class);

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Autowired
    private NotificationResultMessageCreator notificationResultMessageCreator;

    @Autowired
    private NotificationResultMessageSender notificationResultMessageSender;

    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    @Transactional
    public List<NotificationRedelivery> getNotificationsForRedelivery(int batchSize) {
        final var notificationRedeliveryList = getNotificationsForRedelivery(LocalDateTime.now(), batchSize);

        notificationRedeliveryList.forEach(this::addCorrelationIdIfMissing);

        clearRedeliveryTime(notificationRedeliveryList);

        return notificationRedeliveryList;
    }

    private List<NotificationRedelivery> getNotificationsForRedelivery(LocalDateTime timeLess, int batchSize) {
        if (batchSize < 1) {
            return Collections.emptyList();
        }
        return notificationRedeliveryRepo.findRedeliveryUpForDelivery(timeLess, batchSize);
    }

    @Transactional
    public void resend(NotificationRedelivery notificationRedelivery, Handelse event, byte[] message) {
        LOG.info("Initiating redelivery of status update for care [notificationId: {}, event: {}, logicalAddress: {}"
            + ", correlationId: {}]", event.getId(), event.getCode(), event.getEnhetsId(), notificationRedelivery.getCorrelationId());

        try {
            jmsTemplate.convertAndSend(message, jmsMessage -> {
                jmsMessage.setStringProperty(CORRELATION_ID, notificationRedelivery.getCorrelationId());
                jmsMessage.setStringProperty(INTYGS_ID, event.getIntygsId());
                jmsMessage.setStringProperty(LOGISK_ADRESS, event.getEnhetsId());
                jmsMessage.setStringProperty(USER_ID, event.getHanteratAv());
                jmsMessage.setLongProperty(JMS_TIMESTAMP, Instant.now().getEpochSecond());
                return jmsMessage;
            });
        } catch (JmsException e) {
            final var errorMessage = String.format("Failure resending message [notificationId: %s, event: %s, "
                    + "logicalAddress: %s, correlationId: %s]. Exception occurred setting JMs message headers.", event.getId(),
                event.getCode(), event.getEnhetsId(), notificationRedelivery.getCorrelationId());
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public void clearRedeliveryTime(List<NotificationRedelivery> notificationRedeliveryList) {
        if (notificationRedeliveryList.size() == 0) {
            return;
        }

        final var eventIds = notificationRedeliveryList.stream()
            .map(notificationRedelivery -> notificationRedelivery.getEventId())
            .collect(Collectors.toList());

        notificationRedeliveryRepo.clearRedeliveryTime(eventIds);
    }

    private void addCorrelationIdIfMissing(NotificationRedelivery notificationRedelivery) {
        if (notificationRedelivery.getCorrelationId() == null) {
            notificationRedelivery.setCorrelationId(UUID.randomUUID().toString());
        }
    }

    @Transactional
    public void handleErrors(NotificationRedelivery redelivery, Handelse event, Exception exception) {
        final var resultMessage = notificationResultMessageCreator.createFailureMessage(event, redelivery, exception);
        notificationResultMessageSender.sendResultMessage(resultMessage);
    }
}
