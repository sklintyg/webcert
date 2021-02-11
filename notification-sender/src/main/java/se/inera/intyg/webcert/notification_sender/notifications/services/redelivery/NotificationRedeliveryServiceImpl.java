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

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.CLIENT;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.DISCARD;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Autowired
    private FeaturesHelper featuresHelper;

    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    @Override
    public List<NotificationRedelivery> getNotificationsForRedelivery() {
        // TODO: Get all notifications up for redelivery. Remove redundant redeliveries that should be discarded.
        // TODO: Set correlation-id if missing.

        // Create a correlation id for the redelivery.
        // TODO: This can be moved to redelivery service.
//        redelivery.setCorrelationId(UUID.randomUUID().toString());

        // Set that the event will be resent
//        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);

        // This saves changes to the event and redelivery
        // Shall this be done after it has been sent?
//        notificationRedeliveryService.initiateManualNotification(redelivery, event);

        return notificationRedeliveryRepo.findByRedeliveryTimeLessThan(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void resend(NotificationRedelivery notificationRedelivery, Handelse event, byte[] message) {
        // Lägg meddelande på kö.
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
            throw new RuntimeException(String.format("Failure resending message [notificationId: %s, event: %s, "
                    + "logicalAddress: %s, correlationId: %s]. Exception occurred setting JMs message headers.", event.getId(),
                event.getCode(), event.getEnhetsId(), notificationRedelivery.getCorrelationId()), e);
        }

        // Nollställ omsändningstiden.

        // If the feature is not active, then make sure that the redelivery is removed.
        if (!featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING)) {
            // TODO: Change name of method.
            setSentWithV3Client(event, notificationRedelivery);
        }

    }

    private boolean isRedundantRedelivery(Handelse event) {
        return checkRedundantRedelivery(event);
    }

    private void discardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        doDiscardRedundantRedelivery(event, redelivery);
    }


    private void setSentWithV3Client(Handelse event, NotificationRedelivery redelivery) {
        // TODO: Shall this have a different status? Why?
        event.setDeliveryStatus(CLIENT);
        handelseRepo.save(event);
        deleteNotificationRedelivery(redelivery);
    }


    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
    }

    private boolean checkRedundantRedelivery(Handelse event) {
        long numberOfSignedEvents = 0;
        HandelsekodEnum code = event.getCode();
        if (code == ANDRAT || code == SKAPAT) {
            String certificateId = event.getIntygsId();
            List<Handelse> events = handelseRepo.findByIntygsId(certificateId);
            numberOfSignedEvents = events.stream()
                .filter(e -> (code == ANDRAT && e.getCode() == SIGNAT && e.getDeliveryStatus() == SUCCESS)
                    ||  (e.getCode() == HandelsekodEnum.RADERA)).count();
        }
        return numberOfSignedEvents > 0;
    }

    private void doDiscardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        deleteNotificationRedelivery(redelivery);
        event.setDeliveryStatus(DISCARD);
        handelseRepo.save(event);
        LOG.info("Aborting redelivery attempts of redundant notification [eventId: {}, correlationId: {}, eventCode: {}, "
                + "certificateId: {}, logicalAddress: {}]. The event has been removed.", event.getId(), redelivery.getCorrelationId(),
            event.getCode(), event.getIntygsId(), event.getEnhetsId());
    }
}
