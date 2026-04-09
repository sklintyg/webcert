/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
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

@RestController
@RequestMapping("/api/question")
public class QuestionController {

  private static final Logger LOG = LoggerFactory.getLogger(QuestionController.class);
  private static final String UTF_8_CHARSET = ";charset=utf-8";

  @Autowired private DeleteQuestionFacadeService deleteQuestionAggregator;
  @Autowired private CreateQuestionFacadeService createQuestionAggregator;
  @Autowired private SaveQuestionFacadeService saveQuestionAggregator;
  @Autowired private SendQuestionFacadeService sendQuestionAggregator;
  @Autowired private SaveQuestionAnswerFacadeService saveAnswerAggregator;
  @Autowired private DeleteQuestionAnswerFacadeService deleteAnswerAggregator;
  @Autowired private SendQuestionAnswerFacadeService sendAnswerAggregator;
  @Autowired private GetQuestionsResourceLinkService getQuestionsResourceLinkService;
  @Autowired private GetQuestionsFacadeService getQuestionsAggregator;
  @Autowired private HandleQuestionFacadeService handleQuestionAggregator;

  @GetMapping("/{certificateId}")
  @PerformanceLogging(
      eventAction = "question-get-questions",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<QuestionsResponseDTO> getQuestions(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Getting questions for certificate with id: '{}'", certificateId);
    }

    final var questions = getQuestionsAggregator.getQuestions(certificateId);
    final var links = getQuestionsResourceLinkService.get(questions);
    return ResponseEntity.ok(QuestionsResponseDTO.create(questions, links));
  }

  @GetMapping("/{certificateId}/complements")
  @PerformanceLogging(
      eventAction = "question-get-complement-questions",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<QuestionsResponseDTO> getComplementQuestions(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Getting complement questions for certificate with id: '{}'", certificateId);
    }

    final var questions = getQuestionsAggregator.getComplementQuestions(certificateId);
    final var links = getQuestionsResourceLinkService.get(questions);
    return ResponseEntity.ok(QuestionsResponseDTO.create(questions, links));
  }

  @DeleteMapping("/{questionId}")
  @PerformanceLogging(
      eventAction = "question-delete-questions",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable("questionId") @NotNull String questionId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Deleting question with id: '{}'", questionId);
    }

    deleteQuestionAggregator.delete(questionId);
    return ResponseEntity.ok().build();
  }

  @PostMapping
  @PerformanceLogging(
      eventAction = "question-create-question",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<QuestionResponseDTO> createQuestion(
      @RequestBody CreateQuestionRequestDTO createQuestionRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Creating question for certificate with id: '{}'",
          createQuestionRequest.getCertificateId());
    }

    final var question =
        createQuestionAggregator.create(
            createQuestionRequest.getCertificateId(),
            createQuestionRequest.getType(),
            createQuestionRequest.getMessage());

    final var links = getQuestionsResourceLinkService.get(question);
    return ResponseEntity.ok(QuestionResponseDTO.create(question, links));
  }

  @PostMapping("/{questionId}")
  @PerformanceLogging(
      eventAction = "question-save-question",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<QuestionResponseDTO> saveQuestion(
      @PathVariable("questionId") @NotNull String questionId,
      @RequestBody SaveQuestionRequestDTO saveQuestionRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Saving question with id: '{}'", saveQuestionRequest.getQuestion().getId());
    }

    final var savedQuestion = saveQuestionAggregator.save(saveQuestionRequest.getQuestion());
    final var links = getQuestionsResourceLinkService.get(savedQuestion);
    return ResponseEntity.ok(QuestionResponseDTO.create(savedQuestion, links));
  }

  @PostMapping("/{questionId}/send")
  @PerformanceLogging(
      eventAction = "question-send-question",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<QuestionResponseDTO> sendQuestion(
      @PathVariable("questionId") @NotNull String questionId,
      @RequestBody SendQuestionRequestDTO sendQuestionRequestDTO) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Sending question with id: '{}'", questionId);
    }

    final var question = sendQuestionAggregator.send(sendQuestionRequestDTO.getQuestion());
    final var links = getQuestionsResourceLinkService.get(question);
    return ResponseEntity.ok(QuestionResponseDTO.create(question, links));
  }

  @PostMapping("/{questionId}/saveanswer")
  @PerformanceLogging(
      eventAction = "question-save-question-answer",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<QuestionResponseDTO> saveQuestionAnswer(
      @PathVariable("questionId") @NotNull String questionId,
      @RequestBody AnswerRequestDTO answerRequestDTO) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Saving answer for question with id: '{}'", questionId);
    }

    final var questionWithSavedAnswer =
        saveAnswerAggregator.save(questionId, answerRequestDTO.getMessage());
    final var links = getQuestionsResourceLinkService.get(questionWithSavedAnswer);
    return ResponseEntity.ok(QuestionResponseDTO.create(questionWithSavedAnswer, links));
  }

  @DeleteMapping("/{questionId}/answer")
  @PerformanceLogging(
      eventAction = "question-delete-question-answer",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<QuestionResponseDTO> deleteQuestionAnswer(
      @PathVariable("questionId") @NotNull String questionId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Deleting answer for question with id: '{}'", questionId);
    }

    final var questionWithDeletedAnswer = deleteAnswerAggregator.delete(questionId);
    final var links = getQuestionsResourceLinkService.get(questionWithDeletedAnswer);
    return ResponseEntity.ok(QuestionResponseDTO.create(questionWithDeletedAnswer, links));
  }

  @PostMapping("/{questionId}/sendanswer")
  @PerformanceLogging(
      eventAction = "question-send-question-answer",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<QuestionResponseDTO> sendQuestionAnswer(
      @PathVariable("questionId") @NotNull String questionId,
      @RequestBody AnswerRequestDTO answerRequestDTO) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Send answer for question with id: '{}'", questionId);
    }

    final var questionWithSentAnswer =
        sendAnswerAggregator.send(questionId, answerRequestDTO.getMessage());
    final var links = getQuestionsResourceLinkService.get(questionWithSentAnswer);
    return ResponseEntity.ok(QuestionResponseDTO.create(questionWithSentAnswer, links));
  }

  @PostMapping("/{questionId}/handle")
  @PerformanceLogging(
      eventAction = "question-handle-question",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<QuestionResponseDTO> handleQuestion(
      @PathVariable("questionId") @NotNull String questionId,
      @RequestBody HandleQuestionRequestDTO handleQuestionRequestDTO) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Handle question with id: '{}'", questionId);
    }
    final var question =
        handleQuestionAggregator.handle(questionId, handleQuestionRequestDTO.isHandled());
    final var links = getQuestionsResourceLinkService.get(question);
    return ResponseEntity.ok(QuestionResponseDTO.create(question, links));
  }
}
