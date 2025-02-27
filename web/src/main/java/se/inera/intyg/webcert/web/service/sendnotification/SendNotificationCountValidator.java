package se.inera.intyg.webcert.web.service.sendnotification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Component
@RequiredArgsConstructor
public class SendNotificationCountValidator {

    private final HandelseRepository handelseRepository;

    @Value("${max.allowed.notification.send}")
    private int maxAllowedNotificationSend;

    public void careGiver(String careGiverId, SendNotificationsForCareGiverRequestDTO request) {
        final var insertsForCareGiver = handelseRepository.countInsertsForCareGiver(
            careGiverId, getStatusesAsString(request.getStatuses()), request.getStart(), request.getEnd());
        if (insertsForCareGiver > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForCareGiver));
        }
    }

    private static List<String> getStatusesAsString(List<NotificationDeliveryStatusEnum> statuses) {
        return statuses.stream().map(NotificationDeliveryStatusEnum::value).toList();
    }

    public void certificates(SendNotificationsForCertificatesRequestDTO request) {
        final var insertsForCertificates = handelseRepository.countInsertsForCertificates(
            request.getCertificateIds(), getStatusesAsString(request.getStatuses()));
        if (insertsForCertificates > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForCertificates));
        }
    }

    public void units(SendNotificationsForUnitsRequestDTO request) {
        final var insertsForUnits = handelseRepository.countInsertsForUnits(
            request.getUnitIds(), getStatusesAsString(request.getStatuses()), request.getStart(), request.getEnd());
        if (insertsForUnits > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForUnits));
        }
    }

    public void notification(String notificationId) {
        final var insertsForNotification = handelseRepository.countNotification(notificationId);
        if (insertsForNotification > maxAllowedNotificationSend) {
            throw new IllegalArgumentException(buildErrorMessage(insertsForNotification));
        }
    }

    private String buildErrorMessage(int numberOfInserts) {
        return "Request exceeded maximum number of notifications allowed to be sent. Number of inserts '%s' exceeds the limit of '%s'"
            .formatted(numberOfInserts, maxAllowedNotificationSend);
    }
}
