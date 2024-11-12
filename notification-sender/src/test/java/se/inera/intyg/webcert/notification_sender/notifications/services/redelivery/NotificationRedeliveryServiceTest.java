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
package se.inera.intyg.webcert.notification_sender.notifications.services.redelivery;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryServiceTest {

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private NotificationResultMessageCreator notificationResultMessageCreator;

    @Mock
    private NotificationResultMessageSender notificationResultMessageSender;

    @InjectMocks
    private NotificationRedeliveryService notificationRedeliveryService;

    @Test
    public void shallReturnNotificationRedeliveriesScheduledToBeResend() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedNotificationRedeliveryList = Collections.singletonList(expectedNotificationRedelivery);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findRedeliveryUpForDelivery(any(LocalDateTime.class), anyInt());

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(100);

        assertNotNull(actualNotificationRedeliveryList);
        assertEquals(expectedNotificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
        for (var actualNotificationRedelivery : actualNotificationRedeliveryList) {
            assertTrue("Doesn't contain actual notification redelivery",
                expectedNotificationRedeliveryList.contains(actualNotificationRedelivery));
        }
    }

    @Test
    public void shallClearRedeliveryTimeOnNotificationRedeliveriesScheduledToBeResend() {
        final var now = LocalDateTime.now();
        final var expectedNotificationRedeliveryFirst = createNotificationRedelivery(3000L, now);
        final var expectedNotificationRedeliveryMiddle = createNotificationRedelivery(1000L, now.plus(1, SECONDS));
        final var expectedNotificationRedeliveryLast = createNotificationRedelivery(2000L, now.plus(2, SECONDS));
        final var expectedNotificationRedeliveryList = Arrays
            .asList(expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle, expectedNotificationRedeliveryLast);

        final var captureEventIds = ArgumentCaptor.forClass(List.class);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findRedeliveryUpForDelivery(any(LocalDateTime.class), anyInt());

        notificationRedeliveryService.getNotificationsForRedelivery(100);

        verify(notificationRedeliveryRepo).clearRedeliveryTime(captureEventIds.capture());

        assertEquals(expectedNotificationRedeliveryList.size(), captureEventIds.getValue().size());
    }

    @Test
    public void shallReturnNotificationRedeliveriesBasedOnEventInChronologicalAscendingOrder() {
        final var now = LocalDateTime.now();
        final var expectedNotificationRedeliveryFirst = createNotificationRedelivery(3000L, now);
        final var expectedNotificationRedeliveryMiddle = createNotificationRedelivery(1000L, now.plus(1, SECONDS));
        final var expectedNotificationRedeliveryLast = createNotificationRedelivery(2000L, now.plus(2, SECONDS));
        final var expectedNotificationRedeliveryList = Arrays
            .asList(expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle, expectedNotificationRedeliveryLast);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findRedeliveryUpForDelivery(any(LocalDateTime.class), anyInt());

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(100);

        assertNotNull(actualNotificationRedeliveryList);
        assertEquals(expectedNotificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
        for (int i = 0; i < expectedNotificationRedeliveryList.size(); i++) {
            assertEquals(expectedNotificationRedeliveryList.get(i).getEventId(), actualNotificationRedeliveryList.get(i).getEventId());
        }
    }

    @Test
    public void shallAddCorrelationIdIfMissingInNotificationRedelivery() {
        final var expectedNotificationRedeliveryFirst = createNotificationRedelivery(1000L);
        final var expectedNotificationRedeliveryMiddle = createNotificationRedelivery(2000L, (String) null);
        final var expectedNotificationRedeliveryLast = createNotificationRedelivery(3000L);
        final var expectedNotificationRedeliveryList = Arrays
            .asList(expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle, expectedNotificationRedeliveryLast);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findRedeliveryUpForDelivery(any(LocalDateTime.class), anyInt());

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(100);

        assertNotNull(actualNotificationRedeliveryList);
        for (var notificationRedelivery : actualNotificationRedeliveryList) {
            assertNotNull("Missing correlation id", notificationRedelivery.getCorrelationId());
        }
    }

    @Test
    public void shallAddMessageOnJmsWhenResend() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        final var captureMessage = ArgumentCaptor.forClass(byte[].class);
        final var capturePostProcessor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);

        verify(jmsTemplate).convertAndSend(captureMessage.capture(), capturePostProcessor.capture());

        assertEquals(expectedMessage, captureMessage.getValue());
    }

    @Test
    public void shallAddPropertiesOnJmsMessageWhenResend() throws JMSException {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        final var captureMessage = ArgumentCaptor.forClass(byte[].class);
        final var capturePostProcessor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);

        verify(jmsTemplate).convertAndSend(captureMessage.capture(), capturePostProcessor.capture());

        final var mockMessage = mock(Message.class);
        capturePostProcessor.getValue().postProcessMessage(mockMessage);

        verify(mockMessage).setStringProperty(CORRELATION_ID, expectedNotificationRedelivery.getCorrelationId());
        verify(mockMessage).setStringProperty(INTYGS_ID, expectedEvent.getIntygsId());
        verify(mockMessage).setStringProperty(LOGISK_ADRESS, expectedEvent.getEnhetsId());
        verify(mockMessage).setStringProperty(USER_ID, expectedEvent.getHanteratAv());
        verify(mockMessage).setLongProperty(eq(JMS_TIMESTAMP), any(Long.class));
    }

    @Test
    public void shallThrowExceptionIfAddingMessageOnJmsFails() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        final var mockedException = mock(JmsException.class);
        doThrow(mockedException).when(jmsTemplate).convertAndSend(any(byte[].class), any(MessagePostProcessor.class));

        try {
            notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);
            fail("Expected an exception to be thrown when sending message failed!");
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallFetchEventBeforeCallingResendIfNeeded() {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var message = "MESSAGE_AS_BYTES".getBytes();

        notificationRedeliveryService.resend(notificationRedelivery, event, message);

        verify(jmsTemplate).convertAndSend(eq(message), any(MessagePostProcessor.class));
    }

    @Test
    public void shallReturnEmptyListIfBatchSizeIsZero() {
        final var expectedBatchSize = 0;

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(expectedBatchSize);

        assertEquals(expectedBatchSize, actualNotificationRedeliveryList.size());
    }

    @Test
    public void shallLimitBatchWhenRetrievingRedeliveriesUpForRedelivery() {
        final var expectedBatchSize = 10;
        final var notificationRedeliveryList = new ArrayList<NotificationRedelivery>(9);
        for (int i = 0; i < 9; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        doReturn(notificationRedeliveryList).when(notificationRedeliveryRepo)
            .findRedeliveryUpForDelivery(any(LocalDateTime.class), eq(expectedBatchSize));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(expectedBatchSize);

        assertEquals(notificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
    }

    @Test
    public void shouldSendResultMessageToPostProcessor() {
        final var event = createEvent();
        final var notificationRedelivery = createNotificationRedelivery();
        final var notificationResultMessage = createNotificationResultMessage();
        final var exception = new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "EXCEPTION");

        doReturn(notificationResultMessage).when(notificationResultMessageCreator).createFailureMessage(event,
            notificationRedelivery, exception);

        notificationRedeliveryService.handleErrors(notificationRedelivery, event, exception);

        verify(notificationResultMessageSender).sendResultMessage(notificationResultMessage);
    }

    private NotificationRedelivery createNotificationRedelivery() {
        return createNotificationRedelivery(1000L);
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId) {
        return createNotificationRedelivery(eventId, "CORRELATION_ID");
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId, LocalDateTime redeliveryTime) {
        return createNotificationRedelivery(eventId, "CORRELATION_ID", redeliveryTime);
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId, String correlationId) {
        return createNotificationRedelivery(eventId, correlationId, LocalDateTime.now());
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId, String correlationId, LocalDateTime redeliveryTime) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(correlationId);
        notificationRedelivery.setEventId(eventId);
        notificationRedelivery.setMessage("MESSAGE".getBytes());
        notificationRedelivery.setRedeliveryTime(redeliveryTime);
        return notificationRedelivery;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(1000L);
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId("INTYGS_ID");
        event.setEnhetsId("ENHETS_ID");
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.SUCCESS);
        return event;
    }

    private NotificationResultMessage createNotificationResultMessage() {
        return new NotificationResultMessage();
    }
}
