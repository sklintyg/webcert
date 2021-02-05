package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
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

    public void process(NotificationResultMessage resultMessage) {
        executeSuccessOrFailure(resultMessage, resultMessage.getEvent());
    }

    private void executeSuccessOrFailure(NotificationResultMessage resultMessage, Handelse event) {
        NotificationRedelivery existingRedelivery = getExistingRedelivery(resultMessage.getCorrelationId());
        Handelse monitorEvent;
        if (existingRedelivery == null) {
            LOG.debug("Persisting notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            monitorEvent = persistEvent(event);
        } else {
            LOG.debug("Updating persisted notification event {} with delivery status {}", event.getCode().value(),
                event.getDeliveryStatus());
            monitorEvent = updateExistingEvent(existingRedelivery, event.getDeliveryStatus());
            deleteNotificationRedelivery(existingRedelivery);
        }
        // TODO: Consider overloading instead of sending null to parameters you don't have.
        monitorLog(monitorEvent, resultMessage, null); // log success or failure
    }

    private Handelse persistEvent(Handelse event) {
        return handelseRepo.save(event);
    }

    private Handelse updateExistingEvent(NotificationRedelivery existingRedelivery, NotificationDeliveryStatusEnum deliveryStatus) {
        Handelse event = handelseRepo.findById(existingRedelivery.getEventId()).orElseThrow();
        event.setDeliveryStatus(deliveryStatus);
        return handelseRepo.save(event);
    }

    private NotificationRedelivery getExistingRedelivery(String correlationId) {
        return notificationRedeliveryRepo.findByCorrelationId(correlationId).orElse(null);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
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
