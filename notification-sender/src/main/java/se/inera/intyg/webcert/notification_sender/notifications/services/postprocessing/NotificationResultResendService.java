package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
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
    private static final int ONE_ATTEMPTED_DELIVERY = 1;

    private final MonitoringLogService logService;
    private final HandelseRepository handelseRepo;
    private final NotificationRedeliveryRepository notificationRedeliveryRepo;
    private final NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    public NotificationResultResendService(
        MonitoringLogService logService, HandelseRepository handelseRepo, NotificationRedeliveryRepository notificationRedeliveryRepo,
        NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory) {
        this.logService = logService;
        this.handelseRepo = handelseRepo;
        this.notificationRedeliveryRepo = notificationRedeliveryRepo;
        this.notificationRedeliveryStrategyFactory = notificationRedeliveryStrategyFactory;
    }

    @Transactional
    public void process(@NonNull NotificationResultMessage resultMessage) {
        final var event = resultMessage.getEvent();
        final var existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());

        if (existingRedelivery.isEmpty()) {
            createEventAndNotificationRedelivery(event, resultMessage);

        } else {
            updateEventAndNotificationRedelivery(event, resultMessage, existingRedelivery.get());
        }
    }

    private void createEventAndNotificationRedelivery(Handelse event, NotificationResultMessage resultMessage) {
        final var createdEvent = createEvent(event);
        final var createdRedelivery = createNotificationRedelivery(createdEvent, resultMessage);
        monitorLogResend(createdEvent, resultMessage, createdRedelivery);
    }

    private void updateEventAndNotificationRedelivery(Handelse event, NotificationResultMessage resultMessage,
        NotificationRedelivery redelivery) {
        final var strategy = getRedeliveryStrategy(redelivery.getRedeliveryStrategy());
        final var attemptedDeliveries = attemptedRedeliveries(redelivery) + 1;
        final var maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedDeliveries <= maxRedeliveries) {
            event.setId(redelivery.getEventId());
            updateRedeliveryTimeAndSendAttempt(redelivery, strategy, attemptedDeliveries, resultMessage.getNotificationSentTime());
            monitorLogResend(event, resultMessage, redelivery);

        } else {
            final var updatedEvent = setDeliveryStatusFailure(redelivery.getEventId());
            deleteNotificationRedelivery(redelivery);
            redelivery.setAttemptedDeliveries(attemptedDeliveries);
            monitorLogFailure(updatedEvent, resultMessage, redelivery);
        }
    }

    private int attemptedRedeliveries(NotificationRedelivery redelivery) {
        return redelivery.getAttemptedDeliveries() != null ? redelivery.getAttemptedDeliveries() : 0;
    }

    private Optional<NotificationRedelivery> getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId);
    }

    private NotificationRedeliveryStrategy getRedeliveryStrategy(NotificationRedeliveryStrategyEnum strategy) {
        return notificationRedeliveryStrategyFactory.getResendStrategy(strategy);
    }

    private Handelse createEvent(Handelse event) {
        final var createdEvent = handelseRepo.save(event);
        LOG.debug("Creating Notification Event with id {} and delivery status {}", createdEvent.getId(), createdEvent.getDeliveryStatus());
        return createdEvent;
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, NotificationResultMessage resultMessage) {
        final var strategy = getRedeliveryStrategy(STANDARD);
        final var redelivery =  new NotificationRedelivery(
            resultMessage.getCorrelationId(),
            event.getId(),
            resultMessage.getRedeliveryMessageBytes(),
            strategy.getName(),
            getNextRedeliveryTime(strategy, resultMessage.getNotificationSentTime(), ONE_ATTEMPTED_DELIVERY),
            ONE_ATTEMPTED_DELIVERY
        );
        LOG.debug("Creating Notification Redelivery for event with id {} and correlation id {}.", event.getId(),
            redelivery.getCorrelationId());
        return notificationRedeliveryRepo.save(redelivery);
    }

    private void updateRedeliveryTimeAndSendAttempt(NotificationRedelivery redelivery, NotificationRedeliveryStrategy strategy,
        int attemptedDeliveries, LocalDateTime notificationSentTime) {
        final var nextRedeliveryTime = getNextRedeliveryTime(strategy, notificationSentTime, attemptedDeliveries);
        redelivery.setRedeliveryTime(nextRedeliveryTime);
        redelivery.setAttemptedDeliveries(attemptedDeliveries);
        notificationRedeliveryRepo.save(redelivery);
        LOG.debug("Updating Notification Redelivery for event with id {} and correlation id {}", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }

    private LocalDateTime getNextRedeliveryTime(NotificationRedeliveryStrategy strategy, LocalDateTime notificationSentTime,
        int attemptedDeliveries) {
        final var nextTimeValue = strategy.getNextTimeValue(attemptedDeliveries);
        final var nextTimeUnit = strategy.getNextTimeUnit(attemptedDeliveries);
        return notificationSentTime.plus(nextTimeValue, nextTimeUnit);
    }

    private Handelse setDeliveryStatusFailure(Long eventId) {
        final var event = handelseRepo.findById(eventId).orElseThrow();
        event.setDeliveryStatus(FAILURE);
        LOG.debug("Setting Delivery Status {} for event with id {}.", event.getDeliveryStatus(), event.getId());
        return handelseRepo.save(event);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery redelivery) {
        LOG.debug("Deleting Notification Redelivery for event with id {}.", redelivery.getEventId());
        notificationRedeliveryRepo.delete(redelivery);
    }

    private void monitorLogResend(Handelse event, NotificationResultMessage resultMessage, NotificationRedelivery redelivery) {
        final var resultType = resultMessage.getResultType();
        final var errorId = resultType.getNotificationErrorType() != null ? resultType.getNotificationErrorType().name() : null;
        logService.logStatusUpdateForCareStatusResend(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(), errorId, resultType.getNotificationResultText(), redelivery.getAttemptedDeliveries(),
            redelivery.getRedeliveryTime());
    }

    private void monitorLogFailure(Handelse event, NotificationResultMessage resultMessage, NotificationRedelivery redelivery) {
        final var resultType = resultMessage.getResultType();
        final var errorId = resultType.getNotificationErrorType() != null ? resultType.getNotificationErrorType().name() : null;
        logService.logStatusUpdateForCareStatusFailure(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(), errorId, resultType.getNotificationResultText(), redelivery.getAttemptedDeliveries());
    }
}
