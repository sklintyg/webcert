/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;

@ExtendWith(MockitoExtension.class)
class NotificationCertificateEventServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_SIGN = "certificate-sign";
    private final ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);

    private final NotificationCertificateMessage message = NotificationCertificateMessage.builder()
        .certificateId(CERTIFICATE_ID)
        .eventType(CERTIFICATE_SIGN)
        .build();
    @Mock
    @Qualifier("jmsCertificateEventTemplate")
    private JmsTemplate jmsTemplateNotificationPostProcessing;
    @Mock
    private Session session;

    @InjectMocks
    private NotificationCertificateEventService notificationCertificateEventService;

    @Nested
    class RouteHeadersTests {

        @BeforeEach
        void setUp() throws JMSException {
            final var activeMQTextMessage = new ActiveMQTextMessage();
            doReturn(activeMQTextMessage).when(session).createTextMessage();
        }

        @Test
        void shallSetHeaderCertificateId() throws JMSException {
            notificationCertificateEventService.send(message);
            verify(jmsTemplateNotificationPostProcessing).send(messageCaptor.capture());
            assertEquals(CERTIFICATE_ID,
                messageCaptor.getValue().createMessage(session).getStringProperty(NotificationRouteHeaders.CERTIFICATE_ID));
        }

        @Test
        void shallSetHeaderEventType() throws JMSException {
            notificationCertificateEventService.send(message);
            verify(jmsTemplateNotificationPostProcessing).send(messageCaptor.capture());
            assertEquals(CERTIFICATE_SIGN,
                messageCaptor.getValue().createMessage(session).getStringProperty(NotificationRouteHeaders.EVENT_TYPE));
        }

    }

    @Test
    void shallReturnTrueIfMessageIsSent() {
        assertTrue(notificationCertificateEventService.send(message));
    }

    @Test
    void shallReturnFalseIfExceptionIsThrown() {
        doThrow(IllegalStateException.class).when(jmsTemplateNotificationPostProcessing).send(any());
        assertFalse(notificationCertificateEventService.send(message));
    }
}
