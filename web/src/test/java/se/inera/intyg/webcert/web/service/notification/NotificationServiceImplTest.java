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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.*;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
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

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

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

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService = new NotificationServiceImpl();

    @Test
    public void serviceNotifiesThereIsAChangedCertificateDraft() throws Exception {

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(mockSendNotificationStrategy.decideNotificationForIntyg(any(Utkast.class))).thenReturn(Optional.of(NotificationVersion.VERSION_1));

        NotificationMessage notMsg = createNotificationMessage(HandelseType.INTYGSUTKAST_ANDRAT, INTYG_JSON);
        when(mockNotificationMessageFactory.createNotificationMessage(any(Utkast.class), eq(HandelseType.INTYGSUTKAST_ANDRAT),
                eq(NotificationVersion.VERSION_1))).thenReturn(notMsg);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);

        Utkast utkast = createUtkast();
        notificationService.createAndSendNotification(utkast, HandelseType.INTYGSUTKAST_ANDRAT);

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
        assertEquals(HandelseType.INTYGSUTKAST_ANDRAT, captNotMsg.getHandelse());
        assertEquals(INTYG_JSON, captNotMsg.getUtkast());
        assertEquals(NotificationVersion.VERSION_1, captNotMsg.getVersion());
    }

    private NotificationMessage createNotificationMessage(HandelseType handelse, String utkastJson) {
        FragorOchSvar fs = FragorOchSvar.getEmpty();
        LocalDateTime time = new LocalDateTime(2001, 12, 31, 12, 34, 56, 789);
        NotificationMessage notMsg = new NotificationMessage(INTYG_ID, INTYG_TYP_FK, time, handelse, LOGISK_ADDR, utkastJson, fs,
                NotificationVersion.VERSION_1);
        return notMsg;
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel(INTYG_JSON);
        return utkast;
    }

}
