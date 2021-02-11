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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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

    @Test
    public void shouldMonitorLogFailureOnProcessResult() {
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
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());

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

        assertEquals(notificationResultMessage.getEvent().getId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().name(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationErrorType().name(), captureErrorId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationResultText(), captureResultText.getValue());
        assertEquals(1, captureCurrentSendAttempt.getValue().intValue());
    }

    @Test
    public void shouldMonitorLogFailureOnProcessResultWhenExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureErrorId = ArgumentCaptor.forClass(String.class);
        final var captureResultText = ArgumentCaptor.forClass(String.class);
        final var captureCurrentSendAttempt = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(notificationResultMessage.getEvent()
            .getId());

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

        assertEquals(notificationResultMessage.getEvent().getId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().name(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationErrorType().name(), captureErrorId.getValue());
        assertEquals(notificationResultMessage.getResultType().getNotificationResultText(), captureResultText.getValue());
        assertEquals(notificationRedelivery.getAttemptedDeliveries(), captureCurrentSendAttempt.getValue());
    }

    @Test
    public void shouldCreateNewEventOnProcessWithNoExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();

        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());

        notificationResultFailedService.process(notificationResultMessage);

        verify(handelseRepository).save(notificationResultMessage.getEvent());
    }

    @Test
    public void shouldUpdateEventOnProcessWithExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(createNotificationRedelivery(notificationResultMessage))).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(notificationResultMessage.getEvent()
            .getId());

        notificationResultFailedService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());

        assertEquals(notificationResultMessage.getEvent().getDeliveryStatus().name(), captureEvent.getValue().getDeliveryStatus().name());
    }

    @Test
    public void shouldDeleteRedeliveryOnProcessWithExistingRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(notificationResultMessage.getEvent()).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.of(notificationResultMessage.getEvent())).when(handelseRepository).findById(notificationResultMessage.getEvent()
            .getId());

        notificationResultFailedService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).delete(notificationRedelivery);
    }

    private NotificationRedelivery createNotificationRedelivery(NotificationResultMessage notificationResultMessage) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(notificationResultMessage.getCorrelationId());
        notificationRedelivery.setEventId(notificationResultMessage.getEvent().getId());
        notificationRedelivery.setAttemptedDeliveries(2);
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
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.FAILURE);
        return event;
    }

    private NotificationResultType createNotificationResultType() {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(NotificationResultTypeEnum.ERROR);
        notificationResultType.setNotificationErrorType(NotificationErrorTypeEnum.VALIDATION_ERROR);
        notificationResultType.setNotificationResultText("VALIDATION_ERROR_TEXT");
        return notificationResultType;
    }
}
