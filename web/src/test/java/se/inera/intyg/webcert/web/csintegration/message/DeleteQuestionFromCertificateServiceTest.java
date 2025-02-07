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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteMessageRequestDTO;

@ExtendWith(MockitoExtension.class)
class DeleteQuestionFromCertificateServiceTest {


    private static final String QUESTION_ID = "questionId";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    DeleteQuestionFromCertificateService deleteQuestionFromCertificateService;

    @Test
    void shallCallDeleteMessageFromCSIntegrationService() {
        final var request = DeleteMessageRequestDTO.builder().build();

        doReturn(request).when(csIntegrationRequestFactory).deleteMessageRequest();

        deleteQuestionFromCertificateService.delete(QUESTION_ID);

        verify(csIntegrationService).deleteMessage(QUESTION_ID, request);
    }
}
