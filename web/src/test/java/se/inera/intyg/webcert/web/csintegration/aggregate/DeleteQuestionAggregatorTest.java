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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionFacadeService;

@ExtendWith(MockitoExtension.class)
class DeleteQuestionAggregatorTest {

    private static final String QUESTION_ID = "questionId";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    DeleteQuestionFacadeService deleteQuestionFromWC;
    @Mock
    DeleteQuestionFacadeService deleteQuestionFromCS;

    DeleteQuestionFacadeService deleteQuestionAggregator;

    @BeforeEach
    void setUp() {
        deleteQuestionAggregator = new DeleteQuestionAggregator(
            deleteQuestionFromWC, deleteQuestionFromCS, csIntegrationService
        );
    }

    @Test
    void shallCallDeleteQuestionsFromWCIfMessageDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).messageExists(QUESTION_ID);

        deleteQuestionAggregator.delete(QUESTION_ID);

        verify(deleteQuestionFromWC).delete(QUESTION_ID);

        verifyNoInteractions(deleteQuestionFromCS);
    }

    @Test
    void shallCallDeleteQuestionsFromCSIfMessageExistInCertificateService() {
        doReturn(true).when(csIntegrationService).messageExists(QUESTION_ID);

        deleteQuestionAggregator.delete(QUESTION_ID);

        verify(deleteQuestionFromCS).delete(QUESTION_ID);

        verifyNoInteractions(deleteQuestionFromWC);
    }
}
