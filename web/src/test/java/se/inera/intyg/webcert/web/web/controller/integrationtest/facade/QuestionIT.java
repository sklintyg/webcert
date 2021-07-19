/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.AnswerRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendQuestionRequestDTO;

public class QuestionIT {

    private List<String> certificateIdsToCleanAfterTest;

    @BeforeEach
    public void setupBase() {
        final var logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.config = RestAssured.config()
            .logConfig(logConfig)
            .sessionConfig(new SessionConfig("SESSION", null));
        certificateIdsToCleanAfterTest = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        certificateIdsToCleanAfterTest.forEach(certificateId ->
            given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .delete("testability/intyg/{certificateId}")
        );
        RestAssured.reset();
    }

    @Test
    @DisplayName("Shall get question for certificate")
    void shallGetQuestionForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .question()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertFalse(response.isEmpty(), "Expect to contain question")
        );
    }

    @Test
    @DisplayName("Shall get question draft for certificate")
    void shallGetQuestionDraftForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertFalse(response.isEmpty(), "Expect to contain question")
        );
    }

    @Test
    @DisplayName("Shall delete question for certificate")
    void shallDeleteQuestionForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        given()
            .pathParam("questionId", testSetup.questionDraftId())
            .expect().statusCode(200)
            .when()
            .delete("api/question/{questionId}");

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertTrue(response.isEmpty(), "Expect to not contain question")
        );
    }

    @Test
    @DisplayName("Shall create question for certificate")
    void shallCreateQuestionForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var createQuestionRequestDTO = new CreateQuestionRequestDTO();
        createQuestionRequestDTO.setMessage("Message");
        createQuestionRequestDTO.setCertificateId(testSetup.certificateId());
        createQuestionRequestDTO.setType(QuestionType.COORDINATION);

        final var question = given()
            .contentType(ContentType.JSON)
            .body(createQuestionRequestDTO)
            .expect().statusCode(200)
            .when()
            .post("api/question")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertTrue(!question.getId().isEmpty(), "Expect to have a question id")
        );
    }

    @Test
    @DisplayName("Shall save question for certificate")
    void shallSaveQuestionForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var saveQuestionRequestDTO = new SaveQuestionRequestDTO();
        saveQuestionRequestDTO.setQuestion(Question.builder()
            .type(QuestionType.COORDINATION)
            .message("message")
            .id(testSetup.questionDraftId())
            .build());

        given()
            .contentType(ContentType.JSON)
            .body(saveQuestionRequestDTO)
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .post("api/question/{certificateId}");

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertEquals("message", response.get(0).getMessage(), "Expect to contain question with updated message")
        );
    }

    @Test
    @DisplayName("Shall send question for certificate")
    void shallSendQuestionForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var sendQuestionRequestDTO = new SendQuestionRequestDTO();
        Question question = Question.builder()
            .id(testSetup.questionDraftId())
            .message("message")
            .subject("subject")
            .type(QuestionType.COORDINATION)
            .build();

        sendQuestionRequestDTO.setQuestion(question);

        final var receivedQuestion = given()
            .contentType(ContentType.JSON)
            .body(sendQuestionRequestDTO)
            .pathParam("questionId", testSetup.questionDraftId())
            .expect().statusCode(200)
            .when()
            .post("api/question/{questionId}/send")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertTrue(!receivedQuestion.getId().isEmpty(), "Expect to have a question id")
        );
    }

    @Test
    @DisplayName("Shall get question with answer draft for certificate")
    void shallGetQuestionWithAnswerDraftForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionWithAnswerDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertNotNull(response.get(0).getAnswer().getMessage(), "Expect question to have an answer")
        );
    }

    @Test
    @DisplayName("Shall get question with answer for certificate")
    void shallGetQuestionWithAnswerForCertificate() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionWithAnswer()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = given()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertNotNull(response.get(0).getAnswer().getId(), "Expect question to have an answer id"),
            () -> assertNotNull(response.get(0).getAnswer().getAuthor(), "Expect question to have an answer author"),
            () -> assertNotNull(response.get(0).getAnswer().getSent(), "Expect question to have an answer sent"),
            () -> assertNotNull(response.get(0).getAnswer().getMessage(), "Expect question to have an answer message")
        );
    }

    @Test
    @DisplayName("Shall save answer for question")
    void shallSaveAnswerForQuestion() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .question()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var answerRequestDTO = new AnswerRequestDTO();
        answerRequestDTO.setMessage("Det här är vårt svar!");

        final var response = given()
            .pathParam("questionId", testSetup.questionId())
            .contentType(ContentType.JSON)
            .body(answerRequestDTO)
            .expect().statusCode(200)
            .when()
            .post("api/question/{questionId}/saveanswer")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertEquals(answerRequestDTO.getMessage(), response.getAnswer().getMessage(), "Answer should have been saved.")
        );
    }

    @Test
    @DisplayName("Shall delete answer for question")
    void shallDeleteAnswerForQuestion() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionWithAnswerDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = given()
            .pathParam("questionId", testSetup.questionId())
            .expect().statusCode(200)
            .when()
            .delete("api/question/{questionId}/answer")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertNull(response.getAnswer(), "Answer should have been deleted.")
        );
    }

    @Test
    @DisplayName("Shall send answer for question")
    void shallSendAnswerForQuestion() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .sendCertificate()
            .questionWithAnswerDraft()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var answerRequestDTO = new AnswerRequestDTO();
        answerRequestDTO.setMessage("Det här är vårt svar!");

        final var response = given()
            .pathParam("questionId", testSetup.questionId())
            .contentType(ContentType.JSON)
            .body(answerRequestDTO)
            .expect().statusCode(200)
            .when()
            .post("api/question/{questionId}/sendanswer")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertEquals(response.getAnswer().getMessage(), answerRequestDTO.getMessage(),
                "Answer should have been saved before sent."),
            () -> assertNotNull(response.getAnswer().getSent(), "Answer should have been sent.")
        );
    }

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

}
