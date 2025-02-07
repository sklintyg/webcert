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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class GetQuestionsFromCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private GetQuestionsFromCertificateService getQuestionsFromCertificateService;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        assertNull(getQuestionsFromCertificateService.get(CERTIFICATE_ID));
    }

    @Test
    void shallReturnListOfQuestionsFromCertificateService() {
        final var questionDto = Question.builder().build();
        final var expectedQuestions = new ArrayList<Question>();
        expectedQuestions.add(questionDto);
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .patient(
                    Patient.builder()
                        .personId(PersonId.builder()
                            .id(PERSON_ID)
                            .build())
                        .build()
                )
                .build()
        );
        final var getCertificateRequestDTO = GetCertificateRequestDTO.builder().build();
        final var getCertificateMessageRequestDTO = GetCertificateMessageRequestDTO.builder().build();

        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(getCertificateRequestDTO).when(csIntegrationRequestFactory).getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(CERTIFICATE_ID, getCertificateRequestDTO);
        doReturn(getCertificateMessageRequestDTO).when(csIntegrationRequestFactory).getCertificateMessageRequest(PERSON_ID);
        when(csIntegrationService.getQuestions(getCertificateMessageRequestDTO, CERTIFICATE_ID)).thenReturn(expectedQuestions);

        final var actualQuestions = getQuestionsFromCertificateService.get(CERTIFICATE_ID);
        assertEquals(expectedQuestions, actualQuestions);
    }
}
