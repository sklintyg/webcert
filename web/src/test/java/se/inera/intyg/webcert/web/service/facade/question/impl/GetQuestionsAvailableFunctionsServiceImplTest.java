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

import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertExclude;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertInclude;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Question.QuestionBuilder;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

class GetQuestionsAvailableFunctionsServiceImplTest {

    private GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService;

    @BeforeEach
    void setUp() {
        getQuestionsAvailableFunctionsService = new GetQuestionsAvailableFunctionsServiceImpl();
    }

    @Nested
    class RecievedAdministrativeQuestions {

        private QuestionBuilder questionBuilder;

        @BeforeEach
        void setUp() {
            questionBuilder = Question.builder()
                .type(QuestionType.COORDINATION)
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

        @Test
        void shallExcludeComplementCertificate() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }
    }

    @Nested
    class RecievedComplementQuestions {

        private QuestionBuilder questionBuilder;

        @BeforeEach
        void setUp() {
            questionBuilder = Question.builder()
                .type(QuestionType.COMPLEMENT)
                .author("Försäkringskassan");
        }

        @Test
        void shallExcludeAnswerQuestionIfQuestionIsComplement() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestionIfNotHandled() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallExcludeHandleQuestionIfHandled() {
            final var question = questionBuilder
                .isHandled(true)
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallIncludeComplementCertificateIfNotHandled() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeComplementCertificateIfHandled() {
            final var question = questionBuilder
                .isHandled(true)
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeComplementCertificateIfHasAnswerByCertificate() {
            final var question = questionBuilder
                .answeredByCertificate(
                    CertificateRelation.builder().build()
                )
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallIncludeCannotComplementCertificateIfNotHandled() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeCannotComplementCertificateIfHandled() {
            final var question = questionBuilder
                .isHandled(true)
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeCannotComplementCertificateIfHasAnswerByCertificate() {
            final var question = questionBuilder
                .answeredByCertificate(
                    CertificateRelation.builder().build()
                )
                .build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
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

        @Test
        void shallExcludeComplementCertificate() {
            final var question = questionBuilder.build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }
    }

    @Nested
    class ForwardQuestion {

        private QuestionBuilder questionBuilder;

        @BeforeEach
        void setUp() {
            questionBuilder = Question.builder()
                .author("Dr Doktor");
        }

        @Test
        void shallExcludeForwardQuestion() {
            final var question = questionBuilder.isHandled(true).build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertExclude(actualLinks, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }

        @Test
        void shallIncludeForwardQuestion() {
            final var question = questionBuilder.isHandled(false).build();
            final var actualLinks = getQuestionsAvailableFunctionsService.get(question);
            assertInclude(actualLinks, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }
    }
}
