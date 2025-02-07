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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionFacadeService;

@ExtendWith(MockitoExtension.class)
class DeleteQuestionAnswerFacadeServiceImplTest {

    private final String CERTIFICATE_ID = "certificateId";
    private final String QUESTION_ID = "questionId";

    @Mock
    ArendeService arendeService;

    @Mock
    ArendeDraftService arendeDraftService;

    @Mock
    GetQuestionFacadeService getQuestionFacadeService;

    @InjectMocks
    private DeleteQuestionAnswerFacadeServiceImpl deleteQuestionAnswerFacadeService;

    private Arende arende;

    @BeforeEach
    void setup() {
        arende = new Arende();
        arende.setIntygsId(CERTIFICATE_ID);
        arende.setMeddelandeId(QUESTION_ID);

        doReturn(arende)
            .when(arendeService)
            .getArende(QUESTION_ID);

        doReturn(Question.builder().build())
            .when(getQuestionFacadeService)
            .get(QUESTION_ID);
    }

    @Test
    void shallDeleteDraftForQuestion() {
        deleteQuestionAnswerFacadeService.delete(QUESTION_ID);
        // When deleting a question answer draft you pass the questions ID.
        verify(arendeDraftService).delete(CERTIFICATE_ID, QUESTION_ID);
    }

    @Test
    void shallQuestionWhenDeletingAnswerDraft() {
        final var actualQuestion = deleteQuestionAnswerFacadeService.delete(QUESTION_ID);
        assertNull(actualQuestion.getAnswer(), "Answer should be null after deleting the answer draft");
    }
}
