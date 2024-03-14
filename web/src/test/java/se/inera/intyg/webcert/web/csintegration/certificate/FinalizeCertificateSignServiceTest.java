/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.web.csintegration.certificateevents.CertificateEventType.CERTIFICATE_SIGNED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.certificateevents.CertificateEventMessage;
import se.inera.intyg.webcert.web.csintegration.certificateevents.CertificateEventService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class FinalizeCertificateSignServiceTest {

    private static final String HSA_ID = "hsaId";
    private static final String AUTH_SCHEME = "authScheme";
    private static final String ID = "id";
    private static final String TYPE = "type";
    @Mock
    private PDLLogService pdlLogService;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private CertificateEventService certificateEventService;

    @InjectMocks
    private FinalizeCertificateSignService finalizeCertificateSignService;

    private static final Certificate CERTIFICATE = new Certificate();
    private static final WebCertUser USER = new WebCertUser();

    @BeforeEach
    void setUp() {
        USER.setHsaId(HSA_ID);
        USER.setAuthenticationScheme(AUTH_SCHEME);
        doReturn(USER).when(webCertUserService).getUser();

        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build()
        );
    }

    @Nested
    class MonitorLoggingTests {

        private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        private final ArgumentCaptor<RelationKod> argumentCaptorRelation = ArgumentCaptor.forClass(RelationKod.class);

        @Test
        void shouldLogIntygSignedWithIdFromCertificate() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(monitoringLogService).logIntygSigned(argumentCaptor.capture(), eq(TYPE), eq(HSA_ID), eq(AUTH_SCHEME), eq(null));

            assertEquals(ID, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithTypeFromCertificate() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(monitoringLogService).logIntygSigned(eq(ID), argumentCaptor.capture(), eq(HSA_ID), eq(AUTH_SCHEME), eq(null));

            assertEquals(TYPE, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithHsaIdFromWebcertUser() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), argumentCaptor.capture(), eq(AUTH_SCHEME), eq(null));

            assertEquals(HSA_ID, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithAuthSchemaFromWebcertUser() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), eq(HSA_ID), argumentCaptor.capture(), eq(null));

            assertEquals(AUTH_SCHEME, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithRelationCodeNull() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), eq(HSA_ID), eq(AUTH_SCHEME), argumentCaptorRelation.capture());

            assertNull(argumentCaptorRelation.getValue());
        }
    }

    @Nested
    class PdlLoggingTests {

        private final ArgumentCaptor<Certificate> certificateArgumentCaptor = ArgumentCaptor.forClass(Certificate.class);

        @Test
        void shouldLogSignWithProvidedCertificate() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(pdlLogService).logSign(certificateArgumentCaptor.capture());

            assertEquals(CERTIFICATE, certificateArgumentCaptor.getValue());
        }
    }

    @Nested
    class NotificationTests {

        private final ArgumentCaptor<CertificateEventMessage> argumentCaptor = ArgumentCaptor.forClass(
            CertificateEventMessage.class);

        @Test
        void shouldSendNotificationCertificateMessageWithCertificateId() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(certificateEventService).send(argumentCaptor.capture());

            assertEquals(ID, argumentCaptor.getValue().getCertificateId());
        }

        @Test
        void shouldSendNotificationCertificateMessageWithEventTypeCertificateSigned() {
            finalizeCertificateSignService.finalizeSign(CERTIFICATE);
            verify(certificateEventService).send(argumentCaptor.capture());

            assertEquals(CERTIFICATE_SIGNED, argumentCaptor.getValue().getEventType());
        }
    }
}
