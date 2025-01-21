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
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.VALIDATION_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultFailedServiceTest {

    @Mock
    HandelseRepository handelseRepository;

    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    MonitoringLogService monitoringLogService;

    @InjectMocks
    NotificationResultFailedService notificationResultFailedService;

    private static final String CORRELATION_ID = "CORRELATION_ID";
    private static final byte[] STATUS_UPDATE_XML = "STATUS_UPDATE_XML".getBytes();

    private static final Long EVENT_ID = 1000L;
    private static final String UNIT_ID = "UNIT_ID";
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final HandelsekodEnum EVENT_ENUM = HandelsekodEnum.SKAPAT;

    private static final NotificationResultTypeEnum RESULT_TYPE_ENUM = ERROR;
    private static final NotificationErrorTypeEnum RESULT_ERROR_TYPE_ENUM = VALIDATION_ERROR;
    private static final String RESULT_TEXT = "VALIDATION_ERROR_TEXT";

    private static final int ATTEMPTED_DELIVERIES = 2;

    @Test
    public void shouldMonitorLogFailureOnProcessingNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(createSavedEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());

        notificationResultFailedService.process(notificationResultMessage);

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
        assertEquals(1, captureCurrentSendAttempt.getValue().intValue());
    }

    @Test
    public void shouldMonitorLogFailureOnProcessingRedeliveredNotification() {
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
        doReturn(Optional.of(createRedeliveredEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultFailedService.process(notificationResultMessage);

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
    public void shouldMonitorLogFailureOnProcessingRedeliveredNotificationWithNullAttemptedRedeliveries() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();
        notificationRedelivery.setAttemptedDeliveries(null);

        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(Optional.of(createRedeliveredEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultFailedService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusFailure(
            anyLong(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            captureCurrentSendAttempt.capture());

        assertEquals(1, captureCurrentSendAttempt.getValue().intValue());
    }

    @Test
    public void shouldCreateNewEventOnProcessingNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(createSavedEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());

        notificationResultFailedService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(notificationResultMessage.getEvent(), captureEvent.getValue());
        assertNull(captureEvent.getValue().getId());
    }

    @Test
    public void shouldSetDeliveryStatusFailureOnRedeliveredNotification() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(Optional.of(createRedeliveredEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultFailedService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(EVENT_ID, captureEvent.getValue().getId());
        assertEquals(NotificationDeliveryStatusEnum.FAILURE, captureEvent.getValue().getDeliveryStatus());
    }

    @Test
    public void shouldDeleteRedeliveryOnProcessingRedeliveredNotification() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery();

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage
            .getCorrelationId());
        doReturn(Optional.of(createRedeliveredEvent())).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doAnswer(i -> i.getArgument(0)).when(handelseRepository).save(any(Handelse.class));

        notificationResultFailedService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).delete(captureRedelivery.capture());
        assertEquals(notificationRedelivery, captureRedelivery.getValue());
        assertEquals(EVENT_ID, captureRedelivery.getValue().getEventId());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(CORRELATION_ID);
        notificationRedelivery.setEventId(EVENT_ID);
        notificationRedelivery.setAttemptedDeliveries(ATTEMPTED_DELIVERIES);
        return notificationRedelivery;
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createUnsavedEvent());
        notificationResultMessage.setCorrelationId(CORRELATION_ID);
        notificationResultMessage.setStatusUpdateXml(STATUS_UPDATE_XML);
        notificationResultMessage.setResultType(createNotificationResultType());
        return notificationResultMessage;
    }

    private Handelse createUnsavedEvent() {
        final var event = new Handelse();
        event.setCode(EVENT_ENUM);
        event.setIntygsId(CERTIFICATE_ID);
        event.setEnhetsId(UNIT_ID);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.FAILURE);
        return event;
    }

    private Handelse createSavedEvent() {
        final var event = createUnsavedEvent();
        event.setId(EVENT_ID);
        return event;
    }

    private Handelse createRedeliveredEvent() {
        final var event = createUnsavedEvent();
        event.setId(EVENT_ID);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
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
