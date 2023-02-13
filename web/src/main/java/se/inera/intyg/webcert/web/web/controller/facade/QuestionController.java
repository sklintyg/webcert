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
package se.inera.intyg.webcert.web.web.controller.facade;

import java.util.Collections;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier(value = "GetQuestionsFacadeServiceImpl")
    private GetQuestionsFacadeService getQuestionsFacadeService;
    @Autowired
    private DeleteQuestionFacadeService deleteQuestionFacadeService;
    @Autowired
    private CreateQuestionFacadeService createQuestionFacadeService;
    @Autowired
    private SaveQuestionFacadeService saveQuestionFacadeService;
    @Autowired
    private SendQuestionFacadeService sendQuestionFacadeService;
    @Autowired
    private SaveQuestionAnswerFacadeService saveQuestionAnswerFacadeService;
    @Autowired
    private DeleteQuestionAnswerFacadeService deleteQuestionAnswerFacadeService;
    @Autowired
    private SendQuestionAnswerFacadeService sendQuestionAnswerFacadeService;
    @Autowired
    private GetQuestionsResourceLinkService getQuestionsResourceLinkService;
    @Autowired
    private HandleQuestionFacadeService handleQuestionFacadeService;

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getQuestions(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting questions for certificate with id: '{}'", certificateId);
        }

        final var questions = getQuestionsFacadeService.getQuestions(certificateId);
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

        final var questions = getQuestionsFacadeService.getComplementQuestions(certificateId);
        return Response.ok(QuestionsResponseDTO.create(questions, Collections.emptyMap())).build();
    }

    @DELETE
    @Path("/{questionId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response deleteQuestion(@PathParam("questionId") @NotNull String questionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting question with id: '{}'", questionId);
        }

        deleteQuestionFacadeService.delete(questionId);
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

        final var question = createQuestionFacadeService.create(
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

        final var savedQuestion = saveQuestionFacadeService.save(saveQuestionRequest.getQuestion());
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

        final var question = sendQuestionFacadeService.send(sendQuestionRequestDTO.getQuestion());
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

        final var questionWithSavedAnswer = saveQuestionAnswerFacadeService.save(questionId, answerRequestDTO.getMessage());
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

        final var questionWithDeletedAnswer = deleteQuestionAnswerFacadeService.delete(questionId);
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

        final var questionWithSentAnswer = sendQuestionAnswerFacadeService.send(questionId, answerRequestDTO.getMessage());
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

        final var handledQuestion = handleQuestionFacadeService.handle(questionId, handleQuestionRequestDTO.isHandled());
        final var links = getQuestionsResourceLinkService.get(handledQuestion);
        return Response.ok(QuestionResponseDTO.create(handledQuestion, links)).build();
    }
}
