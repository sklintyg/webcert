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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.impl.SendQuestionFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@ExtendWith(MockitoExtension.class)
class SendQuestionFacadeServiceImplTest {

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private ArendeService arendeService;

    @Mock
    private QuestionConverter questionConverter;

    @InjectMocks
    private SendQuestionFacadeServiceImpl sendQuestionFacadeService;

    private ArendeDraft questionDraft;
    private Arende arende;

    private static final QuestionType QUESTION_TYPE = QuestionType.COORDINATION;
    private final ArendeAmne QUESTION_AMNE = ArendeAmne.AVSTMN;
    public static final Long QUESTION_ID_AS_LONG = 1000L;
    private final String QUESTION_ID_AS_STRING = Long.toString(QUESTION_ID_AS_LONG);
    private final String MESSAGE = "message";
    private final String SUBJECT = "subject";
    private final LocalDateTime SENT = LocalDateTime.now();
    private final LocalDateTime LAST_UPDATE = LocalDateTime.now().minusMinutes(100);


    @BeforeEach
    void setUp() {
        questionDraft = new ArendeDraft();
        questionDraft.setId(QUESTION_ID_AS_LONG);
        questionDraft.setAmne(ArendeAmne.AVSTMN.toString());
        doReturn(questionDraft).when(arendeDraftService).getQuestionDraftById(QUESTION_ID_AS_LONG);

        doReturn(new Arende())
            .when(arendeService)
            .sendMessage(questionDraft);

        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(any(Arende.class));
    }

    @Test
    void shallSendQuestion() {
        final var question = Question.builder()
            .id(QUESTION_ID_AS_STRING)
            .message(MESSAGE)
            .type(QUESTION_TYPE)
            .build();

        final var actualQuestion = sendQuestionFacadeService.send(question);
        assertNotNull(actualQuestion);
    }

    @Test
    void shallUpdateMessageWhenSendingQuestion() {
        final var question = Question.builder()
            .id(QUESTION_ID_AS_STRING)
            .message(MESSAGE)
            .type(QUESTION_TYPE)
            .build();

        final var arendeDraftArgumentCaptor = ArgumentCaptor.forClass(ArendeDraft.class);

        sendQuestionFacadeService.send(question);

        verify(arendeService).sendMessage(arendeDraftArgumentCaptor.capture());

        assertEquals(MESSAGE, arendeDraftArgumentCaptor.getValue().getText());
    }

    @Test
    void shallUpdateSubjectWhenSendingQuestion() {
        final var question = Question.builder()
            .id(QUESTION_ID_AS_STRING)
            .message(MESSAGE)
            .type(QuestionType.OTHER)
            .build();

        final var arendeDraftArgumentCaptor = ArgumentCaptor.forClass(ArendeDraft.class);

        sendQuestionFacadeService.send(question);

        verify(arendeService).sendMessage(arendeDraftArgumentCaptor.capture());

        assertEquals(ArendeAmne.OVRIGT.toString(), arendeDraftArgumentCaptor.getValue().getAmne());
    }
}
