package se.inera.intyg.webcert.web.service.sendnotification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Component
@RequiredArgsConstructor
public class SendNotificationCountValidator {

    private final NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Value("${max.allowed.notification.send}")
    private int maxAllowedNotificationSend;

    public void careGiver(String careGiverId, SendNotificationsForCareGiverRequestDTO request) {
        final var insertsForCareGiver = notificationRedeliveryRepository.countInsertsForCareGiver(
            careGiverId, request.getStatuses(), request.getStart(), request.getEnd());
        if (insertsForCareGiver > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForCareGiver));
        }
    }

    public void certiticates(SendNotificationsForCertificatesRequestDTO request) {
        final var insertsForCertificates = notificationRedeliveryRepository.countInsertsForCertificates(
            request.getCertificateIds(), request.getStatuses(), request.getStart(), request.getEnd());
        if (insertsForCertificates > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForCertificates));
        }
    }

    public void units(SendNotificationsForUnitsRequestDTO request) {
        final var insertsForUnits = notificationRedeliveryRepository.countInsertsForCertificates(
            request.getUnitIds(), request.getStatuses(), request.getStart(), request.getEnd());
        if (insertsForUnits > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForUnits));
        }
    }

    public void notification(String notificationId) {
        final var insertsForNotification = notificationRedeliveryRepository.countNotification(notificationId);
        if (insertsForNotification > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForNotification));
        }
    }

    private String buildErrorMessage(int numberOfInserts) {
        return "Request exceeded maximum number of notifications allowed to be sent. Number of inserts '%s' exceeds the limit of '%s'"
            .formatted(numberOfInserts, maxAllowedNotificationSend);
    }
}