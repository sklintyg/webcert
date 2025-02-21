package se.inera.intyg.webcert.web.service.sendnotification;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

public class SendNotificationRequestSanitizer {

    private SendNotificationRequestSanitizer() {
        throw new IllegalStateException("Utility class");
    }

    public static String sanitize(String value) {
        return StringUtils.deleteWhitespace(value);
    }

    public static SendNotificationsForCertificatesRequestDTO sanitize(SendNotificationsForCertificatesRequestDTO request) {
        return SendNotificationsForCertificatesRequestDTO.builder()
            .certificateIds(removeBlankSpaces(request.getCertificateIds()))
            .statuses(removeIncorrectStatuses(request.getStatuses()))
            .build();
    }

    public static SendNotificationsForUnitsRequestDTO sanitize(SendNotificationsForUnitsRequestDTO request) {
        return SendNotificationsForUnitsRequestDTO.builder()
            .unitIds(removeBlankSpaces(request.getUnitIds()))
            .statuses(removeIncorrectStatuses(request.getStatuses()))
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    public static SendNotificationsForCareGiverRequestDTO sanitize(SendNotificationsForCareGiverRequestDTO request) {
        return SendNotificationsForCareGiverRequestDTO.builder()
            .statuses(removeIncorrectStatuses(request.getStatuses()))
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    public static CountNotificationsForCareGiverRequestDTO sanitize(CountNotificationsForCareGiverRequestDTO request) {
        return CountNotificationsForCareGiverRequestDTO.builder()
            .statuses(removeIncorrectStatuses(request.getStatuses()))
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    private static List<String> removeBlankSpaces(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
            .map(StringUtils::deleteWhitespace)
            .toList();
    }

    public static List<NotificationDeliveryStatusEnum> removeIncorrectStatuses(List<NotificationDeliveryStatusEnum> statuses) {
        return statuses.stream()
            .filter(status -> status == NotificationDeliveryStatusEnum.SUCCESS || status == NotificationDeliveryStatusEnum.FAILURE)
            .toList();
    }
}
