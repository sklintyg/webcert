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

import static se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory.NotificationRedeliveryStrategyEnum.STANDARD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.NotificationResultEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    // TODO Perhaps move redelivery service etc to web module
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;


    @Override
    public void handleNotificationSuccess(String correlationId, Handelse event, NotificationResultEnum deliveryStatus) {
        executeSuccessAndFailure(correlationId, event, deliveryStatus);
    }

    @Override
    public void handleNotificationResend(NotificationRedelivery notificationRedelivery) {
        executeResend(notificationRedelivery);
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

    @Override
    public List<NotificationRedelivery> getRedeliveriesForResend() {
        List<NotificationRedelivery> redeliveryList = notificationRedeliveryRepository.findByRedeliveryTimeLessThan(LocalDateTime.now());
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getEventId));
        return redeliveryList;
    }

    @Override
    public Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    @Transactional
    protected void executeSuccessAndFailure(String correlationId, Handelse event, NotificationResultEnum deliveryStatus) {

        NotificationRedelivery existingRedelivery = getExistingRedelivery(correlationId);

        if (existingRedelivery == null) {
            persistEvent(event);
        } else {
            updateExistingEvent(existingRedelivery, deliveryStatus);
            deleteNotificationRedelivery(existingRedelivery);
        }
    }

    @Transactional
    protected void executeResend(NotificationRedelivery redelivery) {
        NotificationRedeliveryStrategy strategy = getRedeliveryStrategy(redelivery);
        updateNotificationRedelivery(redelivery, strategy);
    }

    @Transactional
    protected void executeResend(String correlationId, Handelse event, CertificateStatusUpdateForCareType statusUpdate,
        NotificationResultEnum deliveryStatus) {

        NotificationRedelivery existingRedelivery = getExistingRedelivery(correlationId);

        if (existingRedelivery == null) {
            Handelse persistedEvent = persistEvent(event);
            NotificationRedeliveryStrategy redeliveryStrategy = notificationRedeliveryStrategyFactory.getResendStrategy(STANDARD);
            createNotificationRedelivery(persistedEvent, redeliveryStrategy, correlationId, statusUpdate);
        } else {
            updateExistingEvent(existingRedelivery, deliveryStatus);
            updateNotificationRedelivery(existingRedelivery, getRedeliveryStrategy(existingRedelivery));
        }
    }

    private Handelse persistEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private void updateExistingEvent(NotificationRedelivery existingRedelivery, NotificationResultEnum deliveryStatus) {
        Handelse existingEvent = handelseRepo.findById(existingRedelivery.getEventId()).orElse(null);
        if (existingEvent != null) {
            existingEvent.setDeliveryStatus(deliveryStatus.toString());
            handelseRepo.save(existingEvent);
        }
    }

    private NotificationRedelivery getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepository.findByCorrelationId(correlationId).orElse(null);
    }

    private void createNotificationRedelivery(Handelse persistedEvent, NotificationRedeliveryStrategy strategy, String correlationId,
        CertificateStatusUpdateForCareType message) {
        NotificationRedelivery newRedelivery =
            new NotificationRedelivery(correlationId, persistedEvent.getId(), prepMessageForStorage(message), strategy.getName().toString(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(0), strategy.getNextTimeUnit(0)), 0);
        notificationRedeliveryRepository.save(newRedelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery existingRedelivery, NotificationRedeliveryStrategy strategy) {
        final int attemptedRedeliveries = existingRedelivery.getAttemptedRedeliveries() + 1;
        final int maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedRedeliveries < maxRedeliveries) {
            existingRedelivery.setAttemptedRedeliveries(attemptedRedeliveries);
            existingRedelivery.setRedeliveryTime(existingRedelivery.getRedeliveryTime()
                .plus(strategy.getNextTimeValue(attemptedRedeliveries), strategy.getNextTimeUnit(attemptedRedeliveries)));
            notificationRedeliveryRepository.save(existingRedelivery);
        } else {
            updateExistingEvent(existingRedelivery, NotificationResultEnum.FAILURE);
            notificationRedeliveryRepository.delete(existingRedelivery);
        }
    }

    private NotificationRedeliveryStrategy getRedeliveryStrategy(NotificationRedelivery redelivery) {
        return notificationRedeliveryStrategyFactory
            .getResendStrategy(NotificationRedeliveryStrategyEnum.valueOf(redelivery.getRedeliveryStrategy()));
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepository.delete(record);
    }

    private byte[] prepMessageForStorage(CertificateStatusUpdateForCareType statusMessage) {

        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage().set(statusMessage);
        byte[] redeliveryMessageBytes = null;
        try {
            redeliveryMessageBytes = objectMapper.writeValueAsBytes(redeliveryMessage);
        } catch (JsonProcessingException e) {
            LOG.error("Failure creating notification redelivery storage type [certificatId: {}, event: {}, timestamp: {}]",
                statusMessage.getIntyg().getIntygsId(), statusMessage.getHandelse().getHandelsekod().getCode(),
                statusMessage.getHandelse().getTidpunkt());
        }
        return redeliveryMessageBytes;
    }
}
