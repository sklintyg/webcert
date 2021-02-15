package se.inera.intyg.webcert.web.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;

@Service
public class NotificationResendService {

    private final NotificationRedeliveryService notificationRedeliveryService;

    @Autowired
    public NotificationResendService(NotificationRedeliveryService notificationRedeliveryService) {
        this.notificationRedeliveryService = notificationRedeliveryService;
    }

    public void resendScheduledNotifications() {
        
    }
}
