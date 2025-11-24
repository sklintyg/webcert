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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.certificate.HandleQuestionFromCertificateService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;

@ExtendWith(MockitoExtension.class)
class HandleQuestionAggregatorTest {

    private static final String QUESTION_ID = "questionId";
    @Mock
    private HandleQuestionFacadeService handleQuestionFromWC;
    @Mock
    private HandleQuestionFromCertificateService handleQuestionFromCS;
    @Mock
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;

    private HandleQuestionAggregator handleQuestionAggregator;

    @BeforeEach
    void setUp() {
        handleQuestionAggregator = new HandleQuestionAggregator(
            handleQuestionFromWC, handleQuestionFromCS
        );
    }

    @Test
    void shallReturnQuestionsFromWebcertIfProfileIsInactive() {
        final var question = Question.builder().build();

        doReturn(question).when(handleQuestionFromWC).handle(QUESTION_ID, false);
        handleQuestionAggregator.handle(QUESTION_ID, false);
        verify(handleQuestionFromWC).handle(QUESTION_ID, false);
    }

    @Test
    void shallReturnQuestionsFromCSIfProfileIsActiveAndResponseFromCSIsNotNull() {
        doReturn(Question.builder().build()).when(handleQuestionFromCS).handle(QUESTION_ID, false);
        handleQuestionAggregator.handle(QUESTION_ID, false);
        verifyNoInteractions(handleQuestionFromWC);
        verify(handleQuestionFromCS).handle(QUESTION_ID, false);
    }

    @Test
    void shallReturnQuestionsFromWebcertIfProfileIsActiveAndResponseFromCSIsNull() {
        final var expectedQuestion = Question.builder().build();
        doReturn(null).when(handleQuestionFromCS).handle(QUESTION_ID, false);
        doReturn(expectedQuestion).when(handleQuestionFromWC).handle(QUESTION_ID, false);

        final var actualResult = handleQuestionAggregator.handle(QUESTION_ID, false);
        assertEquals(expectedQuestion, actualResult);
    }
}
