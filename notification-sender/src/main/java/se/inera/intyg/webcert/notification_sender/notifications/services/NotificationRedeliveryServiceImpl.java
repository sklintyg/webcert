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
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.NotificationResultEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.notification_sender.notifications.services.notificationredeliverystrategy.NotificationRedeliveryStrategyFactory.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationWSResultMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    // TODO Perhaps move redelivery service etc to web module
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;


    @Override
    public void handleNotificationSuccess(NotificationWSResultMessage resultMessage, Handelse event) {
        executeSuccess(resultMessage, event);
    }

    /*
    @Override
    public void handleNotificationResend(NotificationRedelivery notificationRedelivery) {
        executeResend(notificationRedelivery);
    }
     */

    @Override
    public void handleNotificationResend(NotificationWSResultMessage resultMessage, Handelse event) {
        executeResend(resultMessage, event);
    }

    @Override
    public void handleNotificationFailure(NotificationWSResultMessage resultMessage, Handelse event) {
        executeFailure(resultMessage, event);
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

    @Override
    public Handelse setNotificationFailure(Long eventId) {
        Handelse event = handelseRepo.findById(eventId).orElse(null);

        if (event != null) {
            event.setDeliveryStatus(NotificationResultEnum.FAILURE.name());
            handelseRepo.save(event);
        }

        List<NotificationRedelivery> redeliveries = notificationRedeliveryRepository.findByEventId(eventId);
        if (!redeliveries.isEmpty()) {
            deleteNotificationRedelivery(redeliveries.get(0));
        }
        return event;
    }

    @Transactional
    protected void executeResend(NotificationWSResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent = null;
        try {
            if (existingRedelivery == null) {
                monitorEvent = persistEvent(event);
                LOG.debug("Persisting notification eventId {} with delivery status {}", monitorEvent.getId(),
                    resultMessage.getDeliveryStatus());
                NotificationRedeliveryStrategy redeliveryStrategy = notificationRedeliveryStrategyFactory.getResendStrategy(STANDARD);
                NotificationRedelivery notificationRedelivery = createNotificationRedelivery(monitorEvent, redeliveryStrategy,
                    resultMessage);
                monitorLogResend(resultMessage, monitorEvent, notificationRedelivery);
            } else {
                Handelse updatedEvent = updateExistingEvent(existingRedelivery, resultMessage.getDeliveryStatus());
                LOG.debug("Updating persisted notification with eventId {} with delivery status {}", event.getId(),
                    resultMessage.getDeliveryStatus());
                updateNotificationRedelivery(existingRedelivery, getRedeliveryStrategy(existingRedelivery),
                    resultMessage, updatedEvent);
            }
        } catch (JsonProcessingException e) {
            LOG.warn("Failure creating redelivery storage message [certificateId: {}, eventType: {}, timestamp: {}]",
                resultMessage.getCertificateId(), event.getCode(), event.getTimestamp());
            Handelse failedEvent = setNotificationFailure(monitorEvent.getId());
            monitorLogFailure(resultMessage, failedEvent);
        }
    }

    private void executeSuccess(NotificationWSResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        updateEventFromResultSuccess(existingRedelivery, event, resultMessage);
    }

    private void executeFailure(NotificationWSResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        updateEventFromResultFailure(existingRedelivery, event, resultMessage);
    }

    private void updateEventFromResultSuccess(NotificationRedelivery existingRedelivery, Handelse event,
        NotificationWSResultMessage resultMessage) {
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
        monitorLogSuccess(resultMessage.getCorrelationId(), monitorEvent);
    }

    private void updateEventFromResultFailure(NotificationRedelivery existingRedelivery, Handelse event,
        NotificationWSResultMessage resultMessage) {
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
        monitorLogFailure(resultMessage, monitorEvent, existingRedelivery);
    }

    private void monitorLogSuccess(String correlationId, Handelse event) {
        monitoringLog.logStatusUpdateForCareStatusSuccess(event.getId(), event.getCode().name(), event.getIntygsId(),
            correlationId, event.getEnhetsId());
    }

    private void monitorLogFailure(NotificationWSResultMessage resultMessage, Handelse event) {
        monitorLogFailure(resultMessage, event, null);
    }

    private void monitorLogFailure(NotificationWSResultMessage resultMessage, Handelse event, NotificationRedelivery existingRedelivery) {
        // Have been sent at least once, and resending column starts at 0, i.e. 1+1
        int sendAttempt = existingRedelivery == null ? 1 : existingRedelivery.getAttemptedRedeliveries() + 2;
        ResultType resultType = resultMessage.getResultType();
        ErrorIdType errorId = null;
        String resultText = null;
        if (resultType != null) {
            errorId = resultType.getErrorId();
            resultText = resultType.getResultText();
        }
        monitoringLog.logStatusUpdateForCareStatusFailure(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(), errorId == null ? null : errorId.value(), resultText, sendAttempt);
    }

    private void monitorLogResend(NotificationWSResultMessage resultMessage, Handelse event, NotificationRedelivery existingRedelivery) {
        ResultType resultType = resultMessage.getResultType();
        ErrorIdType errorId = null;
        String resultText = null;
        if (resultType != null) {
            errorId = resultType.getErrorId();
            resultText = resultType.getResultText();
        }
        monitoringLog.logStatusUpdateForCareStatusResend(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(),
            resultType.getErrorId() == null ? null : resultType.getErrorId().value(),
            resultType == null ? null : resultType.getResultText(), existingRedelivery.getAttemptedRedeliveries() + 1,
            existingRedelivery.getRedeliveryTime());
    }

    private Handelse persistEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private Handelse updateExistingEvent(NotificationRedelivery existingRedelivery,
        NotificationResultEnum deliveryStatus) {
        Handelse event = handelseRepo.findById(existingRedelivery.getEventId()).orElseThrow();
        event.setDeliveryStatus(deliveryStatus.toString());
        return handelseRepo.save(event);
    }

    private NotificationRedelivery getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepository.findByCorrelationId(correlationId).orElse(null);
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, NotificationRedeliveryStrategy strategy,
        NotificationWSResultMessage resultMessage) throws JsonProcessingException {
        NotificationRedelivery newRedelivery =
            new NotificationRedelivery(resultMessage.getCorrelationId(), event.getId(),
                processMessageForStorage(resultMessage.getStatusUpdate()),
                strategy.getName().toString(),
                LocalDateTime.now().plus(strategy.getNextTimeValue(0), strategy.getNextTimeUnit(0)), 0);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", newRedelivery.getCorrelationId(), event.getId());
        return notificationRedeliveryRepository.save(newRedelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery existingRedelivery,
        NotificationRedeliveryStrategy strategy, NotificationWSResultMessage resultMessage, Handelse event) {
        final int attemptedRedeliveries = existingRedelivery.getAttemptedRedeliveries() + 1;
        final int maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedRedeliveries < maxRedeliveries) {
            LOG.debug("Updating redelivery notification for eventId {}", event.getId());
            existingRedelivery.setAttemptedRedeliveries(attemptedRedeliveries);
            existingRedelivery.setRedeliveryTime(existingRedelivery.getRedeliveryTime()
                .plus(strategy.getNextTimeValue(attemptedRedeliveries), strategy.getNextTimeUnit(attemptedRedeliveries)));
            notificationRedeliveryRepository.save(existingRedelivery);
            monitorLogResend(resultMessage, event, existingRedelivery);
        } else {
            LOG.warn("Setting redelivery failure for eventId {}", event.getId());
            updateExistingEvent(existingRedelivery, NotificationResultEnum.FAILURE);
            notificationRedeliveryRepository.delete(existingRedelivery);
            monitorLogFailure(resultMessage, event, existingRedelivery);
        }
    }

    private NotificationRedeliveryStrategy getRedeliveryStrategy(NotificationRedelivery redelivery) {
        return notificationRedeliveryStrategyFactory
            .getResendStrategy(NotificationRedeliveryStrategyEnum.valueOf(redelivery.getRedeliveryStrategy()));
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepository.delete(record);
    }

    private byte[] processMessageForStorage(CertificateStatusUpdateForCareType statusMessage) throws JsonProcessingException {
        NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage().set(statusMessage);
        return objectMapper.writeValueAsBytes(redeliveryMessage);
    }
}

