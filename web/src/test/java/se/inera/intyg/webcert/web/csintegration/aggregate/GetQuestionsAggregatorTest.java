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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.message.GetQuestionsFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

@ExtendWith(MockitoExtension.class)
class GetQuestionsAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private GetQuestionsFacadeService getQuestionsFromWebcert;
    @Mock
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;
    @Mock
    private GetQuestionsFromCertificateService getQuestionsFromCertificateService;
    @InjectMocks
    private GetQuestionsAggregator getQuestionsAggregator;


    @Test
    void shallReturnQuestionsFromCSIfProfileIsActiveAndResponseIsNotNull() {
        final var expectedResult = QuestionsResponseDTO.builder().build();
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(expectedResult).when(getQuestionsFromCertificateService).get(CERTIFICATE_ID);

        final var response = getQuestionsAggregator.get(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(1)).get(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shallReturnQuestionsFromWCIfProfileIsActiveAndResponseIsNull() {
        final var question = Question.builder().build();
        final var resourceLinkDTOS = List.of(new ResourceLinkDTO());
        final var questionListMap = Map.of(question, resourceLinkDTOS);
        final var expectedResult = QuestionsResponseDTO.create(List.of(question), questionListMap);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(List.of(question)).when(getQuestionsFromWebcert).getQuestions(CERTIFICATE_ID);
        doReturn(questionListMap).when(getQuestionsResourceLinkService).get(List.of(question));

        final var response = getQuestionsAggregator.get(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(1)).get(CERTIFICATE_ID);
        verify(getQuestionsFromWebcert, times(1)).getQuestions(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shallReturnQuestionsFromWCIfProfileIsNotActive() {
        final var question = Question.builder().build();
        final var resourceLinkDTOS = List.of(new ResourceLinkDTO());
        final var questionListMap = Map.of(question, resourceLinkDTOS);
        final var expectedResult = QuestionsResponseDTO.create(List.of(question), questionListMap);

        doReturn(false).when(certificateServiceProfile).active();
        doReturn(List.of(question)).when(getQuestionsFromWebcert).getQuestions(CERTIFICATE_ID);
        doReturn(questionListMap).when(getQuestionsResourceLinkService).get(List.of(question));

        final var response = getQuestionsAggregator.get(CERTIFICATE_ID);
        verify(getQuestionsFromCertificateService, times(0)).get(CERTIFICATE_ID);
        verify(getQuestionsFromWebcert, times(1)).getQuestions(CERTIFICATE_ID);

        assertEquals(expectedResult, response);
    }
}
