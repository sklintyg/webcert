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
package se.inera.intyg.webcert.web.service.facade.question;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.service.facade.question.impl.GetQuestionsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;

@ExtendWith(MockitoExtension.class)
public class GetQuestionsFacadeServiceImplTest {

    @Mock
    private IntygService intygService;
    private GetQuestionsFacadeService arendeToQuestionFacadeService;
    private GetQuestionsFacadeService fragaSvarToQuestionFacadeService;
    private GetQuestionsFacadeServiceImpl getQuestionsFacadeServiceImpl;

    private static final String CERTIFICATE_ID = "certificateId";

    @BeforeEach
    void setUp() {
        arendeToQuestionFacadeService = mock(GetQuestionsFacadeService.class);
        fragaSvarToQuestionFacadeService = mock(GetQuestionsFacadeService.class);
        getQuestionsFacadeServiceImpl = new GetQuestionsFacadeServiceImpl(intygService, arendeToQuestionFacadeService,
            fragaSvarToQuestionFacadeService);
    }

    @Nested
    class GetQuestionsFromFragaSvar {

        @BeforeEach
        void setUp() {
            doReturn(new IntygTypeInfo(CERTIFICATE_ID, Fk7263EntryPoint.MODULE_ID, "1.0"))
                .when(intygService)
                .getIntygTypeInfo(CERTIFICATE_ID);
        }

        @Test
        void shallGetQuestionsFromFragaSvar() {
            final var expectedResult = List.of(Question.builder().build());

            doReturn(expectedResult)
                .when(fragaSvarToQuestionFacadeService)
                .getQuestions(CERTIFICATE_ID);

            final var actualResult = getQuestionsFacadeServiceImpl.getQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallGetComplementQuestionsFromFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().type(QuestionType.COMPLEMENT).build());

            doReturn(expectedResult)
                .when(fragaSvarToQuestionFacadeService)
                .getComplementQuestions(CERTIFICATE_ID);

            final var actualResult = getQuestionsFacadeServiceImpl.getComplementQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class GetQuestionsFromArende {

        @BeforeEach
        void setUp() {
            doReturn(new IntygTypeInfo(CERTIFICATE_ID, LisjpEntryPoint.MODULE_ID, "1.0"))
                .when(intygService)
                .getIntygTypeInfo(CERTIFICATE_ID);
        }

        @Test
        void shallGetQuestionsFromFragaSvarToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().build());

            doReturn(expectedResult)
                .when(arendeToQuestionFacadeService)
                .getQuestions(CERTIFICATE_ID);

            final var actualResult = getQuestionsFacadeServiceImpl.getQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }

        @Test
        void shallGetComplementQuestionsFromForArendeToQuestionFacadeService() {
            final var expectedResult = List.of(Question.builder().type(QuestionType.COMPLEMENT).build());

            doReturn(expectedResult)
                .when(arendeToQuestionFacadeService)
                .getComplementQuestions(CERTIFICATE_ID);

            final var actualResult = getQuestionsFacadeServiceImpl.getComplementQuestions(CERTIFICATE_ID);
            assertIterableEquals(expectedResult, actualResult);
        }
    }
}
