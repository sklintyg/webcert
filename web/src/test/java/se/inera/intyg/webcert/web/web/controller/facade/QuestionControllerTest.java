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
package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetQuestionsAggregator;
import se.inera.intyg.webcert.web.csintegration.aggregate.HandleQuestionAggregator;
import se.inera.intyg.webcert.web.service.facade.question.CreateQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.facade.question.SaveQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.SaveQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.SendQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.SendQuestionFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.AnswerRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.HandleQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendQuestionRequestDTO;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private DeleteQuestionFacadeService deleteQuestionFacadeService;

    @Mock
    private CreateQuestionFacadeService createQuestionFacadeService;

    @Mock
    private SaveQuestionFacadeService saveQuestionFacadeService;

    @Mock
    private SendQuestionFacadeService sendQuestionFacadeService;

    @Mock
    private SaveQuestionAnswerFacadeService saveQuestionAnswerFacadeService;

    @Mock
    private DeleteQuestionAnswerFacadeService deleteQuestionAnswerFacadeService;

    @Mock
    private SendQuestionAnswerFacadeService sendQuestionAnswerFacadeService;

    @Mock
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;

    @Mock
    private HandleQuestionAggregator handleQuestionAggregator;
    @Mock
    private GetQuestionsAggregator getQuestionsAggregator;

    @InjectMocks
    private QuestionController questionController;

    @Test
    void shallReturnQuestionResponse() {
        doReturn(Collections.singletonList(Question.builder().build()))
            .when(getQuestionsAggregator)
            .getQuestions("test");
        final var actualResponse = questionController.getQuestions("test");

        assertNotNull(((QuestionsResponseDTO) actualResponse.getEntity()).getQuestions().get(0));
    }

    @Test
    void shallReturnQuestionResponseWithoutResourceLinks() {
        doReturn(Collections.singletonList(Question.builder().build()))
            .when(getQuestionsAggregator)
            .getComplementQuestions("test");
        final var actualResponse = questionController.getComplementQuestions("test");

        assertNotNull(((QuestionsResponseDTO) actualResponse.getEntity()).getQuestions().get(0));
        Assertions.assertTrue(((QuestionsResponseDTO) actualResponse.getEntity()).getQuestions().get(0).getLinks().isEmpty());
    }

    @Test
    void shallReturnCreatedQuestion() {
        final var createQuestionRequestDTO = new CreateQuestionRequestDTO();
        createQuestionRequestDTO.setMessage("Message");
        createQuestionRequestDTO.setCertificateId("CertificateId");
        createQuestionRequestDTO.setType(QuestionType.COORDINATION);

        doReturn(Question.builder().build())
            .when(createQuestionFacadeService)
            .create(createQuestionRequestDTO.getCertificateId(), createQuestionRequestDTO.getType(), createQuestionRequestDTO.getMessage());

        final var actualResponse = questionController.createQuestion(createQuestionRequestDTO);

        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallDeleteQuestion() {
        final var actualResponse = questionController.deleteQuestion("test");

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());

        verify(deleteQuestionFacadeService).delete("test");
    }

    @Test
    void shallSaveQuestion() {
        final var saveQuestionRequestDTO = new SaveQuestionRequestDTO();
        final var question = Question.builder().build();
        saveQuestionRequestDTO.setQuestion(question);

        doReturn(Question.builder().build())
            .when(saveQuestionFacadeService)
            .save(question);

        final var actualResponse = questionController.saveQuestion(saveQuestionRequestDTO.getQuestion().getId(), saveQuestionRequestDTO);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());

        verify(saveQuestionFacadeService).save(question);
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallSendQuestion() {
        final var sendQuestionRequestDTO = new SendQuestionRequestDTO();
        final var question = Question.builder().build();
        sendQuestionRequestDTO.setQuestion(question);

        doReturn(Question.builder().build())
            .when(sendQuestionFacadeService)
            .send(question);

        final var actualResponse = questionController.sendQuestion(sendQuestionRequestDTO.getQuestion().getId(), sendQuestionRequestDTO);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());

        verify(sendQuestionFacadeService).send(question);
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallSaveAnswerForQuestion() {
        final var questionId = "questionId";
        final var answerRequestDTO = new AnswerRequestDTO();
        answerRequestDTO.setMessage("Det här är ett svar!");

        doReturn(Question.builder().build())
            .when(saveQuestionAnswerFacadeService)
            .save(questionId, answerRequestDTO.getMessage());

        final var actualResponse = questionController.saveQuestionAnswer(questionId, answerRequestDTO);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallDeleteAnswerForQuestion() {
        final var questionId = "questionId";

        doReturn(Question.builder().build())
            .when(deleteQuestionAnswerFacadeService)
            .delete(questionId);

        final var actualResponse = questionController.deleteQuestionAnswer(questionId);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallSendAnswerForQuestion() {
        final var questionId = "questionId";
        final var answerRequestDTO = new AnswerRequestDTO();
        answerRequestDTO.setMessage("Det här är ett svar!");

        doReturn(Question.builder().build())
            .when(sendQuestionAnswerFacadeService)
            .send(questionId, answerRequestDTO.getMessage());

        final var actualResponse = questionController.sendQuestionAnswer(questionId, answerRequestDTO);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }

    @Test
    void shallHandleQuestion() {
        final var questionId = "questionId";
        final var handleRequestDTO = new HandleQuestionRequestDTO();
        handleRequestDTO.setHandled(true);

        doReturn(Question.builder().build())
            .when(handleQuestionAggregator)
            .handle(questionId, handleRequestDTO.isHandled());

        final var actualResponse = questionController.handleQuestion(questionId, handleRequestDTO);

        assertEquals(HttpStatus.OK.value(), actualResponse.getStatus());
        assertNotNull(((QuestionResponseDTO) actualResponse.getEntity()).getQuestion());
    }
}
