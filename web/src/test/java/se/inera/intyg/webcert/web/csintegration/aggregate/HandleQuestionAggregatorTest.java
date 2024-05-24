/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.certificate.HandleQuestionFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

@ExtendWith(MockitoExtension.class)
class HandleQuestionAggregatorTest {

    private static final String QUESTION_ID = "questionId";
    @Mock
    private HandleQuestionFacadeService handleQuestionFromWC;
    @Mock
    private HandleQuestionFromCertificateService handleQuestionFromCS;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;

    private HandleQuestionAggregator handleQuestionAggregator;

    @BeforeEach
    void setUp() {
        handleQuestionAggregator = new HandleQuestionAggregator(
            handleQuestionFromWC, handleQuestionFromCS, certificateServiceProfile, getQuestionsResourceLinkService
        );
    }

    @Test
    void shallReturnQuestionsFromWebcertIfProfileIsInactive() {
        final var question = Question.builder().build();
        final var link = List.of(new ResourceLinkDTO());
        doReturn(question).when(handleQuestionFromWC).handle(QUESTION_ID, false);
        doReturn(link).when(getQuestionsResourceLinkService).get(question);
        doReturn(false).when(certificateServiceProfile).active();
        handleQuestionAggregator.handle(QUESTION_ID, false);
        verify(handleQuestionFromWC).handle(QUESTION_ID, false);
    }

    @Test
    void shallReturnQuestionsFromCSIfProfileIsActiveAndResponseFromCSIsNotNull() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(QuestionResponseDTO.builder().build()).when(handleQuestionFromCS).handle(QUESTION_ID, false);
        handleQuestionAggregator.handle(QUESTION_ID, false);
        verifyNoInteractions(handleQuestionFromWC);
        verify(handleQuestionFromCS).handle(QUESTION_ID, false);
    }

    @Test
    void shallReturnQuestionsFromWebcertIfProfileIsActiveAndResponseFromCSIsNull() {
        final var question = Question.builder().build();
        final var link = List.of(new ResourceLinkDTO());
        final var expectedResult = QuestionResponseDTO.create(question, link);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(null).when(handleQuestionFromCS).handle(QUESTION_ID, false);
        doReturn(question).when(handleQuestionFromWC).handle(QUESTION_ID, false);
        doReturn(link).when(getQuestionsResourceLinkService).get(question);

        final var actualResult = handleQuestionAggregator.handle(QUESTION_ID, false);
        assertEquals(expectedResult, actualResult);
    }
}
