package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Service
public class NotificationResultSuccessService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultSuccessService.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Transactional
    public void process(@NonNull NotificationResultMessage resultMessage) {
        final var event = resultMessage.getEvent();
        final var existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        if (existingRedelivery.isEmpty()) {
            LOG.debug("Persisting notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            createEvent(event);
        } else {
            LOG.debug("Updating persisted notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            updateEvent(event);
            deleteNotificationRedelivery(existingRedelivery.get());
        }

        logService.logStatusUpdateForCareStatusSuccess(event.getId(), event.getCode().name(), event.getIntygsId(),
            resultMessage.getCorrelationId(), event.getEnhetsId());
    }

    private Handelse createEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private Handelse updateEvent(Handelse event) {
        final var eventToUpdate = handelseRepo.findById(event.getId()).orElseThrow();
        eventToUpdate.setDeliveryStatus(event.getDeliveryStatus());
        return handelseRepo.save(eventToUpdate);
    }

    private Optional<NotificationRedelivery> getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
    }
}
