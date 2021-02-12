package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum.STANDARD;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Autowired
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    @Transactional
    public void process(@NonNull NotificationResultMessage resultMessage) {
        var event = resultMessage.getEvent();
        final var existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        if (existingRedelivery.isEmpty()) {
            event = createEventRecord(event);
            LOG.debug("Persisting notification eventId {} with delivery status {}", event.getId(), event.getDeliveryStatus());
            final var notificationRedelivery = createNotificationRedelivery(event, resultMessage);
            monitorLogResend(event, resultMessage, notificationRedelivery);
        } else {
            updateNotificationRedelivery(existingRedelivery.get(), resultMessage, event);
        }
    }

    private Optional<NotificationRedelivery> getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId);
    }

    private Handelse createEventRecord(Handelse event) {
        return handelseRepo.save(event);
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, NotificationResultMessage resultMessage) {
        final var strategy = getRedeliveryStrategy(STANDARD);
        final var redelivery =  new NotificationRedelivery(
            resultMessage.getCorrelationId(), event.getId(), resultMessage.getRedeliveryMessageBytes(), strategy.getName(),
            LocalDateTime.now().plus(strategy.getNextTimeValue(1), strategy.getNextTimeUnit(1)), 1);
        LOG.debug("Creating redelivery item correlationId {} for eventId {}", redelivery.getCorrelationId(), event.getId());
        return notificationRedeliveryRepo.save(redelivery);
    }

    private void updateNotificationRedelivery(NotificationRedelivery redelivery, NotificationResultMessage resultMessage,
        Handelse event) {
        final var strategy = getRedeliveryStrategy(redelivery.getRedeliveryStrategy());
        final var attemptedDeliveries = redelivery.getAttemptedDeliveries() + 1;
        final var maxRedeliveries = strategy.getMaxRedeliveries();

        if (attemptedDeliveries - 1 < maxRedeliveries) {
            LOG.debug("Updating redelivery notification for eventId {}", event.getId());
            redelivery.setAttemptedDeliveries(attemptedDeliveries);
            redelivery.setRedeliveryTime(redelivery.getRedeliveryTime()
                .plus(strategy.getNextTimeValue(attemptedDeliveries), strategy.getNextTimeUnit(attemptedDeliveries)));
            notificationRedeliveryRepo.save(redelivery);
            monitorLogResend(event, resultMessage, redelivery);
        } else {
            LOG.warn("Setting redelivery failure for eventId {}", event.getId());
            final var updatedEvent = setRedeliveryFailure(redelivery);
            notificationRedeliveryRepo.delete(redelivery);
            redelivery.setAttemptedDeliveries(attemptedDeliveries);
            monitorLogFailure(updatedEvent, resultMessage, redelivery);
        }
    }

    private Handelse setRedeliveryFailure(NotificationRedelivery existingRedelivery) {
        final var event = handelseRepo.findById(existingRedelivery.getEventId()).orElseThrow();
        event.setDeliveryStatus(FAILURE);
        return handelseRepo.save(event);
    }

    private NotificationRedeliveryStrategy getRedeliveryStrategy(NotificationRedeliveryStrategyEnum strategy) {
        return notificationRedeliveryStrategyFactory.getResendStrategy(strategy);
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
