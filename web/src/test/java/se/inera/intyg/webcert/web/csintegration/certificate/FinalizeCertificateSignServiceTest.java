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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
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
    private SendCertificateFromCertificateService sendCertificateFromCertificateService;
    @Mock
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    private PDLLogService pdlLogService;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    @InjectMocks
    private FinalizeCertificateSignService finalizeCertificateSignService;

    private static final WebCertUser USER = new WebCertUser();

    private final Certificate certificate = new Certificate();

    @BeforeEach
    void setUp() {
        USER.setHsaId(HSA_ID);
        USER.setAuthenticationScheme(AUTH_SCHEME);
        doReturn(USER).when(webCertUserService).getUser();

        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build()
        );

        certificate.setLinks(Collections.emptyList());
    }

    @Nested
    class MonitorLoggingTests {

        private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        private final ArgumentCaptor<RelationKod> argumentCaptorRelation = ArgumentCaptor.forClass(RelationKod.class);

        @Test
        void shouldLogIntygSignedWithIdFromCertificate() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(monitoringLogService).logIntygSigned(argumentCaptor.capture(), eq(TYPE), eq(HSA_ID), eq(AUTH_SCHEME), eq(null));

            assertEquals(ID, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithTypeFromCertificate() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(monitoringLogService).logIntygSigned(eq(ID), argumentCaptor.capture(), eq(HSA_ID), eq(AUTH_SCHEME), eq(null));

            assertEquals(TYPE, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithHsaIdFromWebcertUser() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), argumentCaptor.capture(), eq(AUTH_SCHEME), eq(null));

            assertEquals(HSA_ID, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithAuthSchemaFromWebcertUser() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), eq(HSA_ID), argumentCaptor.capture(), eq(null));

            assertEquals(AUTH_SCHEME, argumentCaptor.getValue());
        }

        @Test
        void shouldLogIntygSignedWithRelationCodeNull() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(monitoringLogService).logIntygSigned(eq(ID), eq(TYPE), eq(HSA_ID), eq(AUTH_SCHEME), argumentCaptorRelation.capture());

            assertNull(argumentCaptorRelation.getValue());
        }
    }

    @Nested
    class PdlLoggingTests {

        private final ArgumentCaptor<Certificate> certificateArgumentCaptor = ArgumentCaptor.forClass(Certificate.class);

        @Test
        void shouldLogSignWithProvidedCertificate() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(pdlLogService).logSign(certificateArgumentCaptor.capture());

            assertEquals(certificate, certificateArgumentCaptor.getValue());
        }
    }

    @Nested
    class PublishCertificateStatusForCare {

        @Test
        void shouldPublishCertificateStatusUpdate() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.SIGNAT);
        }
    }


    @Nested
    class PublishCertificateAnalyticsMessageTests {

        @Test
        void shouldPublishAnalyticsMessageWhenCertificateIsSigned() {
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.certificateSigned(certificate)).thenReturn(analyticsMessage);
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
        }
    }

    @Nested
    class SendCertificateTests {

        @Test
        void shouldSendCertificateIfResourceLinkSendAfterSignIsPresentOnCertificate() {
            certificate.setLinks(
                List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.SEND_AFTER_SIGN_CERTIFICATE)
                    .build())
            );
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(sendCertificateFromCertificateService).sendCertificate(ID);
        }

        @Test
        void shouldNotSendCertificateIfResourceLinkSendAfterSignIsNotPresentOnCertificate() {
            certificate.setLinks(
                List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.SEND_CERTIFICATE)
                    .build())
            );
            finalizeCertificateSignService.finalizeSign(certificate);
            verifyNoInteractions(sendCertificateFromCertificateService);
        }
    }

    @Nested
    class NotifyHandledComplementQuestionsForParentCertificate {

        private List<Question> questionsWithoutComplement;
        private List<Question> questionsWithComplement;

        @BeforeEach
        void setUp() {
            questionsWithoutComplement = List.of(Question.builder().type(QuestionType.CONTACT).build());
            questionsWithComplement = List.of(Question.builder().type(QuestionType.COMPLEMENT).build());
        }

        @Test
        void shouldNotNotifyHandledComplementQuestionWhenNoRelations() {
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateStatusUpdateService, never()).publish(certificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotNotifyHandledComplementQuestionWhenNoParent() {
            certificate.getMetadata().setRelations(CertificateRelations.builder().build());
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateStatusUpdateService, never()).publish(certificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotNotifyHandledComplementQuestionWhenParentIsCopied() {
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(
                        CertificateRelation.builder()
                            .type(CertificateRelationType.COPIED)
                            .build()
                    )
                    .build()
            );
            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateStatusUpdateService, never()).publish(certificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotNotifyHandledComplementQuestionWhenParentHasNoComplementQuestions() {
            final var parentCertificateId = "parentCertificateId";
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(
                        CertificateRelation.builder()
                            .certificateId(parentCertificateId)
                            .type(CertificateRelationType.REPLACED)
                            .build()
                    )
                    .build()
            );

            doReturn(questionsWithoutComplement).when(csIntegrationService).getQuestions(parentCertificateId);

            finalizeCertificateSignService.finalizeSign(certificate);
            verify(publishCertificateStatusUpdateService, never()).publish(certificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotifyHandledComplementQuestionWhenReplacedParentHasComplementQuestions() {
            final var parentCertificateId = "parentCertificateId";
            final var parentCertificate = new Certificate();
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(
                        CertificateRelation.builder()
                            .certificateId(parentCertificateId)
                            .type(CertificateRelationType.REPLACED)
                            .build()
                    )
                    .build()
            );

            doReturn(questionsWithComplement).when(csIntegrationService).getQuestions(parentCertificateId);
            doReturn(parentCertificate).when(csIntegrationService).getInternalCertificate(parentCertificateId);

            finalizeCertificateSignService.finalizeSign(certificate);

            verify(publishCertificateStatusUpdateService).publish(parentCertificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotifyHandledComplementQuestionWhenComplementedParentHasComplementQuestions() {
            final var parentCertificateId = "parentCertificateId";
            final var parentCertificate = new Certificate();
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(
                        CertificateRelation.builder()
                            .certificateId(parentCertificateId)
                            .type(CertificateRelationType.COMPLEMENTED)
                            .build()
                    )
                    .build()
            );

            doReturn(questionsWithComplement).when(csIntegrationService).getQuestions(parentCertificateId);
            doReturn(parentCertificate).when(csIntegrationService).getInternalCertificate(parentCertificateId);

            finalizeCertificateSignService.finalizeSign(certificate);

            verify(publishCertificateStatusUpdateService).publish(parentCertificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shouldNotifyHandledComplementQuestionWhenExtendedParentHasComplementQuestions() {
            final var parentCertificateId = "parentCertificateId";
            final var parentCertificate = new Certificate();
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(
                        CertificateRelation.builder()
                            .certificateId(parentCertificateId)
                            .type(CertificateRelationType.EXTENDED)
                            .build()
                    )
                    .build()
            );

            doReturn(questionsWithComplement).when(csIntegrationService).getQuestions(parentCertificateId);
            doReturn(parentCertificate).when(csIntegrationService).getInternalCertificate(parentCertificateId);

            finalizeCertificateSignService.finalizeSign(certificate);

            verify(publishCertificateStatusUpdateService).publish(parentCertificate, HandelsekodEnum.HANFRFM);
        }
    }
}
