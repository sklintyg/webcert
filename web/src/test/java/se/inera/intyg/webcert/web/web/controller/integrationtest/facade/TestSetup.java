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
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import io.restassured.response.Response;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.common.FakeCredential;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionRequestDTO;

public class TestSetup {

    private final String certificateId;
    private final Certificate certificate;
    private final String routeId;
    private final String csrfToken;
    private final String questionId;
    private final String questionDraftId;

    public static TestSetupBuilder create() {
        return new TestSetupBuilder();
    }

    private TestSetup(String certificateId, Certificate certificate, String routeId,
        String csrfToken, String questionId, String questionDraftId) {
        this.certificateId = certificateId;
        this.certificate = certificate;
        this.routeId = routeId;
        this.csrfToken = csrfToken;
        this.questionId = questionId;
        this.questionDraftId = questionDraftId;
    }

    public String certificateId() {
        return certificateId;
    }

    public Certificate certificate() {
        return certificate;
    }

    public String questionId() {
        return questionId;
    }

    public String questionDraftId() {
        return questionDraftId;
    }

    public String routeId() {
        return routeId;
    }

    public String csrfToken() {
        return csrfToken;
    }

    public static class TestSetupBuilder {

        private String certificateType;
        private String certificateTypeVersion;
        private CreateCertificateFillType fillType;
        private Map<String, CertificateDataValue> values;
        private String patientId;
        private String personId;
        private String unitId;
        private boolean isSent;
        private CertificateStatus status;
        private boolean clearPdlLogMessages;
        private boolean createCertificate;
        private FakeCredential credentials;
        private String origin;
        private boolean sjf;
        private boolean clearNotificationStub;

        private String certificateId;
        private String questionId;
        private String questionDraftId;
        private Certificate certificate;
        private String routeId;
        private String csrfToken;

        private final CustomObjectMapper objectMapper = new CustomObjectMapper();
        private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
        private static final String FAKE_LOGIN_URI = "/fake";

        private boolean createQuestion;
        private boolean createQuestionDraft;
        private boolean createAnswerDraft;
        private boolean createAnswer;
        private boolean createReminder;
        private QuestionType createQuestionType;

        private TestSetupBuilder() {

        }

        public TestSetupBuilder draft(String certificateType, String certificateTypeVersion, CreateCertificateFillType fillType,
            String personId, String unitId, String patientId) {
            this.createCertificate = true;
            this.status = CertificateStatus.UNSIGNED;
            this.fillType = fillType;
            this.values = Collections.emptyMap();
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            this.isSent = false;
            return this;
        }

        public TestSetupBuilder lockedDraft(String certificateType, String certificateTypeVersion, String personId, String unitId,
            String patientId) {
            this.createCertificate = true;
            this.status = CertificateStatus.LOCKED;
            this.fillType = CreateCertificateFillType.EMPTY;
            this.values = Collections.emptyMap();
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            this.isSent = false;
            return this;
        }

        public TestSetupBuilder certificate(String certificateType, String certificateTypeVersion, String unitId, String personId,
            String patientId) {
            this.createCertificate = true;
            this.status = CertificateStatus.SIGNED;
            this.fillType = CreateCertificateFillType.MINIMAL;
            this.values = Collections.emptyMap();
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            return this;
        }

        public TestSetupBuilder draftWithValues(String certificateType, String certificateTypeVersion,
            String personId, String unitId, String patientId, Map<String, CertificateDataValue> valueMap) {
            this.createCertificate = true;
            this.status = CertificateStatus.UNSIGNED;
            this.fillType = CreateCertificateFillType.WITH_VALUES;
            this.values = valueMap;
            this.certificateType = certificateType;
            this.certificateTypeVersion = certificateTypeVersion;
            this.patientId = patientId;
            this.personId = personId;
            this.unitId = unitId;
            this.isSent = false;
            return this;
        }


        public TestSetupBuilder sendCertificate() {
            this.isSent = true;
            return this;
        }

        public TestSetupBuilder question() {
            this.createQuestion = true;
            return this;
        }

        public TestSetupBuilder complementQuestion() {
            this.createQuestion = true;
            this.createQuestionType = QuestionType.COMPLEMENT;
            return this;
        }

        public TestSetupBuilder reminder() {
            this.createReminder = true;
            return this;
        }

        public TestSetupBuilder questionWithAnswer() {
            this.createQuestion = true;
            this.createAnswer = true;
            return this;
        }

        public TestSetupBuilder questionWithAnswerDraft() {
            this.createQuestion = true;
            this.createAnswerDraft = true;
            return this;
        }

        public TestSetupBuilder questionDraft() {
            this.createQuestionDraft = true;
            return this;
        }

        public TestSetupBuilder clearPdlLogMessages() {
            this.clearPdlLogMessages = true;
            return this;
        }

        public TestSetupBuilder login(FakeCredential credentials) {
            this.credentials = credentials;
            return this;
        }

        public TestSetupBuilder useDjupIntegratedOrigin() {
            this.origin = "DJUPINTEGRATION";
            return this;
        }

        public TestSetupBuilder sjf() {
            this.sjf = true;
            return this;
        }

        public TestSetupBuilder clearNotificationStub() {
            this.clearNotificationStub = true;
            return this;
        }

        public TestSetup setup() {
            if (createCertificate) {
                certificateId = createCertificate();
            }

            if (createQuestion) {
                questionId = createQuestion(certificateId);
            }

            if (createQuestionDraft) {
                questionDraftId = createQuestionDraft(certificateId);
            }

            if (credentials != null) {
                RestAssured.sessionId = getAuthSession(credentials);
            }

            if (origin != null) {
                changeOriginTo(origin);
            }

            if (sjf) {
                setSjf();
            }

            if (certificateId != null) {
                certificate = getCertificate(certificateId);
            }

            if (clearNotificationStub) {
                resetNotificationStub();
            }

            if (clearPdlLogMessages) {
                deletePdlLogMessagesFromQueue();
            }

            return new TestSetup(certificateId, certificate, routeId, csrfToken, questionId, questionDraftId);
        }

        private void deletePdlLogMessagesFromQueue() {
            given().when().delete("testability/logMessages");
        }

        private String createCertificate() {
            final var certificateRequest = new CreateCertificateRequestDTO();
            certificateRequest.setCertificateType(certificateType);
            certificateRequest.setCertificateTypeVersion(certificateTypeVersion);
            certificateRequest.setPatientId(patientId);
            certificateRequest.setPersonId(personId);
            certificateRequest.setUnitId(unitId);
            certificateRequest.setStatus(status);
            certificateRequest.setFillType(fillType);
            certificateRequest.setValues(values);
            certificateRequest.setSent(isSent);

            return given()
                .contentType(ContentType.JSON)
                .body(certificateRequest)
                .expect().statusCode(200)
                .when().post("testability/certificate")
                .then().extract().path("certificateId").toString();
        }

        private String createQuestion(String certificateId) {
            final var questionRequest = new CreateQuestionRequestDTO();
            questionRequest.setType(createQuestionType != null ? createQuestionType : QuestionType.COORDINATION);
            questionRequest.setMessage("Det här är ett meddelande!");
            questionRequest.setReminded(createReminder);

            if (createAnswer || createAnswerDraft) {
                questionRequest.setAnswer("Det här är ett svar!");
                questionRequest.setAnswerAsDraft(createAnswerDraft);
            }

            return given()
                .pathParam("certificateId", certificateId)
                .contentType(ContentType.JSON)
                .body(questionRequest)
                .expect().statusCode(200)
                .when().post("testability/certificate/{certificateId}/question")
                .then().extract().path("questionId").toString();
        }

        private String createQuestionDraft(String certificateId) {
            final var questionRequest = new CreateQuestionRequestDTO();
            questionRequest.setType(QuestionType.COORDINATION);
            questionRequest.setMessage("Det här är ett meddelande!");

            return given()
                .pathParam("certificateId", certificateId)
                .contentType(ContentType.JSON)
                .body(questionRequest)
                .expect().statusCode(200)
                .when().post("testability/certificate/{certificateId}/questionDraft")
                .then().extract().path("questionId").toString();
        }

        protected String getAuthSession(FakeCredential fakeCredential) {
            try {
                return getAuthSession(objectMapper.writeValueAsString(fakeCredential));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        private String getAuthSession(String credentialsJson) {
            Response response = given().contentType(ContentType.URLENC).and().redirects().follow(false).and()
                .formParam(USER_JSON_FORM_PARAMETER, credentialsJson)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().post(FAKE_LOGIN_URI)
                .then().extract().response();

            assertNotNull(response.sessionId());
            routeId = response.getCookie("ROUTEID") != null ? response.getCookie("ROUTEID") : "nah";
            csrfToken = response.getCookie("XSRF-TOKEN");

            return response.sessionId();
        }

        private ObjectMapper getObjectMapperForDeserialization() {
            return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
        }

        private Certificate getCertificate(String certificateId) {
            final var certificateDTO = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            final var certificate = new Certificate();
            certificate.setMetadata(certificateDTO.getMetadata());
            certificate.setData(certificateDTO.getData());
            return certificate;
        }

        protected void setSjf() {
            given()
                .expect()
                .statusCode(200)
                .when()
                .post("authtestability/user/parameters/sjf");
        }

        private void changeOriginTo(String newOrigin) {
            given()
                .pathParam("origin", newOrigin)
                .expect().statusCode(200)
                .when().get("authtestability/user/origin/{origin}");
        }

        private void resetNotificationStub() {
            given()
                .when().post("/services/api/notification-api/clear")
                .then().statusCode(HttpStatus.NO_CONTENT.value());
        }
    }
}
