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

package se.inera.intyg.webcert.web.csintegration.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateFromMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendAnswerRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class SendAnswerFromCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "191212121212";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String UNIT_ID = "unitId";
    private static final SendAnswerRequestDTO SEND_ANSWER_REQUEST_DTO = SendAnswerRequestDTO.builder().build();
    private static final GetCertificateFromMessageRequestDTO GET_CERTIFICATE_FROM_MESSAGE_REQUEST_DTO =
        GetCertificateFromMessageRequestDTO.builder()
            .build();
    private static final Answer ANSWER = Answer.builder().build();
    private static final Question SENT_QUESTION = Question.builder()
        .type(QuestionType.CONTACT)
        .answer(ANSWER)
        .build();
    private static final String QUESTION_ID = "questionId";
    private static final String MESSAGE = "message";

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    MonitoringLogService monitoringLogService;
    @Mock
    PDLLogService pdlLogService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    SendAnswerFromCertificateService sendAnswerFromCertificateService;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .build()
                )
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PERSON_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    @Test
    void shallReturnNullIfMessageDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).messageExists(QUESTION_ID);
        assertNull(sendAnswerFromCertificateService.send(QUESTION_ID, MESSAGE));
    }

    @Test
    void shallReturnSentMessage() {
        final var expectedQuestion = SENT_QUESTION;

        doReturn(true).when(csIntegrationService).messageExists(QUESTION_ID);
        doReturn(GET_CERTIFICATE_FROM_MESSAGE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
        doReturn(certificate).when(csIntegrationService).getCertificate(GET_CERTIFICATE_FROM_MESSAGE_REQUEST_DTO, QUESTION_ID);
        doReturn(SEND_ANSWER_REQUEST_DTO).when(csIntegrationRequestFactory).sendAnswerRequest(PERSON_ID, MESSAGE);
        doReturn(expectedQuestion).when(csIntegrationService).sendAnswer(SEND_ANSWER_REQUEST_DTO, QUESTION_ID);

        final var actualQuestion = sendAnswerFromCertificateService.send(QUESTION_ID, MESSAGE);

        assertEquals(expectedQuestion, actualQuestion);
    }

    @Nested
    class LoggingTests {

        @BeforeEach
        void setUp() {
            doReturn(true).when(csIntegrationService).messageExists(QUESTION_ID);
            doReturn(GET_CERTIFICATE_FROM_MESSAGE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
            doReturn(certificate).when(csIntegrationService).getCertificate(GET_CERTIFICATE_FROM_MESSAGE_REQUEST_DTO, QUESTION_ID);
            doReturn(SEND_ANSWER_REQUEST_DTO).when(csIntegrationRequestFactory).sendAnswerRequest(PERSON_ID, MESSAGE);
            doReturn(SENT_QUESTION).when(csIntegrationService).sendAnswer(SEND_ANSWER_REQUEST_DTO, QUESTION_ID);
        }

        @Test
        void shallPdlLogCreateMessage() {
            sendAnswerFromCertificateService.send(QUESTION_ID, MESSAGE);
            ;
            verify(pdlLogService).logCreateMessage(PERSON_ID, CERTIFICATE_ID);
        }

        @Test
        void shallPublishStatusUpdate() {
            sendAnswerFromCertificateService.send(QUESTION_ID, MESSAGE);
            ;
            verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.HANFRFM);
        }

        @Test
        void shallMonitorLog() {
            sendAnswerFromCertificateService.send(QUESTION_ID, MESSAGE);
            ;
            verify(monitoringLogService).logArendeCreated(
                CERTIFICATE_ID,
                CERTIFICATE_TYPE,
                UNIT_ID,
                ArendeAmne.KONTKT,
                false,
                QUESTION_ID
            );
        }
    }
}
