/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private GetQuestionsFacadeService getQuestionsFacadeService;

    @InjectMocks
    private QuestionController questionController;

    @Test
    void shallReturnQuestionResponse() {
        doReturn(Collections.singletonList(Question.builder().build()))
            .when(getQuestionsFacadeService)
            .getQuestions("test");
        final var actualResponse = questionController.getQuestions("test");

        assertNotNull(((QuestionsResponseDTO) actualResponse.getEntity()).getQuestions().get(0));
    }
}