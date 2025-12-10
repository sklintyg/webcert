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
package se.inera.intyg.webcert.web.integration.internalnotification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CARE_UNIT_ID;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_ID;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_TYPE;
import static se.inera.intyg.webcert.web.integration.internalnotification.InternalNotificationMessageListener.CERTIFICATE_TYPE_VERSION;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
public class InternalNotificationMessageListenerTest {

    private static final String INTYG_1 = "intyg-1";
    private static final String LIJSP = "lijsp";
    @Mock
    private CSIntegrationService csIntegrationService;

    @Mock
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InternalNotificationMessageListener testee = new InternalNotificationMessageListener();

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(testee, "logicalAddress", "logisk-adress");
    }

    @Nested
    class CertificateFromWC {


        @Test
        void testOk() throws ModuleException, ModuleNotFoundException {
            Utlatande utlatande = mock(Utlatande.class);
            when(utlatande.getId()).thenReturn(INTYG_1);
            when(utlatande.getTyp()).thenReturn(LIJSP);
            CertificateResponse certificateResponse = mock(CertificateResponse.class);
            when(certificateResponse.getUtlatande()).thenReturn(utlatande);

            ModuleApi moduleApi = mock(ModuleApi.class);
            when(moduleApi.getCertificate(anyString(), anyString(), anyString())).thenReturn(certificateResponse);

            when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
            when(integreradeEnheterRegistry.isEnhetIntegrerad(anyString(), anyString())).thenReturn(true);
            testee.onMessage(createMessage());
            verify(notificationService, times(1))
                .forwardInternalNotification(anyString(), anyString(), any(Utlatande.class), eq(HandelsekodEnum.SKICKA));
        }

        @Test
        void testMissingParameter() throws JMSException {
            Message textMessage = createMessage();
            when(textMessage.getStringProperty(CERTIFICATE_ID)).thenReturn(null);
            testee.onMessage(textMessage);

            verifyNoInteractions(notificationService);
        }

        @Test
        void testDoesNotSendWhenNotDjupintegreradVE() throws ModuleException {
            when(integreradeEnheterRegistry.isEnhetIntegrerad(anyString(), anyString())).thenReturn(false);

            testee.onMessage(createMessage());
            verifyNoInteractions(intygModuleRegistry);
            verifyNoInteractions(notificationService);
        }
    }

    @Nested
    class CertificateFromCS {

        @Test
        void shallNotPublishStatusUpdateIfCertificateDontExistInCertificateService()
            throws ModuleException, ModuleNotFoundException {
            Utlatande utlatande = mock(Utlatande.class);
            ModuleApi moduleApi = mock(ModuleApi.class);
            CertificateResponse certificateResponse = mock(CertificateResponse.class);

            doReturn(true).when(integreradeEnheterRegistry).isEnhetIntegrerad(anyString(), anyString());
            doReturn(false).when(csIntegrationService).certificateExists(INTYG_1);

            doReturn(INTYG_1).when(utlatande).getId();
            doReturn(LIJSP).when(utlatande).getTyp();
            doReturn(utlatande).when(certificateResponse).getUtlatande();
            doReturn(certificateResponse).when(moduleApi).getCertificate(anyString(), anyString(), anyString());
            doReturn(moduleApi).when(intygModuleRegistry).getModuleApi(anyString(), anyString());

            testee.onMessage(createMessage());
            verifyNoInteractions(publishCertificateStatusUpdateService);
        }

        @Test
        void shallPublishStatusUpdateIfCertificateExistInCertificateService() {
            final var certificate = new Certificate();
            doReturn(true).when(integreradeEnheterRegistry).isEnhetIntegrerad(anyString(), anyString());
            doReturn(true).when(csIntegrationService).certificateExists(INTYG_1);
            doReturn(certificate).when(csIntegrationService).getInternalCertificate(INTYG_1);

            testee.onMessage(createMessage());
            verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.SKICKA);
        }
    }


    private Message createMessage() {
        try {
            TextMessage tm = mock(TextMessage.class);
            when(tm.getStringProperty(CERTIFICATE_ID)).thenReturn(INTYG_1);
            when(tm.getStringProperty(CERTIFICATE_TYPE)).thenReturn("lisjp");
            when(tm.getStringProperty(CERTIFICATE_TYPE_VERSION)).thenReturn("1.0");
            when(tm.getStringProperty(CARE_UNIT_ID)).thenReturn("enhet-1");
            return tm;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
