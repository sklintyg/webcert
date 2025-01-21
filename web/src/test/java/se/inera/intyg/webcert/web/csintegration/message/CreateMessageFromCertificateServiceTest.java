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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class CreateMessageFromCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE = "message";
    private static final String PERSONID = "personid";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    CreateMessageFromCertificateService createMessageFromCertificateService;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        assertNull(createMessageFromCertificateService.create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE));
    }

    @Test
    void shallReturnCreatedQuestionFromCertificateService() {
        final var expectedQuestion = Question.builder().build();
        final var createMessageRequestDTO = CreateMessageRequestDTO.builder().build();
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PERSONID)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        final var getCertificateRequestDTO = GetCertificateRequestDTO.builder().build();

        doReturn(getCertificateRequestDTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, getCertificateRequestDTO);
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(createMessageRequestDTO).when(csIntegrationRequestFactory).createMessageRequest(QuestionType.CONTACT, MESSAGE, PERSONID);
        doReturn(expectedQuestion).when(csIntegrationService).createMessage(createMessageRequestDTO, CERTIFICATE_ID);

        assertEquals(expectedQuestion, createMessageFromCertificateService.create(CERTIFICATE_ID, QuestionType.CONTACT, MESSAGE));
    }
}
