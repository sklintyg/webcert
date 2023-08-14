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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

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
import se.inera.intyg.webcert.web.service.facade.question.impl.CreateQuestionFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@ExtendWith(MockitoExtension.class)
class CreateQuestionFacadeServiceImplTest {

    public static final String CERTIFICATE_ID = "certificateId";
    public static final QuestionType QUESTION_TYPE = QuestionType.COORDINATION;
    public static final String MESSAGE = "message";

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private QuestionConverter questionConverter;

    @InjectMocks
    private CreateQuestionFacadeServiceImpl createQuestionFacadeService;

    private ArendeDraft arendeDraft = new ArendeDraft();

    @BeforeEach
    void setUp() {
        doReturn(Question.builder().build())
            .when(questionConverter)
            .convert(arendeDraft);
    }

    @Test
    void shallReturnCreatedQuestion() {
        doReturn(arendeDraft)
            .when(arendeDraftService)
            .create(CERTIFICATE_ID, ArendeAmne.AVSTMN.toString(), MESSAGE, null);

        final var actualQuestion = createQuestionFacadeService.create(CERTIFICATE_ID, QUESTION_TYPE, MESSAGE);
        assertNotNull(actualQuestion, "Should return created question");
    }

    @Test
    void shallAllowToCreateQuestionWithoutQuestionType() {
        doReturn(arendeDraft)
            .when(arendeDraftService)
            .create(CERTIFICATE_ID, "", MESSAGE, null);

        final var actualQuestion = createQuestionFacadeService.create(CERTIFICATE_ID, QuestionType.MISSING, MESSAGE);
        assertNotNull(actualQuestion, "Should return created question");
    }
}