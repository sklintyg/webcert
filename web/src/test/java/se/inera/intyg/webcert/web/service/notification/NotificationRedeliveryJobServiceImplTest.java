/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.web.util.ReflectionUtils.setStaticFinalAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryJobServiceImplTest {

    @Mock
    private NotificationRedeliveryStatusUpdateCreatorService notificationRedeliveryStatusUpdateCreatorService;

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Mock
    private HandelseRepository eventRepository;

    @InjectMocks
    private NotificationRedeliveryJobServiceImpl notificationRedeliveryJobService;

    @Test
    public void shallResendANotificationThatIsUpForRedelivery() throws Exception {
        final var expectedNotificationRedelivery = createNotificationRedelivery();
        final var expectedStatusUpdateXml = "CERTIFICATE_STATUS_XML";
        final var notificationRedeliveryList = Collections.singletonList(expectedNotificationRedelivery);

        final var captureNotificationRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);
        final var captureBytes = ArgumentCaptor.forClass(byte[].class);

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery(any(int.class));
        doReturn(createEventsToReturn(1)).when(eventRepository).findAllById(any(Iterable.class));
        doReturn(expectedStatusUpdateXml).when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(eq(expectedNotificationRedelivery), any(Handelse.class));

        notificationRedeliveryJobService.resendScheduledNotifications(100);

        verify(notificationRedeliveryService).resend(
            captureNotificationRedelivery.capture(),
            any(Handelse.class),
            captureBytes.capture());

        assertEquals(expectedNotificationRedelivery, captureNotificationRedelivery.getValue());
        assertEquals(expectedStatusUpdateXml.getBytes().length, captureBytes.getValue().length);
    }

    @Test
    public void shallResendManyNotificationsThatAreUpForRedelivery() throws Exception {
        final var numberOfMessages = 100;
        final var notificationRedeliveryList = new ArrayList<NotificationRedelivery>();
        for (int i = 0; i < numberOfMessages; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery(any(int.class));
        doReturn(createEventsToReturn(1)).when(eventRepository).findAllById(any(Iterable.class));
        doReturn("CERTIFICATE_STATUS_XML").when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(any(NotificationRedelivery.class), any(Handelse.class));

        notificationRedeliveryJobService.resendScheduledNotifications(100);

        verify(notificationRedeliveryService, VerificationModeFactory.times(numberOfMessages))
            .resend(any(NotificationRedelivery.class), any(Handelse.class), any(byte[].class));
    }

    @Test
    public void shallResendNoneIfNoNotificationsAreUpForRedelivery() {
        doReturn(Collections.emptyList()).when(notificationRedeliveryService).getNotificationsForRedelivery(any(int.class));

        notificationRedeliveryJobService.resendScheduledNotifications(100);

        verify(notificationRedeliveryService, never()).resend(any(), any(), any());
    }

    @Test
    public void shallResendNotificationsThatAreUpForRedeliveryEvenWhenSomeFail() throws Exception {
        final var numberOfMessages = 10;
        final var notificationRedeliveryList = new ArrayList<NotificationRedelivery>();
        for (int i = 0; i < numberOfMessages; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        final var failingNotificationRedelivery = createNotificationRedelivery();
        notificationRedeliveryList.add(3, failingNotificationRedelivery);
        notificationRedeliveryList.add(7, failingNotificationRedelivery);

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery(any(int.class));
        doReturn(createEventsToReturn(1)).when(eventRepository).findAllById(any(Iterable.class));
        doReturn("CERTIFICATE_STATUS_XML").when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(any(NotificationRedelivery.class), any(Handelse.class));
        doThrow(new RuntimeException("Failed!")).when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(eq(failingNotificationRedelivery), any(Handelse.class));

        notificationRedeliveryJobService.resendScheduledNotifications(100);

        verify(notificationRedeliveryService, VerificationModeFactory.times(numberOfMessages))
            .resend(any(NotificationRedelivery.class), any(Handelse.class), any(byte[].class));
    }

    @Test
    public void shallLogSuccessAndFailures() throws Exception {
        final var LOG = mock(Logger.class);
        setStaticFinalAttribute(NotificationRedeliveryJobServiceImpl.class, "LOG", LOG);

        final var numberOfSuccess = 10;
        final var notificationRedeliveryList = new ArrayList<NotificationRedelivery>();
        for (int i = 0; i < numberOfSuccess; i++) {
            notificationRedeliveryList.add(createNotificationRedelivery());
        }

        final var numberOfFailures = 2;
        final var failingNotificationRedelivery = createNotificationRedelivery();
        notificationRedeliveryList.add(3, failingNotificationRedelivery);
        notificationRedeliveryList.add(7, failingNotificationRedelivery);

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery(any(int.class));
        doReturn(createEventsToReturn(1)).when(eventRepository).findAllById(any(Iterable.class));
        doReturn("CERTIFICATE_STATUS_XML").when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(any(NotificationRedelivery.class), any(Handelse.class));
        doThrow(new RuntimeException("Failed!")).when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(eq(failingNotificationRedelivery), any(Handelse.class));

        notificationRedeliveryJobService.resendScheduledNotifications(100);

        verify(LOG).debug(any(String.class), eq(numberOfSuccess + numberOfFailures), any(long.class), eq(numberOfFailures));
    }

    @Test
    public void shallLimitMessagesToBatchSize() {
        final var expectedBatch = 345;

        final var captureInt = ArgumentCaptor.forClass(int.class);

        notificationRedeliveryJobService.resendScheduledNotifications(expectedBatch);

        verify(notificationRedeliveryService).getNotificationsForRedelivery(captureInt.capture());

        assertEquals(expectedBatch, captureInt.getValue().intValue());
    }

    @Test
    public void shouldHandleErrorsWhenExceptionIsReceived()
        throws ModuleNotFoundException, TemporaryException, ModuleException, IOException, JAXBException {
        final var redelivery = createNotificationRedelivery();
        final var notificationRedeliveryList = new ArrayList<NotificationRedelivery>();
        notificationRedeliveryList.add(redelivery);

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery(any(Integer.class));
        doReturn(createEventsToReturn(1)).when(eventRepository).findAllById(any(Iterable.class));
        doThrow(WebCertServiceException.class).when(notificationRedeliveryStatusUpdateCreatorService)
            .getCertificateStatusUpdateXml(eq(redelivery), any(Handelse.class));

        notificationRedeliveryJobService.resendScheduledNotifications(any(Integer.class));

        verify(notificationRedeliveryService).handleErrors(eq(redelivery), any(Handelse.class), any(WebCertServiceException.class));
    }

    private NotificationRedelivery createNotificationRedelivery() {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId("CORRELATION_ID");
        notificationRedelivery.setEventId(1000L);
        notificationRedelivery.setMessage("MESSAGE".getBytes());
        return notificationRedelivery;
    }

    private List<Handelse> createEventsToReturn(int noOfEvents) {
        final var eventList = new ArrayList<Handelse>(noOfEvents);
        for (int i = 0; i < noOfEvents; i++) {
            final var event = new Handelse();
            event.setId((i + 1) * 1000L);
            eventList.add(event);
        }
        return eventList;
    }
}
