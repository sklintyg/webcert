/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.CLIENT;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Service
public class NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryService.class);

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Autowired
    private FeaturesHelper featuresHelper;

    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    /**
     * Retrieves the notifications that are up for redelivery and returns them in the order they should be resent.
     *
     * @return list of {@link NotificationRedelivery} up for redelivery.
     */
    public List<NotificationRedelivery> getNotificationsForRedelivery() {
        final var notificationRedeliveryList = notificationRedeliveryRepo.findByRedeliveryTimeLessThan(LocalDateTime.now());

        notificationRedeliveryList.forEach(this::addCorrelationIdIfMissing);

        return notificationRedeliveryList.stream()
            .sorted(Comparator.comparing(NotificationRedelivery::getEventId))
            .collect(Collectors.toList());
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

        if (usingWebcertMesssaging()) {
            clearRedeliveryTime(notificationRedelivery);
        } else {
            setEventAsDeliveredByClient(event);
            deleteNotificationRedelivery(notificationRedelivery);
        }
    }

    @Transactional
    public void resend(NotificationRedelivery notificationRedelivery, byte[] message) {
        final var event = handelseRepo.findById(notificationRedelivery.getEventId()).orElseThrow();
        resend(notificationRedelivery, event, message);
    }

    private void clearRedeliveryTime(NotificationRedelivery notificationRedelivery) {
        notificationRedelivery.setRedeliveryTime(null);
        notificationRedeliveryRepo.save(notificationRedelivery);
    }

    private boolean usingWebcertMesssaging() {
        return featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);
    }

    private void addCorrelationIdIfMissing(NotificationRedelivery notificationRedelivery) {
        if (notificationRedelivery.getCorrelationId() == null) {
            notificationRedelivery.setCorrelationId(UUID.randomUUID().toString());
        }
    }

    private void setEventAsDeliveredByClient(Handelse event) {
        event.setDeliveryStatus(CLIENT);
        handelseRepo.save(event);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
    }
}
