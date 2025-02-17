package se.inera.intyg.webcert.web.service.sendnotification;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
            .statuses(request.getStatuses())
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    public static SendNotificationsForUnitsRequestDTO sanitize(SendNotificationsForUnitsRequestDTO request) {
        return SendNotificationsForUnitsRequestDTO.builder()
            .unitIds(removeBlankSpaces(request.getUnitIds()))
            .statuses(request.getStatuses())
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
}
