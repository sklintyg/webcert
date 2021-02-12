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

package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Test
    public void shouldMonitorLogResendOnProcessOfNewRedelivery() {
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
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

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

        assertEquals(notificationResultMessage.getEvent().getId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().name(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationErrorType().name(), captureErrorId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationResultText(), captureResultText.getValue());
        assertNotNull(captureCurrentSendAttempt.getValue());
        assertNotNull(captureRedeliveryTime.getValue());
    }

    @Test
    public void shouldMonitorLogResendOnProcessOfExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var maxValueHigherThanCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);
        final var captureRedeliveryTime = ArgumentCaptor.forClass(LocalDateTime.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(notificationRedelivery.getAttemptedDeliveries() + 1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(notificationRedelivery.getAttemptedDeliveries() + 1);
        doReturn(maxValueHigherThanCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

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

        assertEquals(notificationResultMessage.getEvent().getId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().name(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationErrorType().name(), captureErrorId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationResultText(), captureResultText.getValue());
        assertNotNull(captureCurrentSendAttempt.getValue());
        assertNotNull(captureRedeliveryTime.getValue());
    }

    @Test
    public void shouldMonitorLogFailureWhenReachingMaxRedeliveries() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var maxValueEqualToCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries();

        // Attempted redeliveries should be increased before monitorlog on failure.
        final var expectedSendAttemptsAfterServiceCall = notificationRedelivery.getAttemptedDeliveries() + 1;

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(any(Long.class));
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(maxValueEqualToCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();

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

        assertEquals(notificationResultMessage.getEvent().getId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().name(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationErrorType().name(), captureErrorId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationResultText(), captureResultText.getValue());
        assertEquals(expectedSendAttemptsAfterServiceCall, captureCurrentSendAttempt.getValue().intValue());
    }

    @Test
    public void shouldCreateNewEventRecordWhenNoneExists() {
        final var notificationResultMessage = createNotificationResultMessage();

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(5).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(handelseRepository).save(notificationResultMessage.getEvent());
    }

    @Test
    public void shouldNotUpdateExistingEventWhenNoStatusChange() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var maxValueHigherThanCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(maxValueHigherThanCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doReturn(5).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verifyNoInteractions(handelseRepository);
    }

    @Test
    public void shouldCreateNewRedeliveryRecordWhenNoneExists() {
        final var notificationResultMessage = createNotificationResultMessage();
        var currentTimeStamp = LocalDateTime.now();

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(1);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(1);
        doReturn(NotificationRedeliveryStrategyEnum.STANDARD).when(notificationRedeliveryStrategy).getName();
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertEquals(notificationResultMessage.getEvent().getId(), captureRedelivery.getValue().getEventId());
        assertEquals(notificationResultMessage.getCorrelationId(), captureRedelivery.getValue().getCorrelationId());
        assertEquals(notificationResultMessage.getRedeliveryMessageBytes(), captureRedelivery.getValue().getMessage());
        assertEquals(NotificationRedeliveryStrategyEnum.STANDARD, captureRedelivery.getValue().getRedeliveryStrategy());
        assertEquals(1, captureRedelivery.getValue().getAttemptedDeliveries().intValue());
        assertTrue(captureRedelivery.getValue().getRedeliveryTime().isAfter(currentTimeStamp));
    }

    @Test
    public void shouldUpdateRedeliveryTimeAndSendAttemptOnExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var expectedSendAttemptAfterServiceCall = notificationRedelivery.getAttemptedDeliveries() + 1;
        final var expectedRedeliveryTimeAfterServiceCall = notificationRedelivery.getRedeliveryTime().plusMinutes(1L);
        final var maxValueHigherThanCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;

        final var captureRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(maxValueHigherThanCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(any(Integer.class));
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(any(Integer.class));
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).save(captureRedelivery.capture());
        assertEquals(expectedSendAttemptAfterServiceCall, captureRedelivery.getValue().getAttemptedDeliveries().intValue());
        assertEquals(expectedRedeliveryTimeAfterServiceCall, captureRedelivery.getValue().getRedeliveryTime());
    }

    @Test
    public void shouldUpdateEventDeliveryStatusOnFailure() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var maxValueEqualToCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(maxValueEqualToCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(any(Long.class));
        doAnswer(i -> i.getArguments()[0]).when(handelseRepository).save(any(Handelse.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());
        assertEquals(NotificationDeliveryStatusEnum.FAILURE, captureEvent.getValue().getDeliveryStatus());
    }

    @Test
    public void shouldDeleteRedeliveryRecordOnFailure() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(notificationRedelivery.getAttemptedDeliveries()).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(any(Long.class));
        doAnswer(i -> i.getArguments()[0]).when(handelseRepository).save(any(Handelse.class));

        notificationResultResendService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).delete(notificationRedelivery);
    }

    @Test
    public void shouldProperlyUpdateTimeAndSendAttemptOnConsecutiveRedeliveries() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var maxValueHigherThanCurrentSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 3;

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(any(NotificationRedeliveryStrategyEnum.class));
        doReturn(maxValueHigherThanCurrentSendAttempt).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        doAnswer(i -> i.getArguments()[0]).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        var expectedRedeliveryTime = notificationRedelivery.getRedeliveryTime().plusMinutes(1);
        var expectedSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(expectedSendAttempt);
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(expectedSendAttempt);
        notificationResultResendService.process(notificationResultMessage);
        assertEquals(expectedRedeliveryTime, notificationRedelivery.getRedeliveryTime());
        assertEquals(expectedSendAttempt, notificationRedelivery.getAttemptedDeliveries().intValue());

        expectedRedeliveryTime = notificationRedelivery.getRedeliveryTime().plusHours(1);
        expectedSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(expectedSendAttempt);
        doReturn(ChronoUnit.HOURS).when(notificationRedeliveryStrategy).getNextTimeUnit(expectedSendAttempt);
        notificationResultResendService.process(notificationResultMessage);
        assertEquals(expectedRedeliveryTime, notificationRedelivery.getRedeliveryTime());
        assertEquals(expectedSendAttempt, notificationRedelivery.getAttemptedDeliveries().intValue());

        expectedRedeliveryTime = notificationRedelivery.getRedeliveryTime().plusDays(1);
        expectedSendAttempt = notificationRedelivery.getAttemptedDeliveries() + 1;
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(expectedSendAttempt);
        doReturn(ChronoUnit.DAYS).when(notificationRedeliveryStrategy).getNextTimeUnit(expectedSendAttempt);
        notificationResultResendService.process(notificationResultMessage);
        assertEquals(expectedRedeliveryTime, notificationRedelivery.getRedeliveryTime());
        assertEquals(expectedSendAttempt, notificationRedelivery.getAttemptedDeliveries().intValue());
    }

    private NotificationRedelivery createNotificationRedelivery(NotificationResultMessage notificationResultMessage) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(notificationResultMessage.getCorrelationId());
        notificationRedelivery.setEventId(notificationResultMessage.getEvent().getId());
        notificationRedelivery.setAttemptedDeliveries(2);
        notificationRedelivery.setRedeliveryTime(LocalDateTime.now());
        notificationRedelivery.setMessage(new byte[100]);
        notificationRedelivery.setRedeliveryStrategy(NotificationRedeliveryStrategyEnum.STANDARD);
        return notificationRedelivery;
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createEvent());
        notificationResultMessage.setCorrelationId("CORRELATION_ID");
        notificationResultMessage.setResultType(createNotificationResultType());
        return notificationResultMessage;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(1000L);
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId("INTYGS_ID");
        event.setEnhetsId("ENHETS_ID");
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
        return event;
    }

    private NotificationResultType createNotificationResultType() {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(NotificationResultTypeEnum.ERROR);
        notificationResultType.setNotificationErrorType(NotificationErrorTypeEnum.TECHNICAL_ERROR);
        notificationResultType.setNotificationResultText("TECHNICAL_ERROR_TEXT");
        return notificationResultType;
    }
}
