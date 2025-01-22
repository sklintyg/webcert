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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultResendServiceTest {

    @Mock
    HandelseRepository handelseRepository;

    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    NotificationRedeliveryStrategy notificationRedeliveryStrategy;

    @Mock
    NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    @InjectMocks
    NotificationResultResendService notificationResultResendService;

    private static final String CORRELATION_ID = "CORRELATION_ID";
    private static final byte[] STATUS_UPDATE_XML = "STATUS_UPDATE_XML".getBytes();

    private static final Long EVENT_ID = 1000L;
    private static final String UNIT_ID = "UNIT_ID";
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final HandelsekodEnum EVENT_ENUM = HandelsekodEnum.SKAPAT;

    private static final NotificationResultTypeEnum RESULT_TYPE_ENUM = ERROR;
    private static final NotificationErrorTypeEnum RESULT_ERROR_TYPE_ENUM = TECHNICAL_ERROR;
    private static final String RESULT_TEXT = "TECHNICAL_ERROR_TEXT";

    private static final int ATTEMPTED_DELIVERIES = 2;

    @Test
    public void shouldMonitorLogResendOnProcessingNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);
        final var captureRedeliveryTime = ArgumentCaptor.forClass(LocalDateTime.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(createSavedEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusResend(
            captureEventId.capture(),
            captureEventType.capture(),
            captureLogicalAddress.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureErrorId.capture(),
            captureResultText.capture(),
            captureCurrentSendAttempt.capture(),
            captureRedeliveryTime.capture());

        assertEquals(EVENT_ID, captureEventId.getValue());
        assertEquals(EVENT_ENUM.name(), captureEventType.getValue());
        assertEquals(UNIT_ID, captureLogicalAddress.getValue());
        assertEquals(CERTIFICATE_ID, captureCertificateId.getValue());
        assertEquals(CORRELATION_ID, captureCorrelationId.getValue());
        assertEquals(RESULT_ERROR_TYPE_ENUM.name(), captureErrorId.getValue());
        assertEquals(RESULT_TEXT, captureResultText.getValue());
        assertNotNull(captureCurrentSendAttempt.getValue());
        assertNotNull(captureRedeliveryTime.getValue());
    }

    @Test
    public void shouldMonitorLogResendOnProcessingRedeliveredNotification() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);
        final var captureRedeliveryTime = ArgumentCaptor.forClass(LocalDateTime.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(notificationRedelivery.getAttemptedDeliveries() + 1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy)
            .getNextTimeUnit(notificationRedelivery.getAttemptedDeliveries() + 1);
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusResend(
            captureEventId.capture(),
            captureEventType.capture(),
            captureLogicalAddress.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureErrorId.capture(),
            captureResultText.capture(),
            captureCurrentSendAttempt.capture(),
            captureRedeliveryTime.capture());

        assertEquals(EVENT_ID, captureEventId.getValue());
        assertEquals(EVENT_ENUM.name(), captureEventType.getValue());
        assertEquals(UNIT_ID, captureLogicalAddress.getValue());
        assertEquals(CERTIFICATE_ID, captureCertificateId.getValue());
        assertEquals(CORRELATION_ID, captureCorrelationId.getValue());
        assertEquals(RESULT_ERROR_TYPE_ENUM.name(), captureErrorId.getValue());
        assertEquals(RESULT_TEXT, captureResultText.getValue());
        assertNotNull(captureCurrentSendAttempt.getValue());
        assertNotNull(captureRedeliveryTime.getValue());
    }

    @Test
    public void shouldMonitorLogFailureWhenReachingMaxDeliveries() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(Optional.of(createSavedEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(ATTEMPTED_DELIVERIES).when(notificationRedeliveryStrategy).getMaxDeliveries();

        notificationResultResendService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusFailure(
            captureEventId.capture(),
            captureEventType.capture(),
            captureLogicalAddress.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureErrorId.capture(),
            captureResultText.capture(),
            captureCurrentSendAttempt.capture());

        assertEquals(EVENT_ID, captureEventId.getValue());
        assertEquals(EVENT_ENUM.name(), captureEventType.getValue());
        assertEquals(UNIT_ID, captureLogicalAddress.getValue());
        assertEquals(CERTIFICATE_ID, captureCertificateId.getValue());
        assertEquals(CORRELATION_ID, captureCorrelationId.getValue());
        assertEquals(RESULT_ERROR_TYPE_ENUM.name(), captureErrorId.getValue());
        assertEquals(RESULT_TEXT, captureResultText.getValue());
        assertEquals(ATTEMPTED_DELIVERIES + 1, captureCurrentSendAttempt.getValue().intValue());
    }

    @Test
    public void shouldCreateNewEventRecordWhenprocessingNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(createSavedEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(notificationResultMessage.getEvent(), captureEvent.getValue());
        assertNull(captureEvent.getValue().getId());
    }

    @Test
    public void shouldNotRedundantlyUpdateExistingEventWhenNoStatusChange() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verifyNoInteractions(handelseRepository);
    }

    @Test
    public void shouldCreateNewRedeliveryRecordWhenProcessingNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();
        var testStartTime = LocalDateTime.now();

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(createSavedEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doReturn(NotificationRedeliveryStrategyEnum.STANDARD).when(notificationRedeliveryStrategy).getName();
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertEquals(EVENT_ID, captureRedelivery.getValue().getEventId());
        assertEquals(CORRELATION_ID, captureRedelivery.getValue().getCorrelationId());
        assertEquals(STATUS_UPDATE_XML, captureRedelivery.getValue().getMessage());
        assertEquals(NotificationRedeliveryStrategyEnum.STANDARD, captureRedelivery.getValue().getRedeliveryStrategy());
        assertEquals(1, captureRedelivery.getValue().getAttemptedDeliveries().intValue());
        assertTrue(captureRedelivery.getValue().getRedeliveryTime().isAfter(testStartTime));
    }

    @Test
    public void shouldUpdateRedeliveryTimeAndSendAttemptOnExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        final var expectedRedeliveryTimeAfterServiceCall = notificationResultMessage.getNotificationSentTime()
            .plusMinutes(1L);
        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertEquals(ATTEMPTED_DELIVERIES + 1, captureRedelivery.getValue().getAttemptedDeliveries().intValue());
        assertEquals(expectedRedeliveryTimeAfterServiceCall, captureRedelivery.getValue().getRedeliveryTime());
    }

    @Test
    public void shouldUpdateRedeliveryMessageOnExistingRedeliveryIfMissing() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        notificationRedelivery.setMessage(null);
        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertNotNull("Must update redelivery message if it didn't exist from before", captureRedelivery.getValue().getMessage());
        assertEquals(STATUS_UPDATE_XML, captureRedelivery.getValue().getMessage());
    }

    @Test
    public void shouldUpdateEventDeliveryStatusOnFailure() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(ATTEMPTED_DELIVERIES).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(Optional.of(createSavedEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(EVENT_ID, captureEvent.getValue().getId());
        assertEquals(NotificationDeliveryStatusEnum.FAILURE, captureEvent.getValue().getDeliveryStatus());
    }

    @Test
    public void shouldDeleteRedeliveryRecordOnFailure() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(notificationRedelivery.getAttemptedDeliveries()).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(Optional.of(createSavedEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).delete(captureRedelivery.capture());
        assertEquals(notificationRedelivery, captureRedelivery.getValue());
        assertEquals(EVENT_ID, captureRedelivery.getValue().getEventId());
    }

    @Test
    public void shallConsiderNullRedeliveryAttemptsAsOne() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        notificationRedelivery.setAttemptedDeliveries(null);
        final var expectedRedeliveryAttempt = 1;

        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertEquals(expectedRedeliveryAttempt, captureRedelivery.getValue().getAttemptedDeliveries().intValue());
    }

    @Test
    public void shouldUpdateDeliveryStatusOnManualResendIfNeeded() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        final var valueToNotHitMaxDeliveries = ATTEMPTED_DELIVERIES + 2;
        final var savedEvent = createSavedEvent();
        savedEvent.setDeliveryStatus(NotificationDeliveryStatusEnum.FAILURE);
        notificationRedelivery.setAttemptedDeliveries(null);

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(valueToNotHitMaxDeliveries).when(notificationRedeliveryStrategy).getMaxDeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArgument(0)).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));
        doReturn(Optional.of(savedEvent)).when(handelseRepository).findById(notificationRedelivery.getEventId());

        notificationResultResendService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(NotificationDeliveryStatusEnum.RESEND, captureEvent.getValue().getDeliveryStatus());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(CORRELATION_ID);
        notificationRedelivery.setEventId(EVENT_ID);
        notificationRedelivery.setAttemptedDeliveries(ATTEMPTED_DELIVERIES);
        notificationRedelivery.setMessage(STATUS_UPDATE_XML);
        notificationRedelivery.setRedeliveryStrategy(NotificationRedeliveryStrategyEnum.STANDARD);
        return notificationRedelivery;
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createEvent());
        notificationResultMessage.setCorrelationId(CORRELATION_ID);
        notificationResultMessage.setNotificationSentTime(LocalDateTime.now());
        notificationResultMessage.setResultType(createNotificationResultType());
        notificationResultMessage.setStatusUpdateXml(STATUS_UPDATE_XML);
        return notificationResultMessage;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setCode(EVENT_ENUM);
        event.setIntygsId(CERTIFICATE_ID);
        event.setEnhetsId(UNIT_ID);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
        return event;
    }

    private Handelse createSavedEvent() {
        final var event = createEvent();
        event.setId(EVENT_ID);
        return event;
    }

    private NotificationResultType createNotificationResultType() {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(RESULT_TYPE_ENUM);
        notificationResultType.setNotificationErrorType(RESULT_ERROR_TYPE_ENUM);
        notificationResultType.setNotificationResultText(RESULT_TEXT);
        return notificationResultType;
    }
}
