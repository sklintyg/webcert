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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.facade.question.SaveQuestionAnswerFacadeService;

@ExtendWith(MockitoExtension.class)
class SaveAnswerAggregatorTest {


    private static final String QUESTION_ID = "questionId";
    private static final String MESSAGE = "content";
    @Mock
    SaveQuestionAnswerFacadeService saveAnswerFromWC;
    @Mock
    SaveQuestionAnswerFacadeService saveAnswerFromCS;
    SaveAnswerAggregator saveAnswerAggregator;

    @BeforeEach
    void setUp() {
        saveAnswerAggregator = new SaveAnswerAggregator(
            saveAnswerFromWC, saveAnswerFromCS
        );
    }

    @Test
    void shallReturnQuestionFromWCIfResponseFromCSIsNull() {
        final var expectedQuestion = Question.builder().build();
        doReturn(null).when(saveAnswerFromCS).save(QUESTION_ID, MESSAGE);
        doReturn(expectedQuestion).when(saveAnswerFromWC).save(QUESTION_ID, MESSAGE);

        final var actualQuestion = saveAnswerAggregator.save(QUESTION_ID, MESSAGE);
        assertEquals(expectedQuestion, actualQuestion);
    }

    @Test
    void shallReturnQuestionFromCSIResponseFromCSIsNotNull() {
        final var expectedQuestion = Question.builder().build();
        doReturn(expectedQuestion).when(saveAnswerFromCS).save(QUESTION_ID, MESSAGE);

        final var actualQuestion = saveAnswerAggregator.save(QUESTION_ID, MESSAGE);
        assertEquals(expectedQuestion, actualQuestion);
    }
}
