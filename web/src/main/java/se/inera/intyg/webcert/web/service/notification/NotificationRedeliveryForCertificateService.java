package se.inera.intyg.webcert.web.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@Component
@RequiredArgsConstructor
public class NotificationRedeliveryForCertificateService {

    private final CSIntegrationService csIntegrationService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    public boolean resend(NotificationRedelivery notificationRedelivery, Handelse event) {
        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(event.getIntygsId()))) {
            return false;
        }

        final var certificate = csIntegrationService.getInternalCertificate(
            event.getIntygsId()
        );

        publishCertificateStatusUpdateService.resend(certificate, event, notificationRedelivery);
        return true;
    }
}