package se.inera.intyg.webcert.web.service.sendnotification;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Component
@RequiredArgsConstructor
public class SendNotificationRequestSanitizer {

    public String sanitize(String value) {
        return removeBlankSpace(value);
    }

    public SendNotificationsForCertificatesRequestDTO sanitize(SendNotificationsForCertificatesRequestDTO request) {
        return SendNotificationsForCertificatesRequestDTO.builder()
            .certificateIds(removeBlankSpaces(request.getCertificateIds()))
            .statuses(request.getStatuses())
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    public SendNotificationsForUnitsRequestDTO sanitize(SendNotificationsForUnitsRequestDTO request) {
        return SendNotificationsForUnitsRequestDTO.builder()
            .unitIds(removeBlankSpaces(request.getUnitIds()))
            .statuses(request.getStatuses())
            .start(request.getStart())
            .end(request.getEnd())
            .activationTime(request.getActivationTime())
            .build();
    }

    private String removeBlankSpace(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", "");
    }

    private List<String> removeBlankSpaces(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
            .map(this::removeBlankSpace)
            .toList();
    }
}
