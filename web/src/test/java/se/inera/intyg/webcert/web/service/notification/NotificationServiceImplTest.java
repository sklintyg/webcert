/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.jms.*;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolutionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.*;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

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
    private UtkastRepository utkastRepo;

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Before
    public void setup() throws Exception {
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_2));

        when(mockNotificationMessageFactory.createNotificationMessage(any(Utkast.class), any(HandelsekodEnum.class),
                eq(SchemaVersion.VERSION_2), eq(null))).thenAnswer(invocation -> createNotificationMessage(((HandelsekodEnum) invocation.getArguments()[1]), INTYG_JSON));

        when(session.createTextMessage(anyString())).thenAnswer(invocation -> createTextMessage((String) invocation.getArguments()[0]));
    }

    @Test
    public void testCreateAndSendNotification() throws Exception {

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_1));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.ANDRAT, INTYG_JSON);
        when(mockNotificationMessageFactory.createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1), eq(null))).thenReturn(notMsg);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);

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
        verify(mockNotificationMessageFactory).createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1), eq(null));
    }

    @Test
    public void testCreateAndSendNotificationWithReference() throws Exception {
        final String ref = "reference";

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_1));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.ANDRAT, INTYG_JSON);
        notMsg.setReference(ref);
        when(mockNotificationMessageFactory.createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1), eq(ref))).thenReturn(notMsg);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);

        Utkast utkast = createUtkast();
        notificationService.createAndSendNotification(utkast, HandelsekodEnum.ANDRAT, ref);

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
        assertEquals(ref, captNotMsg.getReference());
        verify(mockNotificationMessageFactory).createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.ANDRAT),
                eq(SchemaVersion.VERSION_1), eq(ref));
    }

    @Test
    public void testIntygsutkastCreated() throws Exception {
        final String ref = "reference";
        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(SchemaVersion.VERSION_2));

        NotificationMessage notMsg = createNotificationMessage(HandelsekodEnum.SKAPAT, INTYG_JSON);
        notMsg.setReference(ref);
        when(mockNotificationMessageFactory.createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.SKAPAT),
                eq(SchemaVersion.VERSION_2), eq(ref))).thenReturn(notMsg);

        notificationService.sendNotificationForDraftCreated(createUtkast(), ref);
        verify(mockNotificationMessageFactory).createNotificationMessage(any(Utkast.class), eq(HandelsekodEnum.SKAPAT),
                eq(SchemaVersion.VERSION_2), eq(ref));
    }

    @Test
    public void testDraftCreated() throws Exception {
        notificationService.sendNotificationForDraftCreated(createUtkast(), null);
        verifySuccessfulInvocations(HandelsekodEnum.SKAPAT);
    }

    @Test(expected = JmsException.class)
    public void testDraftCreatedJmsException() throws Exception {
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForDraftCreated(createUtkast(), null);
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
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionReceivedFragaSvar() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionReceived(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionReceivedFragaSvarJmsException() throws Exception {
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
    public void testQuestionHandledFragaSvar() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionHandled(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionHandledFragaSvarJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionHandled(createFragaSvar());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionSentFragaSvar() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionSent(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFV);
    }

    @Test(expected = JmsException.class)
    public void testQuestionSentFragaSvarJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionSent(createFragaSvar());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testAnswerRecievedFragaSvar() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForAnswerRecieved(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM);
    }

    @Test(expected = JmsException.class)
    public void testAnswerRecievedFragaSvarJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForAnswerRecieved(createFragaSvar());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testAnswerHandledFragaSvar() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForAnswerHandled(createFragaSvar());
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test(expected = JmsException.class)
    public void testAnswerHandledFragaSvarJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForAnswerHandled(createFragaSvar());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionReceivedArende() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionReceived(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionReceivedArendeJmsException() throws Exception {
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
    public void testQuestionHandledArende() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionHandled(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFM);
    }

    @Test(expected = JmsException.class)
    public void testQuestionHandledArendeJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionHandled(createArende());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testQuestionSentArende() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionSent(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.NYFRFV);
    }

    @Test(expected = JmsException.class)
    public void testQuestionSentJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionSent(createArende());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    @Test
    public void testAnswerRecievedArende() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForAnswerRecieved(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.NYSVFM);
    }

    @Test(expected = JmsException.class)
    public void testAnswerRecievedArendeJmsException() throws Exception {
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
    public void testAnswerHandledArende() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        notificationService.sendNotificationForQuestionToRecipientHandled(createArende());
        verifySuccessfulInvocations(HandelsekodEnum.HANFRFV);
    }

    @Test(expected = JmsException.class)
    public void testAnswerHandledArendeJmsException() throws Exception {
        when(utkastRepo.findOne(INTYG_ID)).thenReturn(createUtkast());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));
        try {
            notificationService.sendNotificationForQuestionReceived(createArende());
        } finally {
            verify(template).send(any(MessageCreator.class));
            verifyZeroInteractions(mockMonitoringLogService);
        }
    }

    private void verifySuccessfulInvocations(HandelsekodEnum kod) throws Exception {
        verify(mockNotificationMessageFactory).createNotificationMessage(any(Utkast.class), eq(kod),
                eq(SchemaVersion.VERSION_2), eq(null));

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
    }

    private NotificationMessage createNotificationMessage(HandelsekodEnum handelse, String utkastJson) {
        FragorOchSvar fs = FragorOchSvar.getEmpty();
        LocalDateTime time = LocalDateTime.of(2001, 12, 31, 12, 34, 56, 789);
        NotificationMessage notMsg = new NotificationMessage(INTYG_ID, INTYG_TYP_FK, time, handelse, LOGISK_ADDR, utkastJson, fs,
                null, null, SchemaVersion.VERSION_1, null);
        return notMsg;
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setEnhetsId(ENHET_ID);
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel(INTYG_JSON);
        return utkast;
    }

    private TextMessage createTextMessage(String s) throws JMSException {
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setText(s);
        return message;
    }

    private FragaSvar createFragaSvar() {
        FragaSvar fs = new FragaSvar();
        fs.setIntygsReferens(new IntygsReferens());
        fs.getIntygsReferens().setIntygsId(INTYG_ID);
        return fs;
    }

    private Arende createArende() {
        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);
        return arende;
    }
}
