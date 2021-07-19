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

package se.inera.intyg.webcert.web.service.facade.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.impl.GetQuestionsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@ExtendWith(MockitoExtension.class)
public class GetQuestionsFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private QuestionConverter questionConverter;

    @InjectMocks
    private GetQuestionsFacadeServiceImpl getQuestionsFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";

    @Test
    void shallReturnEmptyQuestionsIfNoQuestionDraft() {
        doReturn(null)
            .when(arendeDraftService)
            .getQuestionDraft(CERTIFICATE_ID);

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Test
    void shallReturnOneQuestionIfQuestionDraft() {
        setupMockToReturnQuestionDraft();

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question to be returned");
    }

    @Test
    void shallReturnEmptyQuestionsIfQuestions() {
        doReturn(Collections.emptyList())
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Test
    void shallReturnQuestionsIfQuestions() {
        setupMockToReturnQuestions();

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(3, actualQuestions.size(), "Expect three question to be returned");
    }

    @Test
    void shallReturnQuestionsIfQuestionsAndQuestionDraft() {
        setupMockToReturnQuestions();

        setupMockToReturnQuestionDraft();

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(4, actualQuestions.size(), "Expect four question to be returned");
    }

    @Test
    void shallReturnQuestionWithAnswer() {
        setupMockToReturnQuestionWithAnswer();

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getAnswer(), "Expect an answer for the question");
    }

    @Test
    void shallReturnQuestionWithAnswerDraft() {
        setupMockToReturnQuestionWithAnswerDraft();

        final var actualQuestions = getQuestionsFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(1, actualQuestions.size(), "Expect one question");
        assertNotNull(actualQuestions.get(0).getAnswer(), "Expect an answer for the question");
    }

    private void setupMockToReturnQuestionDraft() {
        doReturn(new ArendeDraft())
            .when(arendeDraftService)
            .getQuestionDraft(CERTIFICATE_ID);

        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(any(ArendeDraft.class));
    }

    private void setupMockToReturnQuestions() {
        doReturn(List.of(new Arende(), new Arende(), new Arende()))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(any(Arende.class));
    }

    private void setupMockToReturnQuestionWithAnswer() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var answer = new Arende();
        answer.setMeddelandeId("answerId");
        answer.setSvarPaId("questionId");

        doReturn(List.of(question, answer))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(Question.builder().answer(Answer.builder().build()).build())
            .when(questionConverter)
            .convert(question, answer);
    }

    private void setupMockToReturnQuestionWithAnswerDraft() {
        final var question = new Arende();
        question.setMeddelandeId("questionId");

        final var answer = new ArendeDraft();
        answer.setQuestionId("questionId");

        doReturn(List.of(question))
            .when(arendeService)
            .getArendenInternal(CERTIFICATE_ID);

        doReturn(List.of(answer))
            .when(arendeDraftService)
            .listAnswerDrafts(CERTIFICATE_ID);

        doReturn(Question.builder().answer(Answer.builder().build()).build())
            .when(questionConverter)
            .convert(question, answer);
    }
}
