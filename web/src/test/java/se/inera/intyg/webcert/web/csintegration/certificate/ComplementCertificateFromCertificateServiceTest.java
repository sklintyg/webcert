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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.facade.model.question.QuestionType.COMPLEMENT;
import static se.inera.intyg.webcert.persistence.arende.model.ArendeAmne.KOMPLT;

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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class ComplementCertificateFromCertificateServiceTest {

    private static final CertificateComplementRequestDTO COMPLEMENT_REQUEST_DTO = CertificateComplementRequestDTO.builder().build();
    private static final AnswerComplementRequestDTO ANSWER_COMPLEMENT_REQUEST_DTO = AnswerComplementRequestDTO.builder().build();
    private static final String PATIENT_ID = "PATIENT_ID";
    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final Certificate COMPLEMENTED_CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";
    private static final String MESSAGE = "message";
    private static final String UNIT_ID = "unitId";
    private static final int ONE = 1;
    private static final Patient PATIENT = Patient.builder()
        .personId(
            PersonId.builder()
                .id(PATIENT_ID)
                .build()
        )
        .build();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    IntegratedUnitRegistryHelper integratedUnitRegistryHelper;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntegrationParameters parameters;

    @Mock
    WebCertUser user;

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    @Mock
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    ComplementCertificateFromCertificateService complementCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCSForComplement() {
        final var response = complementCertificateFromCertificateService.complement(ID, MESSAGE);

        assertNull(response);
    }

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCSForAnswerComplement() {
        final var response = complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(anyString()))
                .thenReturn(true);

            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .patient(PATIENT)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID).build()
                )
                .build());

            COMPLEMENTED_CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(NEW_ID)
                .type(TYPE)
                .patient(PATIENT)
                .build());
        }

        @Nested
        class ComplementTests {

            @BeforeEach
            void setup() {

                when(csIntegrationService.getCertificate(anyString(), any()))
                    .thenReturn(CERTIFICATE);

                when(csIntegrationRequestFactory.complementCertificateRequest(eq(PATIENT), any()))
                    .thenReturn(COMPLEMENT_REQUEST_DTO);

                when(csIntegrationService.complementCertificate(ID, COMPLEMENT_REQUEST_DTO))
                    .thenReturn(COMPLEMENTED_CERTIFICATE);

                when(user.getParameters())
                    .thenReturn(parameters);

                when(webCertUserService.getUser())
                    .thenReturn(user);
            }

            @Test
            void shouldReturnCertificateIfExistInCS() {
                final var response = complementCertificateFromCertificateService.complement(ID, MESSAGE);

                assertEquals(COMPLEMENTED_CERTIFICATE, response);
            }

            @Test
            void shouldCallRequestFactory() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(csIntegrationRequestFactory).complementCertificateRequest(PATIENT, parameters);
            }

            @Test
            void shouldCallComplementRequestWithCertificateId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                complementCertificateFromCertificateService.complement(ID, MESSAGE);

                verify(csIntegrationService).complementCertificate(captor.capture(), any(CertificateComplementRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallComplementWithRequestOfTypeCertificateComplementRequestDTO() {
                final var captor = ArgumentCaptor.forClass(CertificateComplementRequestDTO.class);
                complementCertificateFromCertificateService.complement(ID, MESSAGE);

                verify(csIntegrationService).complementCertificate(eq(ID), captor.capture());
                assertEquals(COMPLEMENT_REQUEST_DTO, captor.getValue());
            }

            @Test
            void shouldPdlLogCreated() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(pdlLogService).logCreated(COMPLEMENTED_CERTIFICATE);

            }

            @Test
            void shouldPublishCreated() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(publishCertificateStatusUpdateService).publish(COMPLEMENTED_CERTIFICATE, HandelsekodEnum.SKAPAT);
            }

            @Test
            void shouldMonitorLogReplace() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(monitoringLogService).logIntygCopiedCompletion(NEW_ID, ID);
            }

            @Test
            void shouldRegisterUnit() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(integratedUnitRegistryHelper).addUnitForCopy(CERTIFICATE, COMPLEMENTED_CERTIFICATE);
            }

            @Test
            void shouldDecorateCertificateFromCSWithInformationFromWC() {
                complementCertificateFromCertificateService.complement(ID, MESSAGE);
                verify(decorateCertificateFromCSWithInformationFromWC, times(ONE)).decorate(COMPLEMENTED_CERTIFICATE);
            }

            @Test
            void shouldPublishAnalyticsMessage() {
                final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
                when(certificateAnalyticsMessageFactory.certificateComplemented(COMPLEMENTED_CERTIFICATE)).thenReturn(analyticsMessage);

                complementCertificateFromCertificateService.complement(ID, MESSAGE);

                verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
            }
        }

        @Nested
        class AnswerComplementTests {

            @BeforeEach
            void setup() {
                when(csIntegrationRequestFactory.answerComplementOnCertificateRequest(MESSAGE))
                    .thenReturn(ANSWER_COMPLEMENT_REQUEST_DTO);

                when(csIntegrationService.answerComplementOnCertificate(ID, ANSWER_COMPLEMENT_REQUEST_DTO))
                    .thenReturn(CERTIFICATE);
            }

            @Test
            void shouldReturnCertificateIfExistInCS() {
                final var response = complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldCallRequestFactory() {
                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);
                verify(csIntegrationRequestFactory).answerComplementOnCertificateRequest(MESSAGE);
            }

            @Test
            void shouldCallComplementWithRequestOfTypeCertificateComplementRequestDTO() {
                final var captor = ArgumentCaptor.forClass(AnswerComplementRequestDTO.class);
                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);

                verify(csIntegrationService).answerComplementOnCertificate(eq(ID), captor.capture());
                assertEquals(ANSWER_COMPLEMENT_REQUEST_DTO, captor.getValue());
            }

            @Test
            void shouldPublishStatusUpdate() {
                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);
                verify(publishCertificateStatusUpdateService).publish(CERTIFICATE, HandelsekodEnum.HANFRFM);
            }

            @Test
            void shouldDecorateCertificateFromCSWithInformationFromWC() {
                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);
                verify(decorateCertificateFromCSWithInformationFromWC, times(ONE)).decorate(CERTIFICATE);
            }
        }

        @Nested
        class MonitorlogMessageIdsOfAnsweredComplements {

            String certId;
            String certType;
            String certUnitId;

            @BeforeEach
            void setup() {
                certId = CERTIFICATE.getMetadata().getId();
                certType = CERTIFICATE.getMetadata().getType();
                certUnitId = CERTIFICATE.getMetadata().getUnit().getUnitId();

                when(csIntegrationRequestFactory.answerComplementOnCertificateRequest(MESSAGE))
                    .thenReturn(ANSWER_COMPLEMENT_REQUEST_DTO);
                when(csIntegrationService.answerComplementOnCertificate(ID, ANSWER_COMPLEMENT_REQUEST_DTO))
                    .thenReturn(CERTIFICATE);
            }

            @Test
            void shouldMonitorArendeCreated() {
                final var questionsBefore = List.of(Question.builder().id("q1").type(COMPLEMENT).build());
                final var questionsAfter = List.of(Question.builder().id("q1").type(COMPLEMENT)
                    .answer(Answer.builder().id("a1").build()).build());

                when(csIntegrationService.getQuestions(ID)).thenReturn(questionsBefore).thenReturn(questionsAfter);

                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);
                verify(monitoringLogService, times(ONE)).logArendeCreated(certId, certType, certUnitId, KOMPLT, true, "a1");
                verifyNoMoreInteractions(monitoringLogService);
            }

            @Test
            void shouldMonitorlogOnlyPreviouslyUnansweredComplements() {
                final var question1 = Question.builder().id("q1").type(COMPLEMENT).answer(Answer.builder().id("a1").build()).build();
                final var question2 = Question.builder().id("q2").type(COMPLEMENT).answer(Answer.builder().id("a2").build()).build();

                final var questionsBefore = List.of(question1, question2,
                    Question.builder().id("q3").type(COMPLEMENT).build(),
                    Question.builder().id("q4").type(COMPLEMENT).build()
                );

                final var questionsAfter = List.of(question1, question2,
                    Question.builder().id("q3").type(COMPLEMENT).answer(Answer.builder().id("a3").build()).build(),
                    Question.builder().id("q4").type(COMPLEMENT).answer(Answer.builder().id("a4").build()).build(),
                    Question.builder().id("q5").type(COMPLEMENT).build()
                );

                when(csIntegrationService.getQuestions(certId)).thenReturn(questionsBefore).thenReturn(questionsAfter);

                complementCertificateFromCertificateService.answerComplement(ID, MESSAGE);
                verify(monitoringLogService, times(ONE)).logArendeCreated(certId, certType, certUnitId, KOMPLT, true, "a3");
                verify(monitoringLogService, times(ONE)).logArendeCreated(certId, certType, certUnitId, KOMPLT, true, "a4");
                verifyNoMoreInteractions(monitoringLogService);
            }
        }
    }
}
