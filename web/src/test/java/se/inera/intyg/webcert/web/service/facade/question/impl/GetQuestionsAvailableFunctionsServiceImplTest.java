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

package se.inera.intyg.webcert.web.service.facade.question.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Question.QuestionBuilder;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

class GetQuestionsAvailableFunctionsServiceImplTest {

    private GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService;

    @BeforeEach
    void setUp() {
        getQuestionsAvailableFunctionsService = new GetQuestionsAvailableFunctionsServiceImpl();
    }

    @Nested
    class RecievedQuestions {

        private QuestionBuilder questionBuilder;

        @BeforeEach
        void setUp() {
            questionBuilder = Question.builder()
                .author("Försäkringskassan");
        }

        @Test
        void shallIncludeAnswerQuestionIfMissingAnswer() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeAnswerQuestionIfExistingAnswerIsNotSent() {
            final var question = questionBuilder
                .answer(Answer.builder().build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallExcludeAnswerQuestionIfExistingAnswerIsSent() {
            final var question = questionBuilder
                .answer(Answer.builder()
                    .sent(LocalDateTime.now())
                    .build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallExcludeAnswerQuestionIfQuestionIsHandled() {
            final var question = questionBuilder
                .isHandled(true)
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallExcludeAnswerQuestionIfQuestionIsComplement() {
            final var question = questionBuilder
                .type(QuestionType.COMPLEMENT)
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfMissingAnswer() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfExistingAnswerIsNotSent() {
            final var question = questionBuilder
                .answer(Answer.builder().build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallExcludeHandleQuestionIfExistingAnswerIsSent() {
            final var question = questionBuilder
                .answer(Answer.builder()
                    .sent(LocalDateTime.now())
                    .build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }
    }

    @Nested
    class SentQuestions {

        private QuestionBuilder questionBuilder;

        @BeforeEach
        void setUp() {
            questionBuilder = Question.builder()
                .author("Dr Doktor");
        }

        @Test
        void shallExcludeAnswerQuestion() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfMissingAnswer() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfExistingAnswerIsNotSent() {
            final var question = questionBuilder
                .answer(Answer.builder().build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfExistingAnswerIsSent() {
            final var question = questionBuilder
                .answer(Answer.builder()
                    .sent(LocalDateTime.now())
                    .build())
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }
    }

    private void assertInclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNotNull(actualResourceLink, () -> String.format("Expected resource link with type '%s'", type));
    }

    private void assertExclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNull(actualResourceLink, () -> String.format("Don't expect resource link with type '%s'", type));
    }

    private ResourceLinkDTO get(List<ResourceLinkDTO> resourceLinks, ResourceLinkTypeDTO type) {
        return resourceLinks.stream()
            .filter(resourceLinkDTO -> resourceLinkDTO.getType().equals(type))
            .findFirst()
            .orElse(null);
    }
}