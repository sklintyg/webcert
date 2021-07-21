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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@ExtendWith(MockitoExtension.class)
class GetQuestionFacadeServiceImplTest {

    @Mock
    private ArendeService arendeService;

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private QuestionConverter questionConverter;

    @InjectMocks
    private GetQuestionFacadeServiceImpl getQuestionFacadeService;

    private String certificateId = "certificateId";
    private String questionId = "questionId";
    private Arende arende;
    private Arende arendeSvar;
    private Arende arendePaminnelse;
    private List<Arende> relatedArenden;

    @BeforeEach
    void setUp() {
        arende = new Arende();
        arende.setMeddelandeId(questionId);
        arende.setIntygsId(certificateId);

        arendeSvar = new Arende();
        arendeSvar.setMeddelandeId("arendeSvarId");
        arendeSvar.setSvarPaId(arende.getMeddelandeId());

        arendePaminnelse = new Arende();
        arendePaminnelse.setMeddelandeId("arendePaminnelseId");
        arendePaminnelse.setPaminnelseMeddelandeId(arende.getMeddelandeId());

        relatedArenden = new ArrayList<>();

        doReturn(arende)
            .when(arendeService)
            .getArende(questionId);

        doReturn(relatedArenden)
            .when(arendeService)
            .getRelatedArenden(questionId);
    }

    @Test
    void shallReturnQuestion() {
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion, "Shall return a question");
    }

    @Test
    void shallReturnQuestionWithReminder() {
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }

    @Test
    void shallReturnQuestionWithAnswer() {
        relatedArenden.add(arendeSvar);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), eq(arendeSvar), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getAnswer(), "Shall return a question with answer");
    }

    @Test
    void shallReturnQuestionWithAnswerWithReminder() {
        relatedArenden.add(arendeSvar);
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), eq(arendeSvar), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }

    @Test
    void shallReturnQuestionWithAnswerDraft() {
        final var answerDraft = new ArendeDraft();
        doReturn(answerDraft)
            .when(arendeDraftService)
            .getAnswerDraft(certificateId, questionId);

        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .build())
            .when(questionConverter)
            .convert(eq(arende), eq(answerDraft), any(List.class));

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getAnswer(), "Shall return a question with answer");
    }

    @Test
    void shallReturnQuestionWithAnswerDraftWithReminder() {
        final var answerDraft = new ArendeDraft();
        doReturn(answerDraft)
            .when(arendeDraftService)
            .getAnswerDraft(certificateId, questionId);
        relatedArenden.add(arendePaminnelse);
        final var remindersArgCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(
            Question.builder()
                .answer(Answer.builder().build())
                .reminders(new Reminder[]{Reminder.builder().build()})
                .build())
            .when(questionConverter)
            .convert(eq(arende), eq(answerDraft), remindersArgCaptor.capture());

        final var actualQuestion = getQuestionFacadeService.get(questionId);
        assertNotNull(actualQuestion.getReminders()[0], "Shall return a question with reminder");
        assertEquals(arendePaminnelse, remindersArgCaptor.getValue().get(0));
    }
}