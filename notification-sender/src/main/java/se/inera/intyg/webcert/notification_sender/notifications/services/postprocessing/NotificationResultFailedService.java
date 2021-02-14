package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Service
public class NotificationResultFailedService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultFailedService.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Transactional
    public void process(@NonNull NotificationResultMessage resultMessage) {
        var event = resultMessage.getEvent();
        final var existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());

        if (existingRedelivery.isEmpty()) {
            LOG.debug("Creating notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            event = createEvent(event);
        } else {
            LOG.debug("Updating notification event {} with delivery status {}", event.getId(), event.getDeliveryStatus());
            updateEvent(existingRedelivery.get());
            deleteNotificationRedelivery(existingRedelivery.get());
        }

        monitorLogFailure(event, resultMessage, existingRedelivery);
    }

    private Handelse createEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private Handelse updateEvent(NotificationRedelivery existingRedelivery) {
        final var event = handelseRepo.findById(existingRedelivery.getEventId()).orElseThrow();
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.FAILURE);
        return handelseRepo.save(event);
    }

    private Optional<NotificationRedelivery> getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
    }

    private void monitorLogFailure(Handelse event, NotificationResultMessage resultMessage, Optional<NotificationRedelivery> redelivery) {
        final var resultType = resultMessage.getResultType();
        final var errorId = resultType.getNotificationErrorType() != null ? resultType.getNotificationErrorType().name() : null;
        final var currentSendAttempt = redelivery.map(notificationRedelivery -> notificationRedelivery.getAttemptedDeliveries() + 1)
            .orElse(1);

        logService.logStatusUpdateForCareStatusFailure(event.getId(), event.getCode().name(), event.getEnhetsId(), event.getIntygsId(),
            resultMessage.getCorrelationId(), errorId, resultType.getNotificationResultText(), currentSendAttempt);
    }
}
