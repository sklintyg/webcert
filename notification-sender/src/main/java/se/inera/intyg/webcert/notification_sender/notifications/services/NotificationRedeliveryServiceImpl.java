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

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.DISCARD;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationWSResultMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    // TODO Perhaps move redelivery service etc to web module
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    private final MonitoringLogService logService;
    private final ObjectMapper objectMapper;
    private final HandelseRepository handelseRepo;
    private final NotificationRedeliveryRepository notificationRedeliveryRepo;
    private final NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    public NotificationRedeliveryServiceImpl(
        MonitoringLogService logService, ObjectMapper objectMapper, HandelseRepository handelseRepo,
        NotificationRedeliveryRepository notificationRedeliveryRepo,
        NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory) {
        this.logService = logService;
        this.objectMapper = objectMapper;
        this.handelseRepo = handelseRepo;
        this.notificationRedeliveryRepo = notificationRedeliveryRepo;
        this.notificationRedeliveryStrategyFactory = notificationRedeliveryStrategyFactory;
    }


    @Override
    public void handleNotificationSuccess(NotificationWSResultMessage resultMessage, Handelse event) {
        executeSuccessOrFailure(resultMessage, event);
    }

    @Override
    public void handleNotificationResend(NotificationWSResultMessage resultMessage, Handelse event) {
        executeResend(resultMessage, event);
    }

    @Override
    public void handleNotificationFailure(NotificationWSResultMessage resultMessage, Handelse event) {
        executeSuccessOrFailure(resultMessage, event);
    }

    @Override
    public List<NotificationRedelivery> getNotificationsForRedelivery() {
        List<NotificationRedelivery> redeliveryList = notificationRedeliveryRepo.findByRedeliveryTimeLessThan(LocalDateTime.now());
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getEventId));
        return redeliveryList;
    }

    @Override
    public boolean isRedundantRedelivery(Handelse event) {
        return checkRedundantRedelivery(event);
    }

    @Override
    public void discardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        doDiscardRedundantRedelivery(event, redelivery);
    }

    @Override
    public Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    @Override
    public void handleManualNotificationResend(Long eventId) {
        executeManualNotificationResend(eventId);
    }

    private void executeManualNotificationResend(Long eventId) {
        Handelse event = getEventById(eventId);
        NotificationRedeliveryStrategy strategy = getRedeliveryStrategy(STANDARD);
        String correlationId = UUID.randomUUID().toString();

        NotificationRedelivery redelivery = new NotificationRedelivery(correlationId, eventId, null, strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)), 1);
    }


    private void executeSuccessOrFailure(NotificationWSResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent;
        if (existingRedelivery == null) {
            LOG.debug("Persisting notification event {} with delivery status {}", event.getCode().value(),
                resultMessage.getDeliveryStatus());
            monitorEvent = persistEvent(event);
        } else {
            LOG.debug("Updating persisted notification event {} with delivery status {}", event.getCode().value(),
                resultMessage.getDeliveryStatus());
            monitorEvent = updateExistingEvent(existingRedelivery, resultMessage.getDeliveryStatus());
            deleteNotificationRedelivery(existingRedelivery);
        }
        monitorLog(monitorEvent, resultMessage, existingRedelivery); // log success or failure
    }

    private void executeResend(NotificationWSResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent = null;
        try {
            if (existingRedelivery == null) {
                monitorEvent = persistEvent(event);
                LOG.debug("Persisting notification eventId {} with delivery status {}", monitorEvent.getId(),
                    resultMessage.getDeliveryStatus());
                NotificationRedeliveryStrategy redeliveryStrategy = getRedeliveryStrategy(STANDARD);
                NotificationRedelivery notificationRedelivery = createNotificationRedelivery(monitorEvent, redeliveryStrategy,
                    resultMessage);
                monitorLog(monitorEvent, resultMessage, notificationRedelivery); // log resend
            } else {
                Handelse updatedEvent = updateExistingEvent(existingRedelivery, resultMessage.getDeliveryStatus());
                LOG.debug("Updating persisted notification with eventId {} with delivery status {}", event.getId(),
                    resultMessage.getDeliveryStatus());
                updateNotificationRedelivery(existingRedelivery, getRedeliveryStrategy(existingRedelivery.getRedeliveryStrategy()),
                    resultMessage, updatedEvent);
            }
        } catch (JsonProcessingException e) {
            LOG.warn("Failure creating redelivery storage message [certificateId: {}, eventType: {}, timestamp: {}]",
                resultMessage.getCertificateId(), event.getCode(), event.getTimestamp());
            Handelse failedEvent = setNotificationFailure(monitorEvent.getId());
            monitorLog(failedEvent, resultMessage, null); // log failure
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
        NotificationWSResultMessage resultMessage) throws JsonProcessingException {
        NotificationRedelivery newRedelivery =  new NotificationRedelivery(resultMessage.getCorrelationId(), event.getId(),
            processMessageForStorage(resultMessage.getStatusUpdate()), strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)), 1);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", newRedelivery.getCorrelationId(), event.getId());
        return notificationRedeliveryRepo.save(newRedelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery existingRedelivery,
        NotificationRedeliveryStrategy strategy, NotificationWSResultMessage resultMessage, Handelse event) {
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
        if (event.getCode() == HandelsekodEnum.ANDRAT) {
            String certificateId = event.getIntygsId();
            List<Handelse> events = handelseRepo.findByIntygsId(certificateId);
            numberOfSignedEvents = events.stream()
                .filter(e -> e.getCode() == HandelsekodEnum.SIGNAT && e.getDeliveryStatus() == SUCCESS).count();
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
        // TODO investigate new status aborted, monitorlog here.
    }

    private Handelse setNotificationFailure(Long eventId) {
        Handelse event = handelseRepo.findById(eventId).orElse(null);
        List<NotificationRedelivery> redeliveries = notificationRedeliveryRepo.findByEventId(eventId);
        if (event != null) {
            event.setDeliveryStatus(FAILURE);
            handelseRepo.save(event);
        }
        if (!redeliveries.isEmpty()) {
            deleteNotificationRedelivery(redeliveries.get(0));
        }
        return event;
        // TODO Monitorlog here if method keeping this method.
    }

    private byte[] processMessageForStorage(CertificateStatusUpdateForCareType statusMessage) throws JsonProcessingException {
        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage().set(statusMessage);
        return objectMapper.writeValueAsBytes(redeliveryMessage);
    }

    private void monitorLog(@NonNull Handelse event, NotificationWSResultMessage resultMessage, NotificationRedelivery redelivery) {

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
            ResultType resultType = resultMessage.getResultType();
            if (resultType != null) {
                errorId = resultType.getErrorId() != null ? resultType.getErrorId().value() : null;
                resultText = resultType.getResultText();
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

