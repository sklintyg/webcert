/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.question;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.service.facade.question.impl.ArendeToQuestionFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.question.impl.FragaSvarToQuestionFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.question.impl.GetQuestionsFacadeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class GetQuestionsFacadeServiceImplTest {

    @Mock
    private ArendeToQuestionFacadeServiceImpl arendeToQuestionFacadeService;
    @Mock
    private FragaSvarToQuestionFacadeServiceImpl fragaSvarToQuestionFacadeService;

    @InjectMocks
    private GetQuestionsFacadeServiceImpl GetQuestionsFacadeServiceImpl;

    private static final String CERTIFICATE_ID = "certificateId";

    @Nested
    class IncludeGetQuestionsTest {

        @Test
        void shallGetQuestionsFromFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().build()
            );

            doReturn(questions).when(fragaSvarToQuestionFacadeService).getQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallGetQuestionsFromForArendeToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().build()
            );

            doReturn(questions).when(arendeToQuestionFacadeService).getQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallMergeQuestionsFromForArendeToQuestionFacadeServiceAndFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().build(), Question.builder().build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().build()
            );
            doReturn(questions).when(arendeToQuestionFacadeService).getQuestions(CERTIFICATE_ID);
            doReturn(questions).when(fragaSvarToQuestionFacadeService).getQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class IncludeGetComplementQuestions {

        @Test
        void shallGetComplementQuestionsFromFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().type(QuestionType.COMPLEMENT).build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().type(QuestionType.COMPLEMENT).build()
            );

            doReturn(questions).when(fragaSvarToQuestionFacadeService).getComplementQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getComplementQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallGetComplementQuestionsFromForArendeToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().type(QuestionType.COMPLEMENT).build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().type(QuestionType.COMPLEMENT).build()
            );

            doReturn(questions).when(arendeToQuestionFacadeService).getComplementQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getComplementQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallMergeComplementQuestionsFromForArendeToQuestionFacadeServiceAndFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().type(QuestionType.COMPLEMENT).build(),
                Question.builder().type(QuestionType.COMPLEMENT).build());

            final ArrayList<Question> questions = new ArrayList<>();
            questions.add(
                Question.builder().type(QuestionType.COMPLEMENT).build()
            );
            doReturn(questions).when(arendeToQuestionFacadeService).getComplementQuestions(CERTIFICATE_ID);
            doReturn(questions).when(fragaSvarToQuestionFacadeService).getComplementQuestions(CERTIFICATE_ID);

            final var actualResult = GetQuestionsFacadeServiceImpl.getComplementQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }
    }
}
