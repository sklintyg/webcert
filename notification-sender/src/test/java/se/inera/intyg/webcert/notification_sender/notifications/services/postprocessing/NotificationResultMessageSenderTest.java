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
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultMessageSenderTest {

    @Mock
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationResultMessageSender notificationResultMessageSender;

    private static final String CORRELATION_ID = "CORRELATION_ID";
    private static final byte[] STATUS_UPDATE_XML = "STATUS_UPDATE_XML".getBytes();

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";
    private static final HandelsekodEnum EVENT_ENUM = HandelsekodEnum.SKAPAT;

    private static final NotificationResultTypeEnum RESULT_TYPE_ENUM = OK;

    @Test
    public void shouldSetProperHeadersOnJmsMessage() throws JMSException {
        final var notificationResultMessage = createNotificationResultMessage();

        ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        doAnswer(i -> createTextMessage(i.getArgument(0))).when(session).createTextMessage(any(String.class));

        final var success = notificationResultMessageSender.sendResultMessage(notificationResultMessage);

        verify(jmsTemplate).send(messageCaptor.capture());
        assertTrue(success);

        final var message = messageCaptor.getValue().createMessage(session);
        assertEquals(CERTIFICATE_ID, message.getStringProperty(NotificationRouteHeaders.INTYGS_ID));
        assertEquals(CORRELATION_ID, message.getStringProperty(NotificationRouteHeaders.CORRELATION_ID));
        assertEquals(LOGICAL_ADDRESS, message.getStringProperty(NotificationRouteHeaders.LOGISK_ADRESS));
        assertEquals(EVENT_ENUM.name(), message.getStringProperty(NotificationRouteHeaders.HANDELSE));
        assertNotNull(((TextMessage) message).getText());
    }

    @Test
    public void shouldSetProperTextMessageOnJmsMessage() throws JMSException, JsonProcessingException {
        final var notificationResultMessage = createNotificationResultMessage();

        ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        doAnswer(i -> createTextMessage(i.getArgument(0))).when(session).createTextMessage(any(String.class));

        notificationResultMessageSender.sendResultMessage(notificationResultMessage);

        verify(jmsTemplate).send(messageCaptor.capture());

        final var message = messageCaptor.getValue().createMessage(session);
        final var capturedTextMessage = objectMapper.readValue(((TextMessage) message).getText(), NotificationResultMessage.class);
        assertNull(capturedTextMessage.getEvent().getId());
        assertNotNull(capturedTextMessage.getStatusUpdateXml());
        assertEquals(CORRELATION_ID, capturedTextMessage.getCorrelationId());
        assertEquals(CERTIFICATE_ID, capturedTextMessage.getEvent().getIntygsId());
        assertEquals(LOGICAL_ADDRESS, capturedTextMessage.getEvent().getEnhetsId());
        assertEquals(EVENT_ENUM, capturedTextMessage.getEvent().getCode());
        assertEquals(RESULT_TYPE_ENUM, capturedTextMessage.getResultType().getNotificationResult());
    }

    @Test
    public void shouldReturnTrueOnSuccessfulJmsDelivery() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var isSuccess = notificationResultMessageSender.sendResultMessage(notificationResultMessage);

        verify(jmsTemplate).send(any(MessageCreator.class));
        assertTrue(isSuccess);
    }

    @Test
    public void shouldReturnFalseOnException() {
        final var notificationResultMessage = createNotificationResultMessage();

        doThrow(RuntimeException.class).when(jmsTemplate).send(any(MessageCreator.class));

        final var isSuccess = notificationResultMessageSender.sendResultMessage(notificationResultMessage);
        verify(jmsTemplate).send(any(MessageCreator.class));
        assertFalse(isSuccess);
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createEvent());
        notificationResultMessage.setCorrelationId(CORRELATION_ID);
        notificationResultMessage.setResultType(createNotificationResultType());
        notificationResultMessage.setStatusUpdateXml(STATUS_UPDATE_XML);
        return notificationResultMessage;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setCode(EVENT_ENUM);
        event.setIntygsId(CERTIFICATE_ID);
        event.setEnhetsId(LOGICAL_ADDRESS);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
        return event;
    }

    private NotificationResultType createNotificationResultType() {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(RESULT_TYPE_ENUM);
        return notificationResultType;
    }

    private TextMessage createTextMessage(String s) throws JMSException {
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setText(s);
        return message;
    }
}
