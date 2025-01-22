/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.TextMessage;
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
