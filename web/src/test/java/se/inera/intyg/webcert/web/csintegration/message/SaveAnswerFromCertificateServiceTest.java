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
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveAnswerRequestDTO;

@ExtendWith(MockitoExtension.class)
class SaveAnswerFromCertificateServiceTest {

    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE = "content";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    SaveAnswerFromCertificateService saveAnswerFromCertificateService;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).messageExists(MESSAGE_ID);
        assertNull(saveAnswerFromCertificateService.save(MESSAGE_ID, MESSAGE));
    }

    @Test
    void shallReturnSavedQuestionFromCertificateService() {
        final var expectedQuestion = Question.builder().build();
        final var saveMessageRequestDTO = SaveAnswerRequestDTO.builder().build();

        doReturn(true).when(csIntegrationService).messageExists(MESSAGE_ID);
        doReturn(saveMessageRequestDTO).when(csIntegrationRequestFactory).saveAnswerRequest(MESSAGE);
        doReturn(expectedQuestion).when(csIntegrationService).saveAnswer(saveMessageRequestDTO, MESSAGE_ID);

        assertEquals(expectedQuestion, saveAnswerFromCertificateService.save(MESSAGE_ID, MESSAGE));
    }
}
