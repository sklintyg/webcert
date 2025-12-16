package se.inera.intyg.webcert.web.service.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

@ExtendWith(MockitoExtension.class)
class NotificationRedeliveryAggregatorTest {

    private static final String STATUS_UPDATE_XML = "statusUpdateXml";
    private static final byte[] STATUS_UPDATE_XML_BYTES = "statusUpdateXml".getBytes(StandardCharsets.UTF_8);
    @Mock
    NotificationRedeliveryStatusUpdateCreatorService notificationRedeliveryStatusUpdateCreatorService;
    @Mock
    NotificationRedeliveryService notificationRedeliveryService;
    @Mock
    NotificationRedeliveryForCertificateService notificationRedeliveryForCertificateService;
    @InjectMocks
    NotificationRedeliveryAggregator notificationRedeliveryAggregator;

    @Test
    void shallSendNotificationWithNotificationRedeliveryServiceIfRedeliveryHandledIsFalse()
        throws TemporaryException, ModuleNotFoundException, JAXBException, IOException, ModuleException {
        final var notificationRedelivery = mock(NotificationRedelivery.class);
        final var handelse = mock(Handelse.class);

        doReturn(STATUS_UPDATE_XML).when(notificationRedeliveryStatusUpdateCreatorService).getCertificateStatusUpdateXml(
            notificationRedelivery, handelse
        );
        doReturn(false).when(notificationRedeliveryForCertificateService).resend(notificationRedelivery, handelse);

        notificationRedeliveryAggregator.resend(notificationRedelivery, handelse);

        verify(notificationRedeliveryService).resend(
            notificationRedelivery, handelse, STATUS_UPDATE_XML_BYTES
        );
    }

    @Test
    void shallSendNotificationWithNotificationRedeliveryForCertificateServiceIfRedeliveryHandledIsTrue()
        throws TemporaryException, ModuleNotFoundException, JAXBException, IOException, ModuleException {
        final var notificationRedelivery = mock(NotificationRedelivery.class);
        final var handelse = mock(Handelse.class);
        doReturn(true).when(notificationRedeliveryForCertificateService).resend(notificationRedelivery, handelse);

        final var resend = notificationRedeliveryAggregator.resend(notificationRedelivery, handelse);

        assertTrue(resend);
        verifyNoInteractions(notificationRedeliveryService);
    }
}