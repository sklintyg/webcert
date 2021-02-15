package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResendServiceTest {

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @InjectMocks
    private NotificationResendService notificationResendService;

    @Test
    public void shallResendANotificationThatIsUpForRedelivery() {
        final NotificationRedelivery expectedNotification = createNotificationRedelivery();
        final var notificationsUpForRedelivery = Collections.singletonList(expectedNotification);

        final var captureNotificationRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(notificationsUpForRedelivery).when(notificationRedeliveryService).getNotificationsForRedelivery();

        notificationResendService.resendScheduledNotifications();

        verify(notificationRedeliveryService).resend(
            captureNotificationRedelivery.capture(),
            any(byte[].class));

        assertEquals(expectedNotification.getCorrelationId(), captureNotificationRedelivery.getValue().getCorrelationId());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId("CORRELATION_ID");
        return notificationRedelivery;
    }

}
