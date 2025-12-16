package se.inera.intyg.webcert.web.service.notification;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedeliveryAggregator {
    private final NotificationRedeliveryStatusUpdateCreatorService notificationRedeliveryStatusUpdateCreatorService;
    private final NotificationRedeliveryService notificationRedeliveryService;
    private final NotificationRedeliveryForCertificateService notificationRedeliveryForCertificateService;


    public boolean resend(NotificationRedelivery notificationRedelivery, Handelse event)
        throws TemporaryException, ModuleNotFoundException, JAXBException, IOException, ModuleException {

        final var redeliveryHandled = notificationRedeliveryForCertificateService.resend(notificationRedelivery, event);

        if (redeliveryHandled) {
            return true;
        }

        notificationRedeliveryService.resend(
            notificationRedelivery,
            event,
            getCertificateStatusUpdateXmlABytes(
                notificationRedelivery,
                event
            )
        );

        return true;
    }

    private byte[] getCertificateStatusUpdateXmlABytes(NotificationRedelivery notificationRedelivery,
        Handelse event)
        throws ModuleNotFoundException, TemporaryException, ModuleException, IOException, JAXBException {
        final var statusUpdateXml = notificationRedeliveryStatusUpdateCreatorService
            .getCertificateStatusUpdateXml(notificationRedelivery, event);
        return statusUpdateXml.getBytes(StandardCharsets.UTF_8);
    }
}
