/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING;
import static se.inera.intyg.webcert.common.Constants.JMS_TIMESTAMP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import javax.jms.JMSException;
import javax.jms.Message;
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
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryServiceTest {

    @Mock
    private HandelseRepository handelseRepo;

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Mock
    private FeaturesHelper featuresHelper;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private NotificationRedeliveryService notificationRedeliveryService;

    @Test
    public void shallReturnNotificationRedeliveriesScheduledToBeResend() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedNotificationRedeliveryList = Arrays.asList(expectedNotificationRedelivery);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findByRedeliveryTimeLessThan(any(LocalDateTime.class));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

        assertNotNull(actualNotificationRedeliveryList);
        assertEquals(expectedNotificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
        for (var actualNotificationRedelivery : actualNotificationRedeliveryList) {
            assertTrue("Doesn't contain actual notification redelivery",
                expectedNotificationRedeliveryList.contains(actualNotificationRedelivery));
        }
    }

    @Test
    public void shallReturnNotificationRedeliveriesBasedOnEventInChronologicalAscendingOrder() {
        final var expectedNotificationRedeliveryFirst = createNotificationRedelivery(1000L);
        final var expectedNotificationRedeliveryMiddle = createNotificationRedelivery(2000L);
        final var expectedNotificationRedeliveryLast = createNotificationRedelivery(3000L);
        final var expectedNotificationRedeliveryList = Arrays
            .asList(expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle, expectedNotificationRedeliveryLast);
        final var incorrectlyOrderedNotificationDeliveryList = Arrays
            .asList(expectedNotificationRedeliveryLast, expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle);

        doReturn(incorrectlyOrderedNotificationDeliveryList).when(notificationRedeliveryRepo)
            .findByRedeliveryTimeLessThan(any(LocalDateTime.class));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

        assertNotNull(actualNotificationRedeliveryList);
        assertEquals(expectedNotificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
        for (int i = 0; i < expectedNotificationRedeliveryList.size(); i++) {
            assertEquals(expectedNotificationRedeliveryList.get(i).getEventId(), actualNotificationRedeliveryList.get(i).getEventId());
        }
    }

    @Test
    public void shallAddCorrelationIdIfMissingInNotificationRedelivery() {
        final var expectedNotificationRedeliveryFirst = createNotificationRedelivery(1000L);
        final var expectedNotificationRedeliveryMiddle = createNotificationRedelivery(2000L, null);
        final var expectedNotificationRedeliveryLast = createNotificationRedelivery(3000L);
        final var expectedNotificationRedeliveryList = Arrays
            .asList(expectedNotificationRedeliveryFirst, expectedNotificationRedeliveryMiddle, expectedNotificationRedeliveryLast);

        doReturn(expectedNotificationRedeliveryList).when(notificationRedeliveryRepo)
            .findByRedeliveryTimeLessThan(any(LocalDateTime.class));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();

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
            assertFalse("Expected an exception to be thrown when sending message failed!", true);
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void shallRemoveNotificationRedeliveryIfWebcertMessagingFeatureIsOff() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        doReturn(false).when(featuresHelper).isFeatureActive(FEATURE_USE_WEBCERT_MESSAGING);

        notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);

        verify(notificationRedeliveryRepo).delete(expectedNotificationRedelivery);
    }

    @Test
    public void shallUpdateEventWithDeliveryStatusClientIfWebcertMessagingFeatureIsOff() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(false).when(featuresHelper).isFeatureActive(FEATURE_USE_WEBCERT_MESSAGING);

        notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);

        verify(handelseRepo).save(captureEvent.capture());

        assertEquals(NotificationDeliveryStatusEnum.CLIENT, captureEvent.getValue().getDeliveryStatus());
    }

    @Test
    public void shallClearRedeliveryTimeOnResend() {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedEvent = createEvent();
        final var expectedMessage = "MESSAGE_AS_BYTES".getBytes();

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(true).when(featuresHelper).isFeatureActive(FEATURE_USE_WEBCERT_MESSAGING);

        notificationRedeliveryService.resend(expectedNotificationRedelivery, expectedEvent, expectedMessage);

        verify(notificationRedeliveryRepo).save(captureRedelivery.capture());

        assertNull("RedeliveryTime hasn't been cleared on resend", captureRedelivery.getValue().getRedeliveryTime());
    }

    @Test
    public void shallLimitBatchWhenMoreNotificationsAreUpForRedelivery() {
        final var expectedBatchSize = 10;
        final var notificationRedeliveryList = new ArrayList(20);
        for (int i = 0; i < 20; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        doReturn(notificationRedeliveryList).when(notificationRedeliveryRepo).findByRedeliveryTimeLessThan(any(LocalDateTime.class));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(expectedBatchSize);

        assertEquals(expectedBatchSize, actualNotificationRedeliveryList.size());
    }

    @Test
    public void shallNotLimitBatchWhenThereAreLessNotificationsUpForRedelivery() {
        final var expectedBatchSize = 10;
        final var notificationRedeliveryList = new ArrayList(9);
        for (int i = 0; i < 9; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        doReturn(notificationRedeliveryList).when(notificationRedeliveryRepo).findByRedeliveryTimeLessThan(any(LocalDateTime.class));

        final var actualNotificationRedeliveryList = notificationRedeliveryService.getNotificationsForRedelivery(expectedBatchSize);

        assertEquals(notificationRedeliveryList.size(), actualNotificationRedeliveryList.size());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        return createNotificationRedelivery(1000L);
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId) {
        return createNotificationRedelivery(eventId, "CORRELATION_ID");
    }

    private NotificationRedelivery createNotificationRedelivery(Long eventId, String correlationId) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(correlationId);
        notificationRedelivery.setEventId(eventId);
        notificationRedelivery.setMessage("MESSAGE".getBytes());
        notificationRedelivery.setRedeliveryTime(LocalDateTime.now());
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
}
