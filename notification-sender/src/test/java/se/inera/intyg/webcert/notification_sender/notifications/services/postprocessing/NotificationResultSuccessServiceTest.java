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
import static org.mockito.ArgumentMatchers.any;
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
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultSuccessServiceTest {

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private NotificationResultSuccessService notificationResultSuccessService;

    private static final Long EVENT_ID = 1000L;
    private static final Integer ATTEMPTED_DELIVERIES = 4;

    @Test
    public void shallMakeMonitorLogOnProcessingOfNewNotification() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureAttemptedDeliveries = ArgumentCaptor.forClass(Integer.class);

        doReturn(eventAsSaved(notificationResultMessage.getEvent())).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());

        notificationResultSuccessService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusSuccess(
            captureEventId.capture(),
            captureEventType.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureLogicalAddress.capture(),
            captureAttemptedDeliveries.capture());

        assertEquals(EVENT_ID, captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().value(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(1, captureAttemptedDeliveries.getValue().intValue());
    }

    @Test
    public void shallMakeMonitorLogOnProcessingRedeliveredNotification() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var eventAsSaved = eventAsSaved(notificationResultMessage.getEvent());

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        final var captureAttemptedDeliveries = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(Optional.of(eventAsSaved)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(eventAsSaved).when(handelseRepository).save(any(Handelse.class));

        notificationResultSuccessService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusSuccess(
            captureEventId.capture(),
            captureEventType.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureLogicalAddress.capture(),
            captureAttemptedDeliveries.capture());

        assertEquals(notificationRedelivery.getEventId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().value(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
        assertEquals(ATTEMPTED_DELIVERIES + 1, captureAttemptedDeliveries.getValue().intValue());
    }

    @Test
    public void shallMonitorLogOneAttemptedDeliveryWhenRedeliveryHasNullSendAttempts() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        notificationRedelivery.setAttemptedDeliveries(null);
        final var eventAsSaved = eventAsSaved(notificationResultMessage.getEvent());

        final var captureAttemptedDeliveries = ArgumentCaptor.forClass(Integer.class);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());
        doReturn(Optional.of(eventAsSaved)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(eventAsSaved).when(handelseRepository).save(any(Handelse.class));

        notificationResultSuccessService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusSuccess(any(Long.class), any(String.class), any(String.class),
            any(String.class), any(String.class), captureAttemptedDeliveries.capture());

        assertEquals(1, captureAttemptedDeliveries.getValue().intValue());
    }

    @Test
    public void shallCreateNewEventOnProcessResult() {
        final var notificationResultMessage = createNotificationResultMessage();

        doReturn(eventAsSaved(notificationResultMessage.getEvent())).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());

        notificationResultSuccessService.process(notificationResultMessage);

        verify(handelseRepository).save(notificationResultMessage.getEvent());
    }

    @Test
    public void shallUpdateEventOnProcessResult() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var eventAsSaved = eventAsSaved(notificationResultMessage.getEvent());

        final var captureEvent = ArgumentCaptor.forClass(Handelse.class);

        doReturn(Optional.of(eventAsSaved)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(eventAsSaved).when(handelseRepository).save(any(Handelse.class));
        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());

        notificationResultSuccessService.process(notificationResultMessage);

        verify(handelseRepository).save(captureEvent.capture());

        assertEquals(notificationResultMessage.getEvent().getDeliveryStatus().value(), captureEvent.getValue().getDeliveryStatus().value());
    }

    @Test
    public void shallRemoveNotificationRedeliveryOnProcessResult() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var eventAsSaved = eventAsSaved(notificationResultMessage.getEvent());

        doReturn(Optional.of(eventAsSaved)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(eventAsSaved).when(handelseRepository).save(any(Handelse.class));
        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(notificationResultMessage.getCorrelationId());

        notificationResultSuccessService.process(notificationResultMessage);

        verify(notificationRedeliveryRepository).delete(notificationRedelivery);
    }

    private NotificationRedelivery createNotificationRedelivery(NotificationResultMessage notificationResultMessage) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(notificationResultMessage.getCorrelationId());
        notificationRedelivery.setEventId(1000L);
        notificationRedelivery.setAttemptedDeliveries(ATTEMPTED_DELIVERIES);
        return notificationRedelivery;
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createEvent());
        notificationResultMessage.setCorrelationId("CORRELATION_ID");
        return notificationResultMessage;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId("INTYGS_ID");
        event.setEnhetsId("ENHETS_ID");
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.SUCCESS);
        return event;
    }

    private Handelse eventAsSaved(Handelse event) {
        final var savedEvent = new Handelse();
        savedEvent.setCode(event.getCode());
        savedEvent.setIntygsId(event.getIntygsId());
        savedEvent.setEnhetsId(event.getEnhetsId());
        savedEvent.setDeliveryStatus(event.getDeliveryStatus());
        savedEvent.setId(EVENT_ID);
        return savedEvent;
    }
}
