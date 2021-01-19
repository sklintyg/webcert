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

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.CLIENT;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.DISCARD;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private MonitoringLogService logService;
    @Autowired
    private HandelseRepository handelseRepo;
    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;
    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;


    @Transactional
    @Override
    public void handleNotificationSuccess(NotificationResultMessage resultMessage) {
        executeSuccessOrFailure(resultMessage, resultMessage.getEvent());
    }

    @Transactional
    @Override
    public void handleNotificationResend(NotificationResultMessage resultMessage) {
        executeResend(resultMessage, resultMessage.getEvent());
    }

    @Transactional
    @Override
    public void handleNotificationFailure(NotificationResultMessage resultMessage) {
        executeSuccessOrFailure(resultMessage, resultMessage.getEvent());
    }

    @Transactional
    @Override
    public List<NotificationRedelivery> getNotificationsForRedelivery() {
        List<NotificationRedelivery> redeliveryList = notificationRedeliveryRepo.findByRedeliveryTimeLessThan(LocalDateTime.now());
        redeliveryList.addAll(notificationRedeliveryRepo.findByCorrelationIdNull());
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getEventId));
        return redeliveryList;
    }

    @Transactional
    @Override
    public boolean isRedundantRedelivery(Handelse event) {
        return checkRedundantRedelivery(event);
    }

    @Transactional
    @Override
    public void discardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        doDiscardRedundantRedelivery(event, redelivery);
    }

    @Transactional
    @Override
    public Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    @Transactional
    @Override
    public void initiateManualNotification(NotificationRedelivery redelivery, Handelse event) {
        handelseRepo.save(event);
        notificationRedeliveryRepo.save(redelivery);
    }

    @Transactional
    @Override
    public void setSentWithV3Client(Handelse event, NotificationRedelivery redelivery) {
        event.setDeliveryStatus(CLIENT);
        handelseRepo.save(event);
        deleteNotificationRedelivery(redelivery);
    }


    private void executeSuccessOrFailure(NotificationResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent;
        if (existingRedelivery == null) {
            LOG.debug("Persisting notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            monitorEvent = persistEvent(event);
        } else if (resultMessage.getIsManualRedelivery()) { // Manually resent notification
            // TODO: Could this be Delivery strategy that is manual instead of an attribute that says it is manual?
            LOG.debug("Updating manually resent notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            monitorEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            deleteNotificationRedelivery(existingRedelivery);
        } else {
            LOG.debug("Updating persisted notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            monitorEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            deleteNotificationRedelivery(existingRedelivery);
        }
        // TODO: Consider overloading instead of sending null to parameters you don't have.
        monitorLog(monitorEvent, resultMessage, null); // log success or failure
    }

    private void executeResend(NotificationResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent;
        if (existingRedelivery == null) {
            monitorEvent = persistEvent(event);
            LOG.debug("Persisting notification eventId {} with delivery status {}", monitorEvent.getId(),
                event.getDeliveryStatus());
            NotificationRedeliveryStrategy redeliveryStrategy = getRedeliveryStrategy(STANDARD);
            NotificationRedelivery notificationRedelivery = createNotificationRedelivery(monitorEvent, redeliveryStrategy,
                resultMessage);
            monitorLog(monitorEvent, resultMessage, notificationRedelivery); // log resend
        } else if (resultMessage.getIsManualRedelivery()) { // Manually resent notification
            Handelse updatedEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            LOG.debug("Updating manually resent notification with eventId {} with delivery status {}", event.getId(),
                event.getDeliveryStatus());
            createManualNotificationRedelivery(existingRedelivery, resultMessage, updatedEvent);
        } else {
            Handelse updatedEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            LOG.debug("Updating persisted notification with eventId {} with delivery status {}", event.getId(),
                event.getDeliveryStatus());
            updateNotificationRedelivery(existingRedelivery, getRedeliveryStrategy(existingRedelivery.getRedeliveryStrategy()),
                resultMessage, updatedEvent);
        }
    }

    private Handelse persistEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private Handelse updateExistingEvent(NotificationRedelivery existingRedelivery, NotificationDeliveryStatusEnum deliveryStatus) {
        Handelse event = handelseRepo.findById(existingRedelivery.getEventId()).orElseThrow();
        event.setDeliveryStatus(deliveryStatus);
        return handelseRepo.save(event);
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, NotificationRedeliveryStrategy strategy,
        NotificationResultMessage resultMessage) {
        NotificationRedelivery newRedelivery =  new NotificationRedelivery(resultMessage.getCorrelationId(), event.getId(),
            resultMessage.getRedeliveryMessageBytes(), strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)), 1);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", newRedelivery.getCorrelationId(), event.getId());
        return notificationRedeliveryRepo.save(newRedelivery);
    }

    private void createManualNotificationRedelivery(NotificationRedelivery redelivery,
        NotificationResultMessage resultMessage, Handelse event) {
        NotificationRedeliveryStrategy strategy = getRedeliveryStrategy(redelivery.getRedeliveryStrategy());
        redelivery.setMessage(resultMessage.getRedeliveryMessageBytes());
        redelivery.setRedeliveryTime(LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)));
        redelivery.setAttemptedDeliveries(1);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", redelivery.getCorrelationId(), event.getId());
        notificationRedeliveryRepo.save(redelivery);
        monitorLog(event, resultMessage, redelivery); // log resend

    }

    private void updateNotificationRedelivery(NotificationRedelivery existingRedelivery,
        NotificationRedeliveryStrategy strategy, NotificationResultMessage resultMessage, Handelse event) {
        final int attemptedDeliveries = existingRedelivery.getAttemptedDeliveries() + 1;
        final int maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedDeliveries - 1 < maxRedeliveries) {
            LOG.debug("Updating redelivery notification for eventId {}", event.getId());
            existingRedelivery.setAttemptedDeliveries(attemptedDeliveries);
            existingRedelivery.setRedeliveryTime(existingRedelivery.getRedeliveryTime()
                .plus(strategy.getNextTimeValue(attemptedDeliveries), strategy.getNextTimeUnit(attemptedDeliveries)));
            NotificationRedelivery updatedRedelivery = notificationRedeliveryRepo.save(existingRedelivery);
            monitorLog(event, resultMessage, updatedRedelivery); // log resend
        } else {
            LOG.warn("Setting redelivery failure for eventId {}", event.getId());
            Handelse updatedEvent = updateExistingEvent(existingRedelivery, FAILURE);
            notificationRedeliveryRepo.delete(existingRedelivery);
            existingRedelivery.setAttemptedDeliveries(attemptedDeliveries);
            monitorLog(updatedEvent, resultMessage, existingRedelivery); // log failure
        }
    }

    private NotificationRedelivery getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId).orElse(null);
    }

    private NotificationRedeliveryStrategy getRedeliveryStrategy(NotificationRedeliveryStrategyEnum strategy) {
        return notificationRedeliveryStrategyFactory.getResendStrategy(strategy);
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

    private Handelse setNotificationFailure(Long eventId) {
        Handelse event = handelseRepo.findById(eventId).orElse(null);
        NotificationRedelivery redelivery = notificationRedeliveryRepo.findByEventId(eventId).orElse(null);
        if (event != null) {
            event.setDeliveryStatus(FAILURE);
            handelseRepo.save(event);
        }
        if (redelivery != null) {
            deleteNotificationRedelivery(redelivery);
        }
        return event;
        // TODO Monitorlog here if method keeping this method.
    }

    private void monitorLog(@NonNull Handelse event, NotificationResultMessage resultMessage, NotificationRedelivery redelivery) {

        String logicalAddress = event.getEnhetsId();
        Long eventId = event.getId();
        String eventType = event.getCode() != null ? event.getCode().name() : null;
        String certificateId = event.getIntygsId();
        NotificationDeliveryStatusEnum deliveryStatus = event.getDeliveryStatus();

        String correlationId = null;
        int currentSendAttempt = 1;
        LocalDateTime redeliveryTime = null;
        String errorId = null;
        String resultText = null;

        if (resultMessage != null) {
            correlationId = resultMessage.getCorrelationId();
            NotificationResultType resultType = resultMessage.getResultType();
            if (resultType != null) {
                errorId = resultType.getNotificationErrorType() != null ? resultType.getNotificationErrorType().value() : null;
                resultText = resultType.getNotificationResultText();
            }
        }
        if (redelivery != null) {
            currentSendAttempt = redelivery.getAttemptedDeliveries();
            redeliveryTime = redelivery.getRedeliveryTime();
        }

        switch (deliveryStatus) {
            case SUCCESS:
                logService.logStatusUpdateForCareStatusSuccess(eventId, eventType, certificateId, correlationId, logicalAddress);
                break;
            case RESEND:
                logService.logStatusUpdateForCareStatusResend(eventId, eventType, logicalAddress, certificateId, correlationId,
                    errorId, resultText, currentSendAttempt, redeliveryTime);
                break;
            case FAILURE:
                logService.logStatusUpdateForCareStatusFailure(eventId, eventType, logicalAddress, certificateId, correlationId,
                    errorId, resultText, currentSendAttempt);
        }
    }
}
