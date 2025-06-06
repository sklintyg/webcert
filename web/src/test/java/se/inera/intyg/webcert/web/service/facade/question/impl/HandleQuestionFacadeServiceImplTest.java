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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionFacadeService;

@ExtendWith(MockitoExtension.class)
class HandleQuestionFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @Mock
    private GetQuestionFacadeService getQuestionFacadeService;

    @InjectMocks
    private HandleQuestionFacadeServiceImpl handleQuestionFacadeService;

    private String questionId = "questionId";
    private String certificateType = "certificateType";

    @Nested
    class QuestionHandled {

        @BeforeEach
        void setUp() {
            final var arende = new Arende();
            arende.setMeddelandeId(questionId);
            arende.setIntygTyp(certificateType);

            doReturn(arende)
                .when(arendeService)
                .getArende(questionId);

            doReturn(Question.builder().build())
                .when(getQuestionFacadeService)
                .get(questionId);
        }

        @Test
        void shallSetQuestionAsHandled() {
            final var isHandled = true;

            final var actualQuestion = handleQuestionFacadeService.handle(questionId, isHandled);

            verify(arendeService).closeArendeAsHandled(questionId, certificateType);
        }

        @Test
        void shallHandleAndReturnQuestion() {
            final var isHandled = true;

            final var actualQuestion = handleQuestionFacadeService.handle(questionId, isHandled);

            assertNotNull(actualQuestion, "Should return the question that was answers");
        }
    }

    @Nested
    class QuestionUnhandled {

        @BeforeEach
        void setUp() {
            doReturn(Question.builder().build())
                .when(getQuestionFacadeService)
                .get(questionId);
        }

        @Test
        void shallSetQuestionAsNotHandled() {
            final var isHandled = false;

            final var actualQuestion = handleQuestionFacadeService.handle(questionId, isHandled);

            verify(arendeService).openArendeAsUnhandled(questionId);
        }

        @Test
        void shallUnHandleAndReturnQuestion() {
            final var isHandled = false;

            final var actualQuestion = handleQuestionFacadeService.handle(questionId, isHandled);

            assertNotNull(actualQuestion, "Should return the question that was answers");
        }
    }
}
