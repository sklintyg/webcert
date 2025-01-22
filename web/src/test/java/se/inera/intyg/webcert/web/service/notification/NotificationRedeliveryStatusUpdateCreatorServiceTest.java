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
package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryStatusUpdateCreatorServiceTest {

    private final LocalDate LAST_DAY_FOR_ANSWER = LocalDate.now().plusDays(1);
    private final LocalDateTime EVENT_TIMESTAMP = LocalDateTime.now();

    @Mock
    private HandelseRepository handelseRepo;

    @Mock
    private UtkastRepository draftRepo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @Mock
    private HsaPersonService hsaPersonService;

    @Mock
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Mock
    private IntygService intygService;

    @Mock
    private NotificationMessageFactory notificationMessageFactory;

    @InjectMocks
    private NotificationRedeliveryStatusUpdateCreatorService notificationRedeliveryStatusUpdateCreatorService;

    @Test
    public void shallUseStatusUpdateXmlFromNotificationRedeliveryMessageIfExists() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedStatusUpdateXml = "STATUS_UPDATE_XML";

        setupMockToReturnStatusUpdateXml(expectedStatusUpdateXml);

        final var actualStatusUpdateXml = notificationRedeliveryStatusUpdateCreatorService
            .getCertificateStatusUpdateXml(notificationRedelivery, event);

        assertNotNull(actualStatusUpdateXml);
        assertEquals(expectedStatusUpdateXml, actualStatusUpdateXml);
    }

    @Test
    public void shallCreateStatusUpdateFromDraftIfRedeliveryMessageIsMissing() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var event = createEvent();
        final var expectedDraft = mock(Utkast.class);
        final var notificationMessage = mock(NotificationMessage.class);

        setupMockToReturnDraft(expectedDraft);
        setupMockToReturnNotificationMessage(notificationMessage);

        notificationRedeliveryStatusUpdateCreatorService.getCertificateStatusUpdateXml(notificationRedelivery, event);

        verify(expectedDraft, times(1)).getModel();
    }

    @Test
    public void shallCreateStatusUpdateFromCertificateIfRedeliveryMessageIsMissing() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var event = createEvent();
        final var expectedCertificate = mock(IntygContentHolder.class);
        final var notificationMessage = mock(NotificationMessage.class);

        setupMockToReturnDraft(null);
        setupMockToReturnIntygHolder(expectedCertificate);
        setupMockToReturnNotificationMessage(notificationMessage);

        notificationRedeliveryStatusUpdateCreatorService.getCertificateStatusUpdateXml(notificationRedelivery, event);

        verify(expectedCertificate, times(1)).getContents();
    }

    @Test
    public void shallCreateStatusUpdateWithoutDraftOrCertificateIfRedeliveryMessageIsMissingAndEventIsRadera() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var expectedEvent = createEvent();
        expectedEvent.setCode(HandelsekodEnum.RADERA);

        doReturn(mock(List.class)).when(hsaPersonService).getHsaPersonInfo(expectedEvent.getCertificateIssuer());

        notificationRedeliveryStatusUpdateCreatorService.getCertificateStatusUpdateXml(notificationRedelivery, expectedEvent);

        verify(hsaOrganizationsService, times(1)).getVardgivareInfo(expectedEvent.getVardgivarId());
        verify(hsaOrganizationsService, times(1)).getVardenhet(expectedEvent.getEnhetsId());
        verify(hsaPersonService, times(1)).getHsaPersonInfo(expectedEvent.getCertificateIssuer());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        return createNotificationRedelivery("STATUS_UPDATE_XML".getBytes());
    }

    private NotificationRedelivery createNotificationRedeliveryWithoutMessage() {
        return createNotificationRedelivery(null);
    }

    private NotificationRedelivery createNotificationRedelivery(byte[] message) {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId("CORRELATION_ID");
        notificationRedelivery.setRedeliveryTime(LocalDateTime.now());
        notificationRedelivery.setAttemptedDeliveries(1);
        notificationRedelivery.setRedeliveryStrategy(NotificationRedeliveryStrategyEnum.STANDARD);
        notificationRedelivery.setEventId(1000L);
        notificationRedelivery.setMessage(message);
        return notificationRedelivery;
    }

    private void setupMockToReturnDraft(Utkast draft) {
        doReturn(draft != null ? Optional.of(draft) : Optional.empty()).when(draftRepo).findById(any(String.class));
    }

    private void setupMockToReturnIntygHolder(IntygContentHolder certificate) {
        doReturn(certificate).when(intygService).fetchIntygDataForInternalUse(any(String.class), eq(true));
    }

    private void setupMockToReturnNotificationMessage(NotificationMessage notificationMessage)
        throws ModuleNotFoundException, IOException, ModuleException {
        doReturn(notificationMessage).when(notificationMessageFactory).createNotificationMessage(any(Handelse.class),
            nullable(String.class));
    }

    private void setupMockToReturnStatusUpdateXml(String statusUpdateXml) throws Exception {
        doReturn(statusUpdateXml).when(objectMapper).readValue(any(byte[].class), eq(String.class));
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(1000L);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
        event.setEnhetsId("UNIT_ID");
        event.setCode(HandelsekodEnum.NYFRFM);
        event.setIntygsId("CERTIFICATE_ID");
        event.setVardgivarId("CAREPROVIDER_ID");
        event.setTimestamp(EVENT_TIMESTAMP);
        event.setHanteratAv("HANDLED_BY");
        event.setPersonnummer("PERSON_NUMBER");
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setCertificateIssuer("CERTIFICATE_ISSUER");
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setAmne(ArendeAmne.AVSTMN);
        event.setSistaDatumForSvar(LAST_DAY_FOR_ANSWER);
        return event;
    }
}
