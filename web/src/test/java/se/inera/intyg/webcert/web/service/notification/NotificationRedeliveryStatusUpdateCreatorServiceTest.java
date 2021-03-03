package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

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
    public void shallUseCertificateFromNotificationRedeliveryMessageIfExists() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedCertificate = mock(Intyg.class);

        setupMockToReturnNotificationRedeliveryMessage(expectedCertificate);

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertEquals(expectedCertificate, actualStatusUpdate.getIntyg());
    }

    @Test
    public void shallUseRefFromNotificationRedeliveryMessage() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedRef = "REF";

        setupMockToReturnNotificationRedeliveryMessage(mock(Intyg.class));

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertEquals(expectedRef, actualStatusUpdate.getRef());
    }

    @Test
    public void shallUseMessageSentFromNotificationRedeliveryMessage() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedMessagesSent = new CertificateMessages();
        expectedMessagesSent.setUnanswered(1);
        expectedMessagesSent.setAnswered(2);
        expectedMessagesSent.setHandled(3);
        expectedMessagesSent.setTotal(4);

        setupMockToReturnNotificationRedeliveryMessage(mock(Intyg.class), expectedMessagesSent, null);

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertNotNull(actualStatusUpdate.getSkickadeFragor());
        assertEquals(expectedMessagesSent.getUnanswered(), actualStatusUpdate.getSkickadeFragor().getEjBesvarade());
        assertEquals(expectedMessagesSent.getAnswered(), actualStatusUpdate.getSkickadeFragor().getBesvarade());
        assertEquals(expectedMessagesSent.getHandled(), actualStatusUpdate.getSkickadeFragor().getHanterade());
        assertEquals(expectedMessagesSent.getTotal(), actualStatusUpdate.getSkickadeFragor().getTotalt());
    }

    @Test
    public void shallUseMessageReceivedFromNotificationRedeliveryMessage() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedMessagesReceived = new CertificateMessages();
        expectedMessagesReceived.setUnanswered(1);
        expectedMessagesReceived.setAnswered(2);
        expectedMessagesReceived.setHandled(3);
        expectedMessagesReceived.setTotal(4);

        setupMockToReturnNotificationRedeliveryMessage(mock(Intyg.class), null, expectedMessagesReceived);

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertNotNull(actualStatusUpdate.getSkickadeFragor());
        assertEquals(expectedMessagesReceived.getUnanswered(), actualStatusUpdate.getMottagnaFragor().getEjBesvarade());
        assertEquals(expectedMessagesReceived.getAnswered(), actualStatusUpdate.getMottagnaFragor().getBesvarade());
        assertEquals(expectedMessagesReceived.getHandled(), actualStatusUpdate.getMottagnaFragor().getHanterade());
        assertEquals(expectedMessagesReceived.getTotal(), actualStatusUpdate.getMottagnaFragor().getTotalt());
    }

    @Test
    public void shallUseHandledByFromEventIfExists() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedHandledBy = "HANDLED_BY";

        setupMockToReturnNotificationRedeliveryMessage(mock(Intyg.class));

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertEquals(expectedHandledBy, actualStatusUpdate.getHanteratAv().getExtension());
    }

    @Test
    public void shallUseEventInformationFromEventIfExists() throws Exception {
        final var notificationRedelivery = createNotificationRedelivery();
        final var event = createEvent();
        final var expectedEvent = createEvent();

        setupMockToReturnNotificationRedeliveryMessage(mock(Intyg.class));

        final var actualStatusUpdate = notificationRedeliveryStatusUpdateCreatorService
            .createCertificateStatusUpdate(notificationRedelivery, event);

        assertNotNull(actualStatusUpdate);
        assertEquals(expectedEvent.getCode().value(), actualStatusUpdate.getHandelse().getHandelsekod().getCode());
        assertEquals(EVENT_TIMESTAMP, actualStatusUpdate.getHandelse().getTidpunkt());
        assertEquals(expectedEvent.getAmne().name(), actualStatusUpdate.getHandelse().getAmne().getCode());
        assertEquals(LAST_DAY_FOR_ANSWER, actualStatusUpdate.getHandelse().getSistaDatumForSvar());
    }

    @Test
    public void shallCreateStatusUpdateFromDraftIfRedeliveryMessageIsMissing() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var event = createEvent();
        final var expectedDraft = mock(Utkast.class);

        setupMockToReturnDraft(expectedDraft);

        notificationRedeliveryStatusUpdateCreatorService.createCertificateStatusUpdate(notificationRedelivery, event);

        verify(expectedDraft, times(1)).getModel();
    }

    @Test
    public void shallCreateStatusUpdateFromCertificateIfRedeliveryMessageIsMissing() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var event = createEvent();
        final var expectedCertificate = mock(IntygContentHolder.class);

        setupMockToReturnDraft(null);
        setupMockToReturnIntygHolder(expectedCertificate);

        notificationRedeliveryStatusUpdateCreatorService.createCertificateStatusUpdate(notificationRedelivery, event);

        verify(expectedCertificate, times(1)).getContents();
    }

    @Test
    public void shallCreateStatusUpdateWithoutDraftOrCertificateIfRedeliveryMessageIsMissingAndEventIsRadera() throws Exception {
        final var notificationRedelivery = createNotificationRedeliveryWithoutMessage();
        final var expectedEvent = createEvent();
        expectedEvent.setCode(HandelsekodEnum.RADERA);

        doReturn(mock(List.class)).when(hsaPersonService).getHsaPersonInfo(expectedEvent.getCertificateIssuer());

        notificationRedeliveryStatusUpdateCreatorService.createCertificateStatusUpdate(notificationRedelivery, expectedEvent);

        verify(hsaOrganizationsService, times(1)).getVardgivareInfo(expectedEvent.getVardgivarId());
        verify(hsaOrganizationsService, times(1)).getVardenhet(expectedEvent.getEnhetsId());
        verify(hsaPersonService, times(1)).getHsaPersonInfo(expectedEvent.getCertificateIssuer());
    }

    private NotificationRedelivery createNotificationRedelivery() {
        return createNotificationRedelivery("REDELIVERY_MESSAGE".getBytes());
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

    private void setupMockToReturnNotificationRedeliveryMessage(Intyg certificate) throws Exception {
        setupMockToReturnNotificationRedeliveryMessage(certificate, null, null);
    }

    private void setupMockToReturnNotificationRedeliveryMessage(Intyg certificate, CertificateMessages sent, CertificateMessages received)
        throws Exception {
        final var mockNotificationRedeliveryMessage = mock(NotificationRedeliveryMessage.class);
        doReturn(mockNotificationRedeliveryMessage).when(objectMapper)
            .readValue(any(byte[].class), eq(NotificationRedeliveryMessage.class));
        doReturn(certificate).when(mockNotificationRedeliveryMessage).getCert();
        doReturn("REF").when(mockNotificationRedeliveryMessage).getReference();
        doReturn(sent).when(mockNotificationRedeliveryMessage).getSent();
        doReturn(received).when(mockNotificationRedeliveryMessage).getReceived();
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
