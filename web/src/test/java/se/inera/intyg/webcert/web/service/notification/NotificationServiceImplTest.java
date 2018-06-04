/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolutionException;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.mail.MailNotification;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.referens.ReferensServiceImpl;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    private static final String INTYG_TYP_FK = "fk7263";
    private static final String INTYG_ID = "1234";
    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";
    private static final String LOGISK_ADDR = "SE12345678-1234";
    private static final String ENHET_ID = "enhetId";
    private static final String ENHET_NAMN = "enhetName";
    private static final String SIGNED_BY_HSA_ID = "signedByHsaId";
    private static final String ARENDE_ID = "arendeId";

    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("19121212-1212").get();

    private static final Long FRAGASVAR_ID = 1L;

    private static final String USER_REFERENCE = "some-ref";

    @Mock
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Mock
    private MailNotificationService mailNotificationService;

    @Mock
    private Session session;

    @Mock
    private JmsTemplate template;

    @Mock
    private SendNotificationStrategy mockSendNotificationStrategy;

    @Mock
    private NotificationMessageFactory mockNotificationMessageFactory;

    @Mock
    private MonitoringLogService mockMonitoringLogService;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private UtkastRepository utkastRepo;

    @Mock
    private ReferensServiceImpl referensService;

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Before
    public void setup() throws Exception {
        setupMocks(SchemaVersion.VERSION_3);

        when(session.createTextMessage(anyString())).thenAnswer(invocation -> createTextMessage((String) invocation.getArguments()[0]));
        when(referensService.getReferensForIntygsId(any(String.class))).thenReturn(USER_REFERENCE);
    }

    private void setupMocks(SchemaVersion schemaVersion) {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(schemaVersion));

        when(mockNotificationMessageFactory
                .createNotificationMessage(any(Utkast.class), any(HandelsekodEnum.class), eq(schemaVersion), anyString(),
                        or(isNull(), any(Amneskod.class)), or(isNull(), any(LocalDate.class))))
                                .thenAnswer(invocation -> createNotificationMessage(((HandelsekodEnum) invocation.getArguments()[1]),
                                        INTYG_JSON));
    }

    @Test
    public void testCreateAndSendNotification() throws Exception {

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_1));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.ANDRAT, INTYG_JSON);

        when(mockNotificationMessageFactory.createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1),
                anyString(),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)))).thenReturn(notMsg);

        Utkast utkast = createUtkast();
        notificationService.createAndSendNotification(utkast, HandelsekodEnum.ANDRAT);

        verify(template, only()).send(messageCreatorCaptor.capture());

        TextMessage textMessage = mock(TextMessage.class);
        Session session = mock(Session.class);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createTextMessage(stringArgumentCaptor.capture())).thenReturn(textMessage);

        MessageCreator messageCreator = messageCreatorCaptor.getValue();
        messageCreator.createMessage(session);

        // get the notfication message as json and transform it back to object
        NotificationMessage captNotMsg = objectMapper.readValue(stringArgumentCaptor.getValue(), NotificationMessage.class);

        // assert that things are still there
        assertNotNull(captNotMsg);
        assertEquals(INTYG_ID, captNotMsg.getIntygsId());
        assertEquals(HandelsekodEnum.ANDRAT, captNotMsg.getHandelse());
        assertEquals(INTYG_JSON, captNotMsg.getUtkast());
        assertEquals(SchemaVersion.VERSION_1, captNotMsg.getVersion());
        assertNull(captNotMsg.getReference());

        // verify call has been made
        verify(mockNotificationMessageFactory).createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1),
                anyString(),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)));

        verify(handelseRepository).save(any(Handelse.class));
    }

    @Test
    public void testCreateAndSendNotificationWithReference() throws Exception {
        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_1));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.ANDRAT, INTYG_JSON);
        notMsg.setReference(USER_REFERENCE);

        when(mockNotificationMessageFactory.createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1),
                eq(USER_REFERENCE),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)))).thenReturn(notMsg);

        Utkast utkast = createUtkast();
        notificationService.createAndSendNotification(utkast, HandelsekodEnum.ANDRAT);

        verify(template, only()).send(messageCreatorCaptor.capture());

        TextMessage textMessage = mock(TextMessage.class);
        Session session = mock(Session.class);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createTextMessage(stringArgumentCaptor.capture())).thenReturn(textMessage);

        MessageCreator messageCreator = messageCreatorCaptor.getValue();
        messageCreator.createMessage(session);

        // get the notfication message as json and transform it back to object
        NotificationMessage captNotMsg = objectMapper.readValue(stringArgumentCaptor.getValue(), NotificationMessage.class);

        // assert that things are still there
        assertNotNull(captNotMsg);
        assertEquals(INTYG_ID, captNotMsg.getIntygsId());
        assertEquals(HandelsekodEnum.ANDRAT, captNotMsg.getHandelse());
        assertEquals(INTYG_JSON, captNotMsg.getUtkast());
        assertEquals(SchemaVersion.VERSION_1, captNotMsg.getVersion());
        assertEquals(USER_REFERENCE, captNotMsg.getReference());

        // verify call has been made
        verify(mockNotificationMessageFactory).createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1),
                eq(USER_REFERENCE),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)));
    }

    @Test
    public void testIntygsutkastCreated() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.SKAPAT, INTYG_JSON);
        notMsg.setReference(USER_REFERENCE);

        when(mockNotificationMessageFactory.createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.SKAPAT),
                eq(SchemaVersion.VERSION_3),
                eq(USER_REFERENCE),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)))).thenReturn(notMsg);

        notificationService.sendNotificationForDraftCreated(createUtkast());

        // verify call has been made
        verify(mockNotificationMessageFactory).createNotificationMessage(
                any(Utkast.class),
                eq(HandelsekodEnum.SKAPAT),
                eq(SchemaVersion.VERSION_3),
                eq(USER_REFERENCE),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)));
    }

    @Test
    public void testDraftCreated() throws Exception {
        notificationService.sendNotificationForDraftCreated(createUtkast());
        verifySuccessfulInvocations(HandelsekodEnum.SKAPAT);
    }

    @Test(expected = JmsException.class)
    public void testDraftCreatedJmsException() throws Exception {
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForDraftCreated(createUtkast());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testDraftSigned() throws Exception {
        notificationService.sendNotificationForDraftSigned(createUtkast());
        verifySuccessfulInvocations(HandelsekodEnum.SIGNAT);
    }

    @Test(expected = JmsException.class)
    public void testDraftSignedJmsException() throws Exception {
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForDraftSigned(createUtkast());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testDraftChanged() throws Exception {
        notificationService.sendNotificationForDraftChanged(createUtkast());
        verifySuccessfulInvocations(HandelsekodEnum.ANDRAT);
    }

    @Test(expected = JmsException.class)
    public void testDraftChangedJmsException() throws Exception {
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForDraftChanged(createUtkast());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testDraftDeleted() throws Exception {
        notificationService.sendNotificationForDraftDeleted(createUtkast());
        verifySuccessfulInvocations(HandelsekodEnum.RADERA);
    }

    @Test(expected = JmsException.class)
    public void testDraftDeletedJmsException() throws Exception {
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForDraftDeleted(createUtkast());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testIntygSent() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForIntygSent(INTYG_ID);
        verifySuccessfulInvocations(HandelsekodEnum.SKICKA);
    }

    @Test(expected = JmsException.class)
    public void testIntygSentJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForIntygSent(INTYG_ID);
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testIntygRevoked() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForIntygRevoked(INTYG_ID);
        verifySuccessfulInvocations(HandelsekodEnum.MAKULE);
    }

    @Test(expected = JmsException.class)
    public void testIntygRevokedJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForIntygRevoked(INTYG_ID);
        } finally {
            // verify(template).send(any(MessageCreator.class));
            // verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionReceivedFragaSvarIntegreradEnhet() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionReceived(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionReceivedFragaSvarIntegreradEnhetJmsException() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionReceived(createFragaSvar());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionReceivedFragaSvarSendsMail() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(false);
        notificationService.sendNotificationForQuestionReceived(createFragaSvar());

        ArgumentCaptor<MailNotification> mailNotificationCaptor = ArgumentCaptor.forClass(MailNotification.class);
        verify(mailNotificationService).sendMailForIncomingQuestion(mailNotificationCaptor.capture());
        assertEquals(ENHET_ID, mailNotificationCaptor.getValue().getCareUnitId());
        assertEquals(FRAGASVAR_ID.toString(), mailNotificationCaptor.getValue().getQaId());
        assertEquals(INTYG_ID, mailNotificationCaptor.getValue().getCertificateId());
        assertEquals(INTYG_TYP_FK, mailNotificationCaptor.getValue().getCertificateType());
        assertEquals(ENHET_NAMN, mailNotificationCaptor.getValue().getCareUnitName());
        assertEquals(SIGNED_BY_HSA_ID, mailNotificationCaptor.getValue().getSignedByHsaId());
        // no jms notifications triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test
    public void testAnswerRecievedFragaSvarIntegreradEnhet() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForAnswerRecieved(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM);
    }

    @Test(expected = JmsException.class)
    public void testAnswerRecievedFragaSvarIntegreradEnhetJmsException() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForAnswerRecieved(createFragaSvar());
        } finally {
            // verify(template).send(any(MessageCreator.class));
            // verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testAnswerRecievedFragaSvarSendsMail() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(false);
        notificationService.sendNotificationForAnswerRecieved(createFragaSvar());

        ArgumentCaptor<MailNotification> mailNotificationCaptor = ArgumentCaptor.forClass(MailNotification.class);
        verify(mailNotificationService).sendMailForIncomingAnswer(mailNotificationCaptor.capture());
        assertEquals(ENHET_ID, mailNotificationCaptor.getValue().getCareUnitId());
        assertEquals(FRAGASVAR_ID.toString(), mailNotificationCaptor.getValue().getQaId());
        assertEquals(INTYG_ID, mailNotificationCaptor.getValue().getCertificateId());
        assertEquals(INTYG_TYP_FK, mailNotificationCaptor.getValue().getCertificateType());
        assertEquals(ENHET_NAMN, mailNotificationCaptor.getValue().getCareUnitName());
        assertEquals(SIGNED_BY_HSA_ID, mailNotificationCaptor.getValue().getSignedByHsaId());
        // no jms notifications triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test
    public void testQuestionReceivedArendeIntegreradEnhet() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionReceived(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionReceivedArendeIntegreradEnhetJmsException() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionReceived(createArende());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionReceivedArendeSendsMail() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(false);
        notificationService.sendNotificationForQuestionReceived(createArende());

        ArgumentCaptor<MailNotification> mailNotificationCaptor = ArgumentCaptor.forClass(MailNotification.class);
        verify(mailNotificationService).sendMailForIncomingQuestion(mailNotificationCaptor.capture());
        assertEquals(ENHET_ID, mailNotificationCaptor.getValue().getCareUnitId());
        assertEquals(ARENDE_ID, mailNotificationCaptor.getValue().getQaId());
        assertEquals(INTYG_ID, mailNotificationCaptor.getValue().getCertificateId());
        assertEquals(INTYG_TYP_FK, mailNotificationCaptor.getValue().getCertificateType());
        assertEquals(ENHET_NAMN, mailNotificationCaptor.getValue().getCareUnitName());
        assertEquals(SIGNED_BY_HSA_ID, mailNotificationCaptor.getValue().getSignedByHsaId());
        // no jms notifications triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test
    public void testAnswerRecievedArendeIntegreradEnhet() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForAnswerRecieved(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM);
    }

    @Test(expected = JmsException.class)
    public void testAnswerRecievedArendeIntegreradEnhetJmsException() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(true);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForAnswerRecieved(createArende());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testAnswerRecievedArendeSendsMail() throws Exception {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(ENHET_ID, INTYG_TYP_FK)).thenReturn(false);
        notificationService.sendNotificationForAnswerRecieved(createArende());

        ArgumentCaptor<MailNotification> mailNotificationCaptor = ArgumentCaptor.forClass(MailNotification.class);
        verify(mailNotificationService).sendMailForIncomingAnswer(mailNotificationCaptor.capture());
        assertEquals(ENHET_ID, mailNotificationCaptor.getValue().getCareUnitId());
        assertEquals(ARENDE_ID, mailNotificationCaptor.getValue().getQaId());
        assertEquals(INTYG_ID, mailNotificationCaptor.getValue().getCertificateId());
        assertEquals(INTYG_TYP_FK, mailNotificationCaptor.getValue().getCertificateType());
        assertEquals(ENHET_NAMN, mailNotificationCaptor.getValue().getCareUnitName());
        assertEquals(SIGNED_BY_HSA_ID, mailNotificationCaptor.getValue().getSignedByHsaId());
        // no jms notifications triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test(expected = JmsException.class)
    public void testSendNotificationForQAsJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testSendNotificationForQAsQuestionReceivedSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_RECIPIENT);
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsQuestionHandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsQuestionUnhandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsQuestionSentSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_CARE);
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFV, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsQuestionFromCareHandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);

        // no notification triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test
    public void testSendNotificationForQAsQuestionFromCareUnhandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);

        // no notification triggered
        verifyZeroInteractions(mockNotificationMessageFactory);
        verifyZeroInteractions(template);
        verifyZeroInteractions(mockMonitoringLogService);
    }

    @Test
    public void testSendNotificationForQAsAnswerReceivedSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsAnswerHandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsAnswerUnhandledSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsAnswerSentSchemaVersion1() throws Exception {
        setupMocks(SchemaVersion.VERSION_1);
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM, SchemaVersion.VERSION_1);
    }

    @Test
    public void testSendNotificationForQAsQuestionReceivedSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_RECIPIENT);
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM);
    }

    @Test
    public void testSendNotificationForQAsQuestionHandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM);
    }

    @Test
    public void testSendNotificationForQAsQuestionUnhandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM);
    }

    @Test
    public void testSendNotificationForQAsQuestionSentSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_CARE);
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFV);
    }

    @Test
    public void testSendNotificationForQAsQuestionFromCareHandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test
    public void testSendNotificationForQAsQuestionFromCareUnhandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test
    public void testSendNotificationForQAsAnswerReceivedSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM);
    }

    @Test
    public void testSendNotificationForQAsAnswerHandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test
    public void testSendNotificationForQAsAnswerUnhandledSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test
    public void testSendNotificationForQAsAnswerSentSchemaVersion2() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_3));
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM);
    }

    private void verifySuccessfulInvocations(HandelsekodEnum kod) throws Exception {
        verifySuccessfulInvocations(kod, SchemaVersion.VERSION_3);
    }

    private void verifySuccessfulInvocations(HandelsekodEnum kod, SchemaVersion schemaVersion) throws Exception {
        verify(mockNotificationMessageFactory).createNotificationMessage(
                any(Utkast.class),
                eq(kod),
                eq(schemaVersion),
                anyString(),
                or(isNull(), any(Amneskod.class)),
                or(isNull(), any(LocalDate.class)));

        ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(template).send(messageCaptor.capture());

        Message res = messageCaptor.getValue().createMessage(session);
        assertEquals(INTYG_ID, res.getStringProperty(NotificationRouteHeaders.INTYGS_ID));
        assertEquals(INTYG_TYP_FK, res.getStringProperty(NotificationRouteHeaders.INTYGS_TYP));
        assertEquals(kod.value(), res.getStringProperty(NotificationRouteHeaders.HANDELSE));
        assertNotNull(((TextMessage) res).getText());

        NotificationMessage nm = objectMapper.readValue(((TextMessage) res).getText(), NotificationMessage.class);
        assertEquals(INTYG_JSON, nm.getUtkast());

        verify(mockMonitoringLogService).logNotificationSent(kod.value(), ENHET_ID);
        verify(handelseRepository).save(any(Handelse.class));
    }

    private NotificationMessage createNotificationMessage(HandelsekodEnum handelse, String utkastJson) {
        FragorOchSvar fs = FragorOchSvar.getEmpty();
        LocalDateTime time = LocalDateTime.of(2001, 12, 31, 12, 34, 56, 789);

        NotificationMessage notMsg = new NotificationMessage(INTYG_ID, INTYG_TYP_FK, time, handelse, LOGISK_ADDR,
                utkastJson, fs, null, null, SchemaVersion.VERSION_1, null);

        return notMsg;
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setEnhetsId(ENHET_ID);
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel(INTYG_JSON);
        utkast.setPatientPersonnummer(PATIENT_ID);
        return utkast;
    }

    private TextMessage createTextMessage(String s) throws JMSException {
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setText(s);
        return message;
    }

    private FragaSvar createFragaSvar() {
        FragaSvar fs = new FragaSvar();
        fs.setAmne(Amne.OVRIGT);
        fs.setSistaDatumForSvar(LocalDate.of(2018, 12, 12));
        fs.setInternReferens(FRAGASVAR_ID);
        fs.setIntygsReferens(new IntygsReferens());
        fs.getIntygsReferens().setIntygsId(INTYG_ID);
        fs.getIntygsReferens().setIntygsTyp(INTYG_TYP_FK);
        fs.setVardperson(new Vardperson());
        fs.getVardperson().setEnhetsId(ENHET_ID);
        fs.getVardperson().setEnhetsnamn(ENHET_NAMN);
        fs.getVardperson().setHsaId(SIGNED_BY_HSA_ID);
        return fs;
    }

    private Arende createArende() {
        Arende arende = new Arende();
        arende.setMeddelandeId(ARENDE_ID);
        arende.setIntygsId(INTYG_ID);
        arende.setEnhetId(ENHET_ID);
        arende.setEnhetName(ENHET_NAMN);
        arende.setSigneratAv(SIGNED_BY_HSA_ID);
        arende.setIntygTyp(INTYG_TYP_FK);
        return arende;
    }

}
