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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
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
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateFromMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;

@ExtendWith(MockitoExtension.class)
class HandleQuestionFromCertificateServiceTest {


    private static final String MESSAGE_ID = "messageId";
    private static final String PERSON_ID = "personId";
    private static final String WC = "WC";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String FK = "Försäkringskassan";
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    private PDLLogService pdlLogService;
    @Mock
    private PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @InjectMocks
    private HandleQuestionFromCertificateService handleQuestionFromCertificateService;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
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
    void shallReturnNullIfMessageDontExist() {
        doReturn(false).when(csIntegrationService).messageExists(MESSAGE_ID);
        assertNull(handleQuestionFromCertificateService.handle(MESSAGE_ID, false));
    }

    @Test
    void shallReturnHandledQuestion() {
        final var question = Question.builder()
            .author(WC)
            .build();

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        final var handleMessageRequestDTO = HandleMessageRequestDTO.builder().build();
        final var certificteFromMessageRequestDTO = GetCertificateFromMessageRequestDTO.builder().build();

        doReturn(handleMessageRequestDTO).when(csIntegrationRequestFactory).handleMessageRequestDTO(false);
        doReturn(certificteFromMessageRequestDTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
        doReturn(question).when(csIntegrationService).handleMessage(handleMessageRequestDTO, MESSAGE_ID);
        doReturn(certificate).when(csIntegrationService).getCertificate(certificteFromMessageRequestDTO, MESSAGE_ID);

        final var actualResponse = handleQuestionFromCertificateService.handle(MESSAGE_ID, false);
        assertEquals(question, actualResponse);
    }

    @Test
    void shallPdlLogCreateMessage() {
        final var question = Question.builder()
            .author(WC)
            .build();

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        final var handleMessageRequestDTO = HandleMessageRequestDTO.builder().build();
        final var certificteFromMessageRequestDTO = GetCertificateFromMessageRequestDTO.builder().build();

        doReturn(handleMessageRequestDTO).when(csIntegrationRequestFactory).handleMessageRequestDTO(false);
        doReturn(certificteFromMessageRequestDTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
        doReturn(question).when(csIntegrationService).handleMessage(handleMessageRequestDTO, MESSAGE_ID);
        doReturn(certificate).when(csIntegrationService).getCertificate(certificteFromMessageRequestDTO, MESSAGE_ID);

        handleQuestionFromCertificateService.handle(MESSAGE_ID, false);
        verify(pdlLogService).logCreateMessage(PERSON_ID, CERTIFICATE_ID);
    }

    @Test
    void shallPublishEventHandledByRecipient() {
        final var question = Question.builder()
            .author(FK)
            .build();

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        final var handleMessageRequestDTO = HandleMessageRequestDTO.builder().build();
        final var certificteFromMessageRequestDTO = GetCertificateFromMessageRequestDTO.builder().build();

        doReturn(handleMessageRequestDTO).when(csIntegrationRequestFactory).handleMessageRequestDTO(false);
        doReturn(certificteFromMessageRequestDTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
        doReturn(question).when(csIntegrationService).handleMessage(handleMessageRequestDTO, MESSAGE_ID);
        doReturn(certificate).when(csIntegrationService).getCertificate(certificteFromMessageRequestDTO, MESSAGE_ID);

        handleQuestionFromCertificateService.handle(MESSAGE_ID, false);
        verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.HANFRFM);
    }

    @Test
    void shallPublishEventHandledByCare() {
        final var question = Question.builder()
            .author(WC)
            .build();

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        final var handleMessageRequestDTO = HandleMessageRequestDTO.builder().build();
        final var certificteFromMessageRequestDTO = GetCertificateFromMessageRequestDTO.builder().build();

        doReturn(handleMessageRequestDTO).when(csIntegrationRequestFactory).handleMessageRequestDTO(false);
        doReturn(certificteFromMessageRequestDTO).when(csIntegrationRequestFactory).getCertificateFromMessageRequestDTO();
        doReturn(question).when(csIntegrationService).handleMessage(handleMessageRequestDTO, MESSAGE_ID);
        doReturn(certificate).when(csIntegrationService).getCertificate(certificteFromMessageRequestDTO, MESSAGE_ID);

        handleQuestionFromCertificateService.handle(MESSAGE_ID, false);
        verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.HANFRFV);
    }
}
