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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.question.CreateQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.facade.question.HandleQuestionFacadeService;
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

@Path("/question")
public class QuestionController {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private DeleteQuestionFacadeService deleteQuestionAggregator;
    @Autowired
    private CreateQuestionFacadeService createQuestionAggregator;
    @Autowired
    private SaveQuestionFacadeService saveQuestionAggregator;
    @Autowired
    private SendQuestionFacadeService sendQuestionAggregator;
    @Autowired
    private SaveQuestionAnswerFacadeService saveAnswerAggregator;
    @Autowired
    private DeleteQuestionAnswerFacadeService deleteAnswerAggregator;
    @Autowired
    private SendQuestionAnswerFacadeService sendAnswerAggregator;
    @Autowired
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;
    @Autowired
    private GetQuestionsFacadeService getQuestionsAggregator;
    @Autowired
    private HandleQuestionFacadeService handleQuestionAggregator;

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getQuestions(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting questions for certificate with id: '{}'", certificateId);
        }

        final var questions = getQuestionsAggregator.getQuestions(certificateId);
        final var links = getQuestionsResourceLinkService.get(questions);
        return Response.ok(QuestionsResponseDTO.create(questions, links)).build();
    }

    @GET
    @Path("/{certificateId}/complements")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getComplementQuestions(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting complement questions for certificate with id: '{}'", certificateId);
        }

        final var questions = getQuestionsAggregator.getComplementQuestions(certificateId);
        final var links = getQuestionsResourceLinkService.get(questions);
        return Response.ok(QuestionsResponseDTO.create(questions, links)).build();
    }

    @DELETE
    @Path("/{questionId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response deleteQuestion(@PathParam("questionId") @NotNull String questionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting question with id: '{}'", questionId);
        }

        deleteQuestionAggregator.delete(questionId);
        return Response.ok().build();
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response createQuestion(CreateQuestionRequestDTO createQuestionRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating question for certificate with id: '{}'", createQuestionRequest.getCertificateId());
        }

        final var question = createQuestionAggregator.create(
            createQuestionRequest.getCertificateId(),
            createQuestionRequest.getType(),
            createQuestionRequest.getMessage()
        );

        final var links = getQuestionsResourceLinkService.get(question);
        return Response.ok(QuestionResponseDTO.create(question, links)).build();
    }

    @POST
    @Path("/{questionId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveQuestion(@PathParam("questionId") @NotNull String questionId, SaveQuestionRequestDTO saveQuestionRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving question with id: '{}'", saveQuestionRequest.getQuestion().getId());
        }

        final var savedQuestion = saveQuestionAggregator.save(saveQuestionRequest.getQuestion());
        final var links = getQuestionsResourceLinkService.get(savedQuestion);
        return Response.ok(QuestionResponseDTO.create(savedQuestion, links)).build();
    }

    @POST
    @Path("/{questionId}/send")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response sendQuestion(@PathParam("questionId") @NotNull String questionId, SendQuestionRequestDTO sendQuestionRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending question with id: '{}'", questionId);
        }

        final var question = sendQuestionAggregator.send(sendQuestionRequestDTO.getQuestion());
        final var links = getQuestionsResourceLinkService.get(question);
        return Response.ok(QuestionResponseDTO.create(question, links)).build();
    }

    @POST
    @Path("/{questionId}/saveanswer")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveQuestionAnswer(@PathParam("questionId") @NotNull String questionId, AnswerRequestDTO answerRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving answer for question with id: '{}'", questionId);
        }

        final var questionWithSavedAnswer = saveAnswerAggregator.save(questionId, answerRequestDTO.getMessage());
        final var links = getQuestionsResourceLinkService.get(questionWithSavedAnswer);
        return Response.ok(QuestionResponseDTO.create(questionWithSavedAnswer, links)).build();
    }

    @DELETE
    @Path("/{questionId}/answer")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response deleteQuestionAnswer(@PathParam("questionId") @NotNull String questionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting answer for question with id: '{}'", questionId);
        }

        final var questionWithDeletedAnswer = deleteAnswerAggregator.delete(questionId);
        final var links = getQuestionsResourceLinkService.get(questionWithDeletedAnswer);
        return Response.ok(QuestionResponseDTO.create(questionWithDeletedAnswer, links)).build();
    }

    @POST
    @Path("/{questionId}/sendanswer")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response sendQuestionAnswer(@PathParam("questionId") @NotNull String questionId, AnswerRequestDTO answerRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Send answer for question with id: '{}'", questionId);
        }

        final var questionWithSentAnswer = sendAnswerAggregator.send(questionId, answerRequestDTO.getMessage());
        final var links = getQuestionsResourceLinkService.get(questionWithSentAnswer);
        return Response.ok(QuestionResponseDTO.create(questionWithSentAnswer, links)).build();
    }

    @POST
    @Path("/{questionId}/handle")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response handleQuestion(@PathParam("questionId") @NotNull String questionId, HandleQuestionRequestDTO handleQuestionRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handle question with id: '{}'", questionId);
        }
        final var question = handleQuestionAggregator.handle(questionId, handleQuestionRequestDTO.isHandled());
        final var links = getQuestionsResourceLinkService.get(question);
        return Response.ok(QuestionResponseDTO.create(question, links)).build();
    }
}
