package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;

@Component
public class NotificationResultMessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultMessageSender.class);

    @Autowired
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplateNotificationPostProcessing;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean sendResultMessage(NotificationResultMessage resultMessage) {

        try {
            final var notificationMessageJson = objectMapper.writeValueAsString(resultMessage);

            jmsTemplateNotificationPostProcessing.send(session -> {
                TextMessage textMessage = session.createTextMessage(notificationMessageJson);
                textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, resultMessage.getEvent().getIntygsId());
                textMessage.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, resultMessage.getCorrelationId());
                textMessage.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, resultMessage.getEvent().getEnhetsId());
                textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, resultMessage.getEvent().getCode().value());
                return textMessage;
            });
            return true;
        } catch (Exception e) {
            LOG.error(String.format("Exception occured sending NotificationResultMessage after exception %s",
                resultMessage), e);
            return false;
        }
    }
}
