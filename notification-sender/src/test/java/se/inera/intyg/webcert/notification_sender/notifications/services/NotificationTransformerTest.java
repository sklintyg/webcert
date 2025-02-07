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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.HANDELSE;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.VERSION;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import org.apache.camel.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.ObjectFactory;

@ExtendWith(MockitoExtension.class)
class NotificationTransformerTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private FeaturesHelper featuresHelper;
    @Mock
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;
    @Mock
    private NotificationResultMessageCreator notificationResultMessageCreator;
    @Mock
    private NotificationResultMessageSender notificationResultMessageSender;
    @Spy
    private MdcHelper mdcHelper;

    @InjectMocks
    private NotificationTransformer notificationTransformer;

    @Test
    void shallTransformNotificationMessageToCertificateStatusUpdateForCare() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var mockCertificateStatusUpdateForCare = mock(CertificateStatusUpdateForCareType.class);
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);
        doReturn(mockCertificateStatusUpdateForCare).when(certificateStatusUpdateForCareCreator).create(eq(notificationMessage), any());

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setBody(mockCertificateStatusUpdateForCare);
    }

    @Test
    void shallUseProvidedCertificateStatusUpdateForCareIfIncludedInNotificationMessage() throws Exception {
        final var notificationMessage = new NotificationMessage();
        notificationMessage.setHandelse(HandelsekodEnum.SKAPAT);
        notificationMessage.setLogiskAdress("ENHETS_ID");
        notificationMessage.setIntygsId("INTYGS_ID");
        notificationMessage.setVersion(SchemaVersion.VERSION_3);
        notificationMessage.setStatusUpdateXml("statusUpdateXml".getBytes(StandardCharsets.UTF_8));

        final var mockMessage = mock(Message.class);
        final var factory = new ObjectFactory();
        final var certificateStatusUpdateForCareType = new CertificateStatusUpdateForCareType();
        final var certificateStatusUpdateForCare = factory.createCertificateStatusUpdateForCare(certificateStatusUpdateForCareType);

        try (MockedStatic<XmlMarshallerHelper> mockedStatic = mockStatic(XmlMarshallerHelper.class)) {
            doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

            mockedStatic.when(
                    () -> XmlMarshallerHelper.unmarshal(new String(notificationMessage.getStatusUpdateXml(), StandardCharsets.UTF_8)))
                .thenReturn(certificateStatusUpdateForCare);

            notificationTransformer.process(mockMessage);
            verifyNoInteractions(certificateStatusUpdateForCareCreator);
            verify(mockMessage).setBody(certificateStatusUpdateForCareType);
        }
    }

    @Test
    void shallSetVersionHeaderOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage(SchemaVersion.VERSION_3);
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(VERSION, notificationMessage.getVersion().name());
    }

    @Test
    void shallSetVersionV3HeaderOnTransformedMessageWithoutVersion() throws Exception {
        final var notificationMessage = createNotificationMessage(null);
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(VERSION, SchemaVersion.VERSION_3.name());
    }

    @Test
    void shallThrowExceptionOnTransformedMessageWithUnsupportedVersion() throws Exception {
        final var notificationMessage = createNotificationMessage(SchemaVersion.VERSION_1);
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        try {
            notificationTransformer.process(mockMessage);
            fail("Should have thrown exception due to unsupported version!");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    void shallSetLogicalAddressHeaderOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(LOGISK_ADRESS, notificationMessage.getLogiskAdress());
    }

    @Test
    void shallSetCertificateIdHeaderOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(INTYGS_ID, notificationMessage.getIntygsId());
    }

    @Test
    void shallSetEventTypeHeaderOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(HANDELSE, notificationMessage.getHandelse().value());
    }

    @Test
    void shallSetCertificateTypeVersionHeaderOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var expectedCertificateTypeVersion = "CERTIFICATE_TYPE";
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);
        doReturn(expectedCertificateTypeVersion).when(moduleRegistry)
            .resolveVersionFromUtlatandeJson(notificationMessage.getIntygsTyp(), "CERTIFICATE_AS_JSON_STRING");

        notificationTransformer.process(mockMessage);

        verify(mockMessage).setHeader(INTYG_TYPE_VERSION, expectedCertificateTypeVersion);
    }

    @Test
    void shallUseCertificateTypeVersionHeaderIfAlreadyExistsOnTransformedMessage() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var expectedCertificateTypeVersion = "CERTIFICATE_TYPE";
        final var mockMessage = mock(Message.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);
        doReturn(expectedCertificateTypeVersion).when(mockMessage).getHeader(INTYG_TYPE_VERSION);

        notificationTransformer.process(mockMessage);

        verify(moduleRegistry, noInteractions()).resolveVersionFromUtlatandeJson(any(), any());
    }

    @Test
    void shallSendResultMessageOnTransformationError() throws Exception {
        final var notificationMessage = createNotificationMessage();
        final var mockNotificationResultMessage = mock(NotificationResultMessage.class);
        final var mockMessage = mock(Message.class);

        final var captureNotificationResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        doReturn(notificationMessage).when(mockMessage).getBody(NotificationMessage.class);
        doThrow(new RuntimeException()).when(certificateStatusUpdateForCareCreator).create(any(), any());
        doReturn(mockNotificationResultMessage).when(notificationResultMessageCreator)
            .createFailureMessage(any(), any(), any(), any(), any());

        try {
            notificationTransformer.process(mockMessage);
            Assertions.fail("Should have thrown exception due to transformation error!");
        } catch (Exception e) {
            verify(notificationResultMessageSender).sendResultMessage(captureNotificationResultMessage.capture());

            Assertions.assertEquals(mockNotificationResultMessage, captureNotificationResultMessage.getValue());
        }
    }

    private NotificationMessage createNotificationMessage() {
        return createNotificationMessage(SchemaVersion.VERSION_3);
    }

    private NotificationMessage createNotificationMessage(SchemaVersion version) {
        final var notificationMessage = new NotificationMessage();
        notificationMessage.setHandelse(HandelsekodEnum.SKAPAT);
        notificationMessage.setLogiskAdress("ENHETS_ID");
        notificationMessage.setIntygsId("INTYGS_ID");
        notificationMessage.setVersion(version);
        final var mockCertificateAsJson = mock(JsonNode.class);
        doReturn("CERTIFICATE_AS_JSON_STRING").when(mockCertificateAsJson).toString();
        notificationMessage.setUtkast(mockCertificateAsJson);
        return notificationMessage;
    }
}
