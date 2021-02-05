package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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
public class NotificationResultResendService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultResendService.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    public void process(NotificationResultMessage resultMessage) {
        executeResend(resultMessage, resultMessage.getEvent());
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
        } else {
            updateNotificationRedelivery(existingRedelivery, getRedeliveryStrategy(existingRedelivery.getRedeliveryStrategy()),
                resultMessage, event);
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
        NotificationRedelivery newRedelivery = new NotificationRedelivery(resultMessage.getCorrelationId(), event.getId(),
            resultMessage.getRedeliveryMessageBytes(), strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)), 1);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", newRedelivery.getCorrelationId(), event.getId());
        return notificationRedeliveryRepo.save(newRedelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery existingRedelivery,
        NotificationRedeliveryStrategy strategy, NotificationResultMessage resultMessage, Handelse event) {
        final int attemptedDeliveries = existingRedelivery.getAttemptedDeliveries() + 1;
        final int maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedDeliveries - 1 < maxRedeliveries) {
            Handelse updatedEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            LOG.debug("Updating persisted notification with eventId {} with delivery status {}", updatedEvent.getId(),
                updatedEvent.getDeliveryStatus());
            LOG.debug("Updating redelivery notification for eventId {}", updatedEvent.getId());
            existingRedelivery.setAttemptedDeliveries(attemptedDeliveries);
            existingRedelivery.setRedeliveryTime(existingRedelivery.getRedeliveryTime()
                .plus(strategy.getNextTimeValue(attemptedDeliveries), strategy.getNextTimeUnit(attemptedDeliveries)));
            // TODO: Only update if necessary.
            NotificationRedelivery updatedRedelivery = notificationRedeliveryRepo.save(existingRedelivery);
            monitorLog(updatedEvent, resultMessage, updatedRedelivery); // log resend
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

    private void monitorLog(@NonNull Handelse event, NotificationResultMessage resultMessage, NotificationRedelivery redelivery) {

        // TODO: Check if it should be logical address or unit id. Logical address is already in the result message: resultMessage.getLogicalAddress()
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
