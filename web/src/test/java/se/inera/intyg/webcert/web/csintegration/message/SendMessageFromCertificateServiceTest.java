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
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class SendMessageFromCertificateServiceTest {

    private static final String MESSAGE_ID = "messageId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final Question QUESTION = Question.builder()
        .id(MESSAGE_ID)
        .certificateId(CERTIFICATE_ID)
        .build();
    private static final String PERSON_ID = "191212121212";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String UNIT_ID = "unitId";
    private static final SendMessageRequestDTO SEND_MESSAGE_REQUEST_DTO = SendMessageRequestDTO.builder().build();
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST_DTO = GetCertificateRequestDTO.builder().build();
    private static final Question SENT_QUESTION = Question.builder()
        .type(QuestionType.CONTACT)
        .build();

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
    SendMessageFromCertificateService sendMessageFromCertificateService;
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
        doReturn(false).when(csIntegrationService).messageExists(MESSAGE_ID);
        assertNull(sendMessageFromCertificateService.send(QUESTION));
    }

    @Test
    void shallReturnSentMessage() {
        final var expectedQuestion = SENT_QUESTION;

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
        doReturn(SEND_MESSAGE_REQUEST_DTO).when(csIntegrationRequestFactory).sendMessageRequest(PERSON_ID);
        doReturn(expectedQuestion).when(csIntegrationService).sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);

        final var actualQuestion = sendMessageFromCertificateService.send(QUESTION);
        assertEquals(expectedQuestion, actualQuestion);
    }

    @Nested
    class LoggingTests {

        @BeforeEach
        void setUp() {
            doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
            doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCertificateRequest();
            doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO);
            doReturn(SEND_MESSAGE_REQUEST_DTO).when(csIntegrationRequestFactory).sendMessageRequest(PERSON_ID);
            doReturn(SENT_QUESTION).when(csIntegrationService).sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);
        }

        @Test
        void shallPdlLogCreateMessage() {
            sendMessageFromCertificateService.send(QUESTION);
            verify(pdlLogService).logCreateMessage(PERSON_ID, CERTIFICATE_ID);
        }

        @Test
        void shallPublishStatusUpdate() {
            sendMessageFromCertificateService.send(QUESTION);
            verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.NYFRFV);
        }

        @Test
        void shallMonitorLog() {
            sendMessageFromCertificateService.send(QUESTION);
            verify(monitoringLogService).logArendeCreated(
                CERTIFICATE_ID,
                CERTIFICATE_TYPE,
                UNIT_ID,
                ArendeAmne.KONTKT,
                false,
                MESSAGE_ID
            );
        }
    }
}
