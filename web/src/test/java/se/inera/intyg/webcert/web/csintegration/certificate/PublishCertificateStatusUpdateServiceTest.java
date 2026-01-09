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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class PublishCertificateStatusUpdateServiceTest {

    private static final String UNIT_ID = "unitId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String TYPE_VERSION = "typeVersion";
    private static final String INTYG_USER_HSA_ID = "hsaId";
    private static final String WEBCERT_HSA_ID = "webcertHsaId";
    private static final LocalDate LAST_DATE_TO_ANSWER = LocalDate.now();
    private final IntygUser intygUser = new IntygUser(INTYG_USER_HSA_ID);
    private final WebCertUser webCertUser = new WebCertUser();

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationMessageFactory notificationMessageFactory;
    @Mock
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;
    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    private final Certificate certificate = new Certificate();
    private final String xml = "xml";

    @BeforeEach
    void setUp() {
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .typeVersion(TYPE_VERSION)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .build()
                )
                .build()
        );
        webCertUser.setHsaId(WEBCERT_HSA_ID);
    }

    @Nested
    class UnitNotInRegistryTest {

        @BeforeEach
        void setUp() {
            doReturn(null).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
        }

        @Test
        void shallNotPublishStatusUpdateIfUnitNotInRegistry() {
            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);
            verifyNoInteractions(csIntegrationService);
        }
    }

    @Nested
    class GetCertificateXmlTest {

        @BeforeEach
        void setUp() {
            doReturn(new IntegreradEnhet()).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);
        }

        @Test
        void shallGetCertificateXmlWithCertificateId() {
            final var argumentCaptor = ArgumentCaptor.forClass(String.class);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT,
                Optional.empty(), Optional.empty(), null, null
            );

            verify(csIntegrationService).getInternalCertificateXml(argumentCaptor.capture());
            assertEquals(CERTIFICATE_ID, argumentCaptor.getValue());
        }
    }

    @Nested
    class NotificationMessageFactoryTest {

        private static final String XML_DATA = "xmlData";

        @BeforeEach
        void setUp() {
            doReturn(new IntegreradEnhet()).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
        }

        @Test
        void shallCallNotificationMessageFactoryWithHsaIdFromIntygUserIfPresent() {
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, INTYG_USER_HSA_ID, null, null);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT,
                Optional.of(intygUser), Optional.empty(), null, null
            );

            verify(notificationMessageFactory).create(certificate, xml, HandelsekodEnum.SKAPAT,
                INTYG_USER_HSA_ID, null, null);
        }

        @Test
        void shallCallNotificationMessageFactoryWithHsaIdFromWebcertUserIfIntygUserNotPresent() {
            doReturn(true).when(webCertUserService).hasAuthenticationContext();
            doReturn(webCertUser).when(webCertUserService).getUser();
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, WEBCERT_HSA_ID, null, null);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);

            verify(notificationMessageFactory).create(certificate, xml, HandelsekodEnum.SKAPAT,
                WEBCERT_HSA_ID, null, null);
        }

        @Test
        void shallCallNotificationMessageFactoryWithoutHsaIdIfHasAuthenticationContextReturnsFalse() {
            doReturn(false).when(webCertUserService).hasAuthenticationContext();
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, null, null, null);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);

            verify(notificationMessageFactory).create(certificate, xml, HandelsekodEnum.SKAPAT,
                null, null, null);
        }

        @Test
        void shallCallNotificationMessageFactoryWithProvidedXmlDataIfPresent() {
            doReturn(true).when(webCertUserService).hasAuthenticationContext();
            doReturn(webCertUser).when(webCertUserService).getUser();
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, XML_DATA, HandelsekodEnum.SKAPAT, WEBCERT_HSA_ID,
                    null, null);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT, XML_DATA);

            verify(notificationMessageFactory).create(certificate, XML_DATA, HandelsekodEnum.SKAPAT,
                WEBCERT_HSA_ID, null, null);
        }

        @Test
        void shallCallNotificationMessageFactoryWithProvidedQuestionTypeAndLastDateToAnswer() {
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, INTYG_USER_HSA_ID, ArendeAmne.KOMPLT, LAST_DATE_TO_ANSWER);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT,
                Optional.of(intygUser), Optional.empty(), ArendeAmne.KOMPLT, LAST_DATE_TO_ANSWER
            );

            verify(notificationMessageFactory).create(certificate, xml, HandelsekodEnum.SKAPAT,
                INTYG_USER_HSA_ID, ArendeAmne.KOMPLT, LAST_DATE_TO_ANSWER);
        }
    }

    @Nested
    class NotificationServiceTest {

        @BeforeEach
        void setUp() {
            doReturn(new IntegreradEnhet()).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);
            doReturn(true).when(webCertUserService).hasAuthenticationContext();
            doReturn(webCertUser).when(webCertUserService).getUser();
        }

        @Test
        void shallCallNotificationServiceWithNotificationMessage() {
            final var expectedMessage = new NotificationMessage();
            final var argumentCaptor = ArgumentCaptor.forClass(NotificationMessage.class);

            doReturn(expectedMessage).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, WEBCERT_HSA_ID, null, null);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);
            verify(notificationService).send(argumentCaptor.capture(), eq(UNIT_ID), eq(TYPE_VERSION));

            assertEquals(expectedMessage, argumentCaptor.getValue());
        }

        @Test
        void shallCallNotificationServiceWithUnitIdFromIssuingUnit() {
            final var argumentCaptor = ArgumentCaptor.forClass(String.class);
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, WEBCERT_HSA_ID, null, null);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);
            verify(notificationService).send(any(NotificationMessage.class), argumentCaptor.capture(),
                eq(TYPE_VERSION));

            assertEquals(UNIT_ID, argumentCaptor.getValue());
        }

        @Test
        void shallCallNotificationServiceWithTypeVersionFromCertificate() {
            final var argumentCaptor = ArgumentCaptor.forClass(String.class);
            doReturn(new NotificationMessage()).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, WEBCERT_HSA_ID, null, null);

            publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);
            verify(notificationService).send(any(NotificationMessage.class), eq(UNIT_ID),
                argumentCaptor.capture());

            assertEquals(TYPE_VERSION, argumentCaptor.getValue());
        }
    }

    @Nested
    class Resend {

        @Test
        void shallNotAllowNonIntegratedUnit() {
            final var event = mock(Handelse.class);
            final var notificationRedelivery = mock(NotificationRedelivery.class);

            doReturn(null).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);

            publishCertificateStatusUpdateService.resend(certificate, event, notificationRedelivery);

            verifyNoInteractions(notificationRedeliveryService);
        }

        @Test
        void shallCallNotificationMessageFactoryWithHsaIdFromRequestIfPresent() {
            final var expectedHsaId = "expectedHsaId";
            final var event = mock(Handelse.class);
            final var notificationRedelivery = mock(NotificationRedelivery.class);
            final var notificationMessage = new NotificationMessage();
            final var expectedStatusUpdate = "expected".getBytes(StandardCharsets.UTF_8);
            notificationMessage.setStatusUpdateXml(expectedStatusUpdate);

            doReturn(new IntegreradEnhet()).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
            doReturn(expectedHsaId).when(event).getHanteratAv();
            doReturn(HandelsekodEnum.SKAPAT).when(event).getCode();
            doReturn(notificationMessage).when(notificationMessageFactory)
                .create(certificate, xml, HandelsekodEnum.SKAPAT, expectedHsaId, null, null);
            doReturn(xml).when(csIntegrationService)
                .getInternalCertificateXml(CERTIFICATE_ID);

            publishCertificateStatusUpdateService.resend(certificate, event, notificationRedelivery);

            verify(notificationRedeliveryService).resend(notificationRedelivery, event,
                expectedStatusUpdate);
        }

    }
}