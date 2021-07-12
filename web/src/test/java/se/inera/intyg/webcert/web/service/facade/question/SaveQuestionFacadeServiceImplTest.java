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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;

@ExtendWith(MockitoExtension.class)
class SaveQuestionFacadeServiceImplTest {

    public static final String CERTIFICATE_ID = "certificateId";
    public static final QuestionType QUESTION_TYPE = QuestionType.COORDINATION;
    public static final String MESSAGE = "message";
    public static final Long QUESTION_ID = 1000L;

    @Mock
    private ArendeDraftService arendeDraftService;

    @InjectMocks
    private SaveQuestionFacadeServiceImpl saveQuestionFacadeService;

    private ArendeDraft questionDraft;

    @BeforeEach
    void setUp() {
        questionDraft = new ArendeDraft();
        questionDraft.setId(QUESTION_ID);
        questionDraft.setAmne(ArendeAmne.AVSTMN.toString());
        doReturn(questionDraft).when(arendeDraftService).getQuestionDraftById(QUESTION_ID);
    }

    @Test
    void shallSaveQuestion() {
        Question question = Question.builder()
            .id(Long.toString(QUESTION_ID))
            .message(MESSAGE)
            .type(QUESTION_TYPE)
            .build();
        saveQuestionFacadeService.save(question);
        verify(arendeDraftService).saveDraft(questionDraft);
    }
}