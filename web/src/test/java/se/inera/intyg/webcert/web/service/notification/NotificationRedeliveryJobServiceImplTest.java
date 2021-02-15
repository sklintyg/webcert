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

package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryJobServiceImplTest {

    @Mock
    private HandelseRepository handelseRepo;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private IntygService certificateService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UtkastService draftService;

    @Mock
    private UtkastRepository draftRepo;

    @Mock
    private SendNotificationStrategy sendNotificationStrategy;

    @Mock
    private ReferensService referenceService;

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @Mock
    private HsaPersonService hsaPersonService;

    @Mock
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Mock
    private NotificationMessageFactory notificationMessageFactory;

    @InjectMocks
    private NotificationRedeliveryJobServiceImpl notificationRedeliveryJobService;

    @Test
    public void shallResendANotificationThatIsUpForRedelivery() throws IOException {
        final var expectedEvent = createEvent();
        final var expectedNotificationRedelivery = createNotificationRedelivery(expectedEvent.getId());
        final var expectedStatusUpdateXml = "CERTIFICATE_STATUS_XML";
        final var notificationRedeliveryList = Arrays.asList(expectedNotificationRedelivery);

        final var captureNotificationRedelivery = ArgumentCaptor.forClass(NotificationRedelivery.class);
        final var captureBytes = ArgumentCaptor.forClass(byte[].class);

        final var notificationRedeliveryMessageMock = mock(NotificationRedeliveryMessage.class);

        doReturn(notificationRedeliveryList).when(notificationRedeliveryService).getNotificationsForRedelivery();
        doReturn(Optional.of(expectedEvent)).when(handelseRepo).findById(expectedNotificationRedelivery.getEventId());
        doReturn(notificationRedeliveryMessageMock).when(objectMapper)
            .readValue(expectedNotificationRedelivery.getMessage(), NotificationRedeliveryMessage.class);
        doReturn(true).when(notificationRedeliveryMessageMock).hasCertificate();
        doReturn(expectedStatusUpdateXml).when(certificateStatusUpdateForCareCreator).marshal(any());

        notificationRedeliveryJobService.resendScheduledNotifications();

        verify(notificationRedeliveryService).resend(
            captureNotificationRedelivery.capture(),
            captureBytes.capture());

        assertEquals(expectedNotificationRedelivery, captureNotificationRedelivery.getValue());
        assertEquals(expectedStatusUpdateXml.getBytes().length, captureBytes.getValue().length);
    }

    private NotificationRedelivery createNotificationRedelivery(Long id) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId("CORRELATION_ID");
        notificationRedelivery.setEventId(id);
        notificationRedelivery.setMessage("MESSAGE".getBytes());
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
