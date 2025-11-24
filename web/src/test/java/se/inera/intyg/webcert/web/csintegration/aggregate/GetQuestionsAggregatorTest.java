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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.message.GetQuestionsFromCertificateService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;

@ExtendWith(MockitoExtension.class)
class GetQuestionsAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    @Mock
    private GetQuestionsFacadeService getQuestionsFromWebcert;
    @Mock
    private GetQuestionsFromCertificateService getQuestionsFromCertificateService;
    @InjectMocks
    private GetQuestionsAggregator getQuestionsAggregator;


    @Test
    void shallReturnQuestionsFromCSIfProfileIsActiveAndResponseIsNotNull() {
        final var expectedResult = List.of(Question.builder().build());        doReturn(expectedResult).when(getQuestionsFromCertificateService).get(CERTIFICATE_ID);

        final var response = getQuestionsAggregator.getQuestions(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(1)).get(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shallReturnQuestionsFromWCIfProfileIsActiveAndResponseIsNull() {
        final var question = Question.builder().build();
        final var expectedResult = List.of(question);        doReturn(null).when(getQuestionsFromCertificateService).get(CERTIFICATE_ID);
        doReturn(List.of(question)).when(getQuestionsFromWebcert).getQuestions(CERTIFICATE_ID);

        final var response = getQuestionsAggregator.getQuestions(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(1)).get(CERTIFICATE_ID);
        verify(getQuestionsFromWebcert, times(1)).getQuestions(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shallReturnQuestionsFromWCIfProfileIsNotActive() {
        final var question = Question.builder().build();
        final var expectedResult = List.of(question);        doReturn(List.of(question)).when(getQuestionsFromWebcert).getQuestions(CERTIFICATE_ID);

        final var response = getQuestionsAggregator.getQuestions(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(0)).get(CERTIFICATE_ID);
        verify(getQuestionsFromWebcert, times(1)).getQuestions(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shallReturnComplementQuestions() {
        final var resultFromCS = List.of(
            Question.builder()
                .type(QuestionType.COMPLEMENT)
                .build(),
            Question.builder()
                .type(QuestionType.CONTACT)
                .build()
        );

        final var expectedResult = List.of(
            Question.builder()
                .type(QuestionType.COMPLEMENT)
                .build()
        );        doReturn(resultFromCS).when(getQuestionsFromCertificateService).get(CERTIFICATE_ID);

        final var response = getQuestionsAggregator.getComplementQuestions(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(1)).get(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }
}
