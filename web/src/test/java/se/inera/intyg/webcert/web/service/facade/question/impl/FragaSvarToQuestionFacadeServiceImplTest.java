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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.facade.question.util.FragaSvarToQuestionConverter;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;

@ExtendWith(MockitoExtension.class)
class FragaSvarToQuestionFacadeServiceImplTest {

    @Mock
    private FragaSvarService fragaSvarService;
    @Mock
    private FragaSvarToQuestionConverter fragaSvarToQuestionConverter;
    private static final String CERTIFICATE_ID = "certificateId";
    @InjectMocks
    private FragaSvarToQuestionFacadeServiceImpl fragaSvarToQuestionFacadeService;

    @Test
    void shallReturnEmptyListOfQuestionsIfNoQuestionsArePresent() {
        doReturn(Collections.emptyList())
            .when(fragaSvarService)
            .getFragaSvar(CERTIFICATE_ID);

        final var actualQuestions = fragaSvarToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertTrue(actualQuestions.isEmpty(), "Don't expect any questions");
    }

    @Test
    void shallReturnListOfQuestionsIfPresent() {
        setupMockToReturnQuestions();

        final var actualQuestions = fragaSvarToQuestionFacadeService.getQuestions(CERTIFICATE_ID);

        assertEquals(3, actualQuestions.size(), "Expect three question to be returned");
    }

    @Test
    void shallReturnListOfComplementQuestionsIfComplementQuestionsArePresent() {
        final var expectedResult = List.of(
            Question.builder().type(QuestionType.COMPLEMENT).build(),
            Question.builder().type(QuestionType.COMPLEMENT).build());

        final var fragaSvar = new FragaSvar();
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

        final var fragaSvarViews = List.of(
            FragaSvarView.builder().fragaSvar(new FragaSvar()).build(),
            FragaSvarView.builder().fragaSvar(new FragaSvar()).build()
        );

        doReturn(fragaSvarViews)
            .when(fragaSvarService)
            .getFragaSvar(CERTIFICATE_ID);

        doReturn(Question.builder().type(QuestionType.COMPLEMENT).build())
            .when(fragaSvarToQuestionConverter)
            .convert(any(FragaSvar.class));

        final var actualComplementQuestions = fragaSvarToQuestionFacadeService.getComplementQuestions(
            CERTIFICATE_ID);

        assertIterableEquals(expectedResult, actualComplementQuestions);
    }

    private void setupMockToReturnQuestions() {
        doReturn(
            List.of(
                FragaSvarView.builder().fragaSvar(new FragaSvar()).build(),
                FragaSvarView.builder().fragaSvar(new FragaSvar()).build(),
                FragaSvarView.builder().fragaSvar(new FragaSvar()).build()
            ))
            .when(fragaSvarService)
            .getFragaSvar(CERTIFICATE_ID);

        doReturn(Question.builder().build())
            .when(fragaSvarToQuestionConverter)
            .convert(any(FragaSvar.class));
    }
}
