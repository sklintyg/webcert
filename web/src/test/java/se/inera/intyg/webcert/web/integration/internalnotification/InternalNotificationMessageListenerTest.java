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
package se.inera.intyg.webcert.web.integration.internalnotification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CARE_UNIT_ID;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_ID;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_TYPE;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_TYPE_VERSION;

@RunWith(MockitoJUnitRunner.class)
public class InternalNotificationMessageListenerTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InternalNotificationMessageListener testee = new InternalNotificationMessageListener();

    @Before
    public void init() throws ModuleException, ModuleNotFoundException {
        ReflectionTestUtils.setField(testee, "logicalAddress", "logisk-adress");

        Utlatande utlatande = mock(Utlatande.class);
        when(utlatande.getId()).thenReturn("intyg-1");
        when(utlatande.getTyp()).thenReturn("lijsp");

        CertificateResponse certificateResponse = mock(CertificateResponse.class);
        when(certificateResponse.getUtlatande()).thenReturn(utlatande);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getCertificate(anyString(), anyString(), anyString())).thenReturn(certificateResponse);

        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
    }

    @Test
    public void testOk() {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(anyString(), anyString())).thenReturn(true);
        testee.onMessage(createMessage());
        verify(notificationService, times(1))
                .forwardInternalNotification(anyString(), anyString(), any(Utlatande.class), eq(HandelsekodEnum.SKICKA));
    }

    @Test
    public void testMissingParameter() throws JMSException {
        Message textMessage = createMessage();
        when(textMessage.getStringProperty(CERTIFICATE_ID)).thenReturn(null);
        testee.onMessage(textMessage);

        verifyZeroInteractions(notificationService);
    }

    @Test
    public void testDoesNotSendWhenNotDjupintegreradVE() {
        when(integreradeEnheterRegistry.isEnhetIntegrerad(anyString(), anyString())).thenReturn(false);

        testee.onMessage(createMessage());
        verifyZeroInteractions(intygModuleRegistry);
        verifyZeroInteractions(notificationService);
    }

    private Message createMessage() {
        try {
            TextMessage tm = mock(TextMessage.class);
            when(tm.getStringProperty(CERTIFICATE_ID)).thenReturn("intyg-1");
            when(tm.getStringProperty(CERTIFICATE_TYPE)).thenReturn("lisjp");
            when(tm.getStringProperty(CERTIFICATE_TYPE_VERSION)).thenReturn("1.0");
            when(tm.getStringProperty(CARE_UNIT_ID)).thenReturn("enhet-1");
            return tm;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
