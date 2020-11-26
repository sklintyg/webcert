/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import java.time.LocalDateTime;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPostProcessor.NotificationResultEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    private static final String LOCAL_PART = "CertificateStatusUpdateForCareType";
    private static final String NAMESPACE_URL = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";


    @Override
    public void handleNotificationSuccess(String correlationId, Handelse event, NotificationResultEnum deliveryStatus) {
        executeSuccessAndFailure(correlationId, event, deliveryStatus);
    }

    @Override
    public void handleNotificationResend(String correlationId, Handelse event, NotificationResultEnum deliveryStatus,
        CertificateStatusUpdateForCareType statusUpdate) {
        executeResend(correlationId, event, statusUpdate, deliveryStatus);
    }

    @Override
    public void handleNotificationFailure(String correlationId, Handelse event, NotificationResultEnum deliveryStatus) {
        executeSuccessAndFailure(correlationId, event, deliveryStatus);
    }


    private void executeResend(String correlationId, Handelse event, CertificateStatusUpdateForCareType statusUpdate,
        NotificationResultEnum deliveryStatus) {

        NotificationRedeliveryStrategy redeliveryStrategy =
            notificationRedeliveryStrategyFactory.getResendStrategy(NotificationRedeliveryStrategyEnum.STANDARD);
        NotificationRedelivery existingRedelivery = getExistingRedelivery(correlationId);

        if (existingRedelivery == null) {
            Handelse persistedEvent = persistEvent(event);
            createNotificationRedelivery(persistedEvent, redeliveryStrategy, correlationId, statusUpdate);
        } else {
            updateCurrentEvent(existingRedelivery, deliveryStatus);
            updateNotificationRedelivery(existingRedelivery, redeliveryStrategy);
        }

    }

    private void executeSuccessAndFailure(String correlationId, Handelse event, NotificationResultEnum deliveryStatus) {

        NotificationRedelivery existingRedelivery = getExistingRedelivery(correlationId);

        if (existingRedelivery == null) {
            persistEvent(event);
        } else {
            updateCurrentEvent(existingRedelivery, deliveryStatus);
            deleteNotificationRedelivery(existingRedelivery);
        }
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepository.delete(record);
    }

    private NotificationRedelivery getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepository.findByCorrelationId(correlationId).orElse(null);
    }

    private Handelse persistEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private void updateCurrentEvent(NotificationRedelivery currentRedelivery, NotificationResultEnum deliveryStatus) {
        Handelse currentEvent = handelseRepo.findById(currentRedelivery.getEventId()).orElse(null);
        if (currentEvent != null) {
            currentEvent.setDeliveryStatus(deliveryStatus.toString());
            handelseRepo.save(currentEvent);
        }
    }

    private void createNotificationRedelivery(Handelse persistedEvent, NotificationRedeliveryStrategy strategy, String correlationId,
        CertificateStatusUpdateForCareType message) {
        NotificationRedelivery newRedelivery =
            new NotificationRedelivery(correlationId, persistedEvent.getId(), marshalStatusMessage(message), strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(0), strategy.getNextTimeUnit(0)), 0);
        notificationRedeliveryRepository.save(newRedelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery currentRedelivery, NotificationRedeliveryStrategy strategy) {
        final int attemptedRedeliveries = currentRedelivery.getAttemptedRedeliveries() + 1;
        final int maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedRedeliveries < maxRedeliveries) {
            currentRedelivery.setAttemptedRedeliveries(attemptedRedeliveries);
            currentRedelivery.setRedeliveryTime(LocalDateTime.now().plus(strategy.getNextTimeValue(attemptedRedeliveries),
                strategy.getNextTimeUnit(attemptedRedeliveries)));
            notificationRedeliveryRepository.save(currentRedelivery);
        } else {
            updateCurrentEvent(currentRedelivery, NotificationResultEnum.FAILURE);
            notificationRedeliveryRepository.delete(currentRedelivery);
        }
    }

    private String marshalStatusMessage(CertificateStatusUpdateForCareType statusMessage) {
        QName qName = new QName(NAMESPACE_URL, LOCAL_PART);
        return XmlMarshallerHelper.marshal(new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, statusMessage));
    }
}

