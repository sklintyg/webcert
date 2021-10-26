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
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.KFSIGN;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.MAKULE;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYSVFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.RADERA;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKICKA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_REGIONEN;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_REGIONEN_NAME;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL_NAME;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.VARDADMIN_ALVA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.VARDADMIN_ALVA_ALFA_VARDCENTRAL;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.TypeRef;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.http.HttpStatus;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.web.controller.facade.dto.AnswerRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.HandleQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

@TestInstance(Lifecycle.PER_CLASS)
public class NotificationServicesIT {

    private static final String VERSION_1_2 = "1.2";
    private static final String PATIENT_ATHENA = ATHENA_ANDERSSON.getPersonId().getId();
    private static final Integer POLL_DELAY = 1;
    private static final Integer POLL_INTERVAL = 3;
    private static final Integer WAIT_AT_MOST = 16;
    private static final Integer STATUS_UPDATE_RESULT_OK = 0;
    private static final TypeRef<List<CertificateStatusUpdateForCareType>> LIST_NOTIFICATIONS = new TypeRef<>() { };
    private static final TypeRef<List<IntegreradEnhetEntryWithSchemaVersion>> LIST_INTEGRATED_UNIT = new TypeRef<>() { };

    private ST requestTemplateAnswer;
    private ST requestTemplateQuestion;
    private ST requestTemplateCreateDraft;
    private List<String> certificateIdsToCleanAfterTest;
    private IntegreradEnhetEntryWithSchemaVersion replacedIntegratedUnit;

    @BeforeAll
    public void initiate() {
        configureRestAssured();
        setIntegratedUnit();
        resetNotificationStub();
        setNotificationStubResponseOk();
        requestTemplateAnswer = new STGroupFile("integrationtestTemplates/sendMessageToCareAnswer.v2.stg").getInstanceOf("request");
        requestTemplateQuestion = new STGroupFile("integrationtestTemplates/sendMessageToCareQuestion.v2.stg").getInstanceOf("request");
        requestTemplateCreateDraft = new STGroupFile("integrationtestTemplates/createDraftCertificateForIT.v3.stg")
            .getInstanceOf("request");
        RestAssured.reset();
    }

    @BeforeEach
    public void setupBase() {
        configureRestAssured();
        certificateIdsToCleanAfterTest = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        resetNotificationStub();
        clearCreatedEvents();
        clearCreatedCertificates();
        RestAssured.reset();
    }

    @AfterAll
    public void cleanup() {
        configureRestAssured();
        resetIntegratedUnit();
        RestAssured.reset();
    }

    @Test
    @DisplayName("Status update for create draft should have event type SKAPAT")
    public void statusUpdateForCreateDraftShouldHaveEventTypeSkapat() {
        final var createDraftCertificateData = new CreateDraftCertificateData(LisjpEntryPoint.MODULE_ID.toUpperCase(), PATIENT_ATHENA,
            DR_AJLA, ALFA_VARDCENTRAL);

        final var certificateId = given()
            .contentType(ContentType.XML).body(requestTemplateCreateDraft.add("data", createDraftCertificateData).render())
            .when().post("/services/create-draft-certificate/v3.0")
            .then().statusCode(HttpStatus.OK.value())
            .extract().response().getBody().xmlPath().get("Envelope.Body.CreateDraftCertificateResponse.intygs-id.extension").toString();

        certificateIdsToCleanAfterTest.add(certificateId);
        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(SKAPAT, certificateId);

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertNull(expectedStatusUpdates.get(0).getHanteratAv())
        );
    }

    @Test
    @DisplayName("Status update for sign draft should have event type SIGNAT")
    public void statusUpdateForSignDraftShouldHaveEventTypeSignat() {
        final var testSetup = TestSetup.create()
            .draft(LisjpEntryPoint.MODULE_ID, VERSION_1_2, CreateCertificateFillType.MINIMAL, DR_AJLA, ALFA_VARDCENTRAL, PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        given()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON).body(testSetup.certificate())
            .when().post("/api/certificate/{certificateId}/sign")
            .then().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(SIGNAT, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for draft ready to sign should have event type KFSIGN")
    public void statusUpdateForDraftReadyToSignShouldHaveEventTypeKfsign() {
        final var testSetup = TestSetup.create()
            .draft(LisjpEntryPoint.MODULE_ID, VERSION_1_2, CreateCertificateFillType.MINIMAL, VARDADMIN_ALVA, ALFA_VARDCENTRAL,
                PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(VARDADMIN_ALVA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        given()
            .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
            .pathParam("certificateId", testSetup.certificateId())
            .pathParam("certificateVersion", 0)
            .when().put("/api/intyg/{certificateType}/{certificateId}/{certificateVersion}/redoattsignera")
            .then().assertThat().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(KFSIGN, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(VARDADMIN_ALVA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for delete draft should have event type RADERA")
    public void statusUpdateForDeleteDraftShouldHaveEventTypeRadera() {
        final var testSetup = TestSetup.create()
            .draft(LisjpEntryPoint.MODULE_ID, VERSION_1_2, CreateCertificateFillType.EMPTY, DR_AJLA, ALFA_VARDCENTRAL, PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        given()
            .pathParam("certificateId", testSetup.certificateId())
            .pathParam("certificateVersion", 1)
            .when().delete("/api/certificate/{certificateId}/{certificateVersion}")
            .then().assertThat().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(RADERA, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for revoke certificate should have event type MAKULE")
    public void statusUpdateForRevokeCertificateShouldHaveEventTypeMakule() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var revokeCertificate = new RevokeCertificateRequestDTO();
        revokeCertificate.setReason("Integration test for MAKULE notification");
        revokeCertificate.setMessage("Integration test for MAKULE notification");

        given()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON).body(revokeCertificate)
            .when().post("/api/certificate/{certificateId}/revoke")
            .then().assertThat().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(MAKULE, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for send certificate should have event type SKICKA")
    public void statusUpdateForSendCertificateShouldHaveEventTypeSkicka() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        given()
            .pathParam("certificateId", testSetup.certificateId())
            .when().post("/api/certificate/{certificateId}/send")
            .then().assertThat().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(SKICKA, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for new question from care should have event type NYFRFV")
    public void statusUpdateForNewQuestionFromCareShouldHaveEventTypeNyfrfv() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .sendCertificate()
            .questionDraft()
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        sendQuestionFromCare(testSetup);
        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(NYFRFV, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getSkickadeFragor().getTotalt()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getSkickadeFragor().getEjBesvarade())
        );
    }

    @Test
    @DisplayName("Status update for new answer from recipient should have event type NYSVFM")
    public void statusUpdateForNewAnswerFromRecipientShouldHaveEventTypeNysvfm() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .sendCertificate()
            .question()
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var messageToCareData = new SendMessageToCareData(UUID.randomUUID().toString(), testSetup.certificateId(),
            PATIENT_ATHENA,
            ALFA_VARDCENTRAL, ArendeAmne.KONTKT.name(), testSetup.questionId(), null);

        given()
            .contentType(ContentType.XML).body(requestTemplateAnswer.add("data", messageToCareData).render())
            .when().post("/services/send-message-to-care/v2.0")
            .then().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(NYSVFM, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getTotalt()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getBesvarade()),
            () -> assertNull(expectedStatusUpdates.get(0).getHanteratAv())
        );
    }

    @Test
    @DisplayName("Status update for handled question from care should have event type HANFRFV")
    public void statusUpdateForHandledQuestionFromCareShouldHaveEventTypeHanfrfv() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .sendCertificate()
            .questionDraft()
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var questionResponse =  sendQuestionFromCare(testSetup);
        final var handleQuestionRequest = new HandleQuestionRequestDTO();
        handleQuestionRequest.setHandled(true);

        given()
            .pathParam("questionId", questionResponse.getQuestion().getId())
            .contentType(ContentType.JSON).body(handleQuestionRequest)
            .when().post("/api/question/{questionId}/handle")
            .then().statusCode(HttpStatus.OK.value());

        waitForMessages(2);

        final var expectedStatusUpdates = getStatusUpdatesOfType(HANFRFV, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getSkickadeFragor().getTotalt()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getSkickadeFragor().getHanterade()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    @Test
    @DisplayName("Status update for new question from recipient should have event type NYFRFM")
    public void statusUpdateForNewQuestionFromRecipientShouldHaveEventTypeNyfrfm() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var lastDateForResponse = LocalDateTime.now().plusDays(1).toLocalDate().toString();
        final var messageToCareData = new SendMessageToCareData(UUID.randomUUID().toString(), testSetup.certificateId(), PATIENT_ATHENA,
            ALFA_VARDCENTRAL, ArendeAmne.KONTKT.name(), null, lastDateForResponse);

        given()
            .contentType(ContentType.XML).body(requestTemplateQuestion.add("data", messageToCareData).render())
            .when().post("/services/send-message-to-care/v2.0")
            .then().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(NYFRFM, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getTotalt()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getEjBesvarade()),
            () -> assertEquals(lastDateForResponse, expectedStatusUpdates.get(0).getHandelse().getSistaDatumForSvar().toString()),
            () -> assertNull(expectedStatusUpdates.get(0).getHanteratAv())
        );
    }

    @Test
    @DisplayName("Status update for answered question from recipient should have event type HANFRFM")
    public void statusUpdateForAnsweredQuestionFromRecipientShouldHaveEventTypeHanfrfm() {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION_1_2, ALFA_VARDCENTRAL, DR_AJLA, PATIENT_ATHENA)
            .sendCertificate()
            .questionWithAnswerDraft()
            .useDjupIntegratedOrigin()
            .clearNotificationStub()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var answerRequest = new AnswerRequestDTO();
        answerRequest.setMessage("This is an answer to question from recipient.");

        given()
            .pathParam("questionId", testSetup.questionId())
            .contentType(ContentType.JSON).body(answerRequest)
            .when().post("/api/question/{questionId}/sendanswer")
            .then().statusCode(HttpStatus.OK.value());

        waitForMessages(1);

        final var expectedStatusUpdates = getStatusUpdatesOfType(HANFRFM, testSetup.certificateId());

        assertAll(
            () -> assertEquals(1, expectedStatusUpdates.size()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getTotalt()),
            () -> assertEquals(1, expectedStatusUpdates.get(0).getMottagnaFragor().getHanterade()),
            () -> assertEquals(DR_AJLA, expectedStatusUpdates.get(0).getHanteratAv().getExtension())
        );
    }

    private void configureRestAssured() {
        final var logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.objectMapper(new Jackson2Mapper(((type, charset) -> new CustomObjectMapper())));
        RestAssured.config = RestAssured.config()
            .logConfig(logConfig)
            .sessionConfig(new SessionConfig("SESSION", null));
    }

    private List<CertificateStatusUpdateForCareType> getStatusUpdatesOfType(HandelsekodEnum eventEnum, String certificateId) {
        final var statusUpdates = given()
            .when().get("/services/api/notification-api/notifieringar/v3")
            .then().statusCode(HttpStatus.OK.value())
            .extract().response().getBody().as(LIST_NOTIFICATIONS);

        return statusUpdates.stream().filter(statusUpdate -> statusUpdate.getIntyg().getIntygsId().getExtension().equals(certificateId)
            && statusUpdate.getHandelse().getHandelsekod().getCode().equals(eventEnum.value())).collect(Collectors.toList());
    }

    private QuestionResponseDTO sendQuestionFromCare(TestSetup testSetup) {
        final var sendQuestionRequestDTO = new SendQuestionRequestDTO();
        Question question = Question.builder()
            .id(testSetup.questionDraftId())
            .message("Test question message")
            .subject("Test question subject")
            .type(QuestionType.COORDINATION)
            .build();

        sendQuestionRequestDTO.setQuestion(question);

        return given()
            .pathParam("questionId", testSetup.questionDraftId())
            .contentType(ContentType.JSON).body(sendQuestionRequestDTO)
            .when().post("/api/question/{questionId}/send")
            .then().statusCode(HttpStatus.OK.value())
            .extract().body().as(QuestionResponseDTO.class);
    }

    private void clearCreatedEvents() {
        certificateIdsToCleanAfterTest.forEach(certificateId -> given()
            .pathParam("certificateId", certificateId)
            .when().delete("/testability/intyg/handelser/{certificateId}")
            .then().statusCode(HttpStatus.OK.value())
        );
    }

    private void clearCreatedCertificates() {
        certificateIdsToCleanAfterTest.forEach(certificateId ->
            given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(HttpStatus.OK.value())
                .when()
                .delete("/testability/intyg/{certificateId}")
        );
    }

    private void waitForMessages(int expectedNumberOfMessages) {
        with()
            .pollDelay(Duration.ofSeconds(POLL_DELAY)).and().pollInterval(Duration.ofSeconds(POLL_INTERVAL))
            .await().atMost(Duration.ofSeconds(WAIT_AT_MOST)).until(messagesArrived(expectedNumberOfMessages));
    }

    private Callable<Boolean> messagesArrived(int expectedNumberOfMessages) {
        return () -> given()
            .when().get("/services/api/notification-api/notifieringar/v3")
            .then().statusCode(HttpStatus.OK.value())
            .extract().response().getBody().as(LIST_NOTIFICATIONS).size() >= expectedNumberOfMessages;
    }

    private void setNotificationStubResponseOk() {
        given().pathParam("code", STATUS_UPDATE_RESULT_OK)
            .when().get("/services/api/notification-api/notifieringar/v3/emulateError/{code}")
            .then().statusCode(HttpStatus.OK.value());
    }

    private void resetNotificationStub() {
        given()
            .when().post("/services/api/notification-api/clear")
            .then().statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void setIntegratedUnit() {
        replacedIntegratedUnit = given()
            .when().get("/testability/integreradevardenheter")
            .then().statusCode(HttpStatus.OK.value()).extract().body().as(LIST_INTEGRATED_UNIT)
            .stream().filter(unit -> unit.getEnhetsId().equals(ALFA_VARDCENTRAL)).findFirst().orElse(null);

        if (replacedIntegratedUnit != null) {
            deleteCurrentIntegratedUnit();
        }

        given()
            .contentType(ContentType.JSON).body(getIntegreradUnitAlfa())
            .when().post("/testability/integreradevardenheter")
            .then().statusCode(HttpStatus.OK.value());
    }

    private void resetIntegratedUnit() {
        deleteCurrentIntegratedUnit();
        if (replacedIntegratedUnit != null) {
            given().contentType(ContentType.JSON).body(replacedIntegratedUnit)
                .when().post("/testability/integreradevardenheter")
                .then().statusCode(HttpStatus.OK.value());
        }
    }

    private void deleteCurrentIntegratedUnit() {
        given().pathParam("unitId", ALFA_VARDCENTRAL)
            .when().delete("/testability/integreradevardenheter/{unitId}")
            .then().statusCode(HttpStatus.OK.value());
    }

    private IntegreradEnhetEntryWithSchemaVersion getIntegreradUnitAlfa() {
        final var integratedUnit = new IntegreradEnhetEntryWithSchemaVersion();
        integratedUnit.setEnhetsId(ALFA_VARDCENTRAL);
        integratedUnit.setEnhetsNamn(ALFA_VARDCENTRAL_NAME);
        integratedUnit.setVardgivareId(ALFA_REGIONEN);
        integratedUnit.setVardgivareNamn(ALFA_REGIONEN_NAME);
        integratedUnit.setSchemaVersion("2.0");
        return integratedUnit;
    }

    private static final class CreateDraftCertificateData {
        public final String certificateType;
        public final String patientId;
        public final String carePersonId;
        public final String unitId;

        CreateDraftCertificateData(String certificateType, String patientId, String carePersonId, String unitId) {
            this.certificateType = certificateType;
            this.patientId = patientId;
            this.carePersonId = carePersonId;
            this.unitId = unitId;
        }
    }

    private static final class SendMessageToCareData {
        public final String messageId;
        public final String sentTime;
        public final String certificateId;
        public final String patientId;
        public final String unitId;
        public final String subject;
        public final String responseToMessageId;
        public final String lastDateForResponse;

        SendMessageToCareData(String messageId, String certificateId, String patientId, String unitId, String subject,
            String responseToMessageId, String lastDateForResponse) {
            this.messageId = messageId;
            this.sentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
            this.certificateId = certificateId;
            this.patientId = patientId;
            this.unitId = unitId;
            this.subject = subject;
            this.responseToMessageId = responseToMessageId;
            this.lastDateForResponse = lastDateForResponse;
        }
    }
}
