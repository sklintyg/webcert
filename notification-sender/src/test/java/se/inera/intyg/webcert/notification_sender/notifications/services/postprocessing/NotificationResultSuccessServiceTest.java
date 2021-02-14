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

    @Test
    public void shallMakeMonitorLogOnProcessResult() {
        final var notificationResultMessage = createNotificationResultMessage();

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);

        doReturn(eventAsSaved(notificationResultMessage.getEvent())).when(handelseRepository).save(notificationResultMessage.getEvent());
        doReturn(Optional.empty()).when(notificationRedeliveryRepository).findByCorrelationId(notificationResultMessage.getCorrelationId());

        notificationResultSuccessService.process(notificationResultMessage);

        verify(monitoringLogService).logStatusUpdateForCareStatusSuccess(
            captureEventId.capture(),
            captureEventType.capture(),
            captureCertificateId.capture(),
            captureCorrelationId.capture(),
            captureLogicalAddress.capture());

        assertEquals(EVENT_ID, captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().value(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
    }

    @Test
    public void shallMakeMonitorLogOnProcessResultForRedelivery() {
        final var notificationResultMessage = createNotificationResultMessage();
        final var notificationRedelivery = createNotificationRedelivery(notificationResultMessage);
        final var eventAsSaved = eventAsSaved(notificationResultMessage.getEvent());

        final var captureEventId = ArgumentCaptor.forClass(Long.class);
        final var captureEventType = ArgumentCaptor.forClass(String.class);
        final var captureCertificateId = ArgumentCaptor.forClass(String.class);
        final var captureCorrelationId = ArgumentCaptor.forClass(String.class);
        final var captureLogicalAddress = ArgumentCaptor.forClass(String.class);
        
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
            captureLogicalAddress.capture());

        assertEquals(notificationRedelivery.getEventId(), captureEventId.getValue());
        assertEquals(notificationResultMessage.getEvent().getCode().value(), captureEventType.getValue());
        assertEquals(notificationResultMessage.getEvent().getIntygsId(), captureCertificateId.getValue());
        assertEquals(notificationResultMessage.getCorrelationId(), captureCorrelationId.getValue());
        assertEquals(notificationResultMessage.getEvent().getEnhetsId(), captureLogicalAddress.getValue());
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