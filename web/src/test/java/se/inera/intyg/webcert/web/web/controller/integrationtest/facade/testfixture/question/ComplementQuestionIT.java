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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.question;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.HandleQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class ComplementQuestionIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    protected abstract List<String> typeVersionList();

    protected Stream<String> typeVersionStream() {
        return typeVersionList().stream();
    }

    protected abstract Boolean shouldReturnLastestVersion();

    @Test
    @DisplayName("Shall be able to complement current version")
    void shallBeAbleToComplementCurrentVersion() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .complementQuestion()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        assertEquals(testSetup.certificateId(),
            newCertificate.getCertificate().getMetadata().getRelations().getParent().getCertificateId(),
            () -> String.format("Failed for certificate '%s'", testSetup.certificateId()));
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall return draft when complementing")
    void shallReturnCertificateWhenComplement(String typeVersion) {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion)
            .complementQuestion()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        if (shouldReturnLastestVersion()) {
            assertEquals(typeVersion(), newCertificate.getCertificate().getMetadata().getTypeVersion());
        } else {
            assertEquals(typeVersion, newCertificate.getCertificate().getMetadata().getTypeVersion());
        }
    }

    @Test
    @DisplayName("Shall pdl log crete activity when complementing a certificate")
    void shallPdlLogCreateActivityWhenComplementCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .clearPdlLogMessages()
            .complementQuestion()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        assertNumberOfPdlMessages(2);
        assertPdlLogMessage(ActivityType.CREATE, newCertificate.getCertificate().getMetadata().getId());
        assertPdlLogMessage(ActivityType.READ, newCertificate.getCertificate().getMetadata().getId());

    }

    @Test
    @DisplayName("Shall get question with complement")
    void shallGetQuestionWithComplement() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .sendCertificate()
            .complementQuestion()
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertNotNull(response.get(0).getComplements()[0].getQuestionId(),
                "Expect question to have a complement with questionId"),
            () -> assertNotNull(response.get(0).getComplements()[0].getQuestionText(), "Expect question to have a complement with text"),
            () -> assertNotNull(response.get(0).getComplements()[0].getValueId(), "Expect question to have a complement with valueId"),
            () -> assertNotNull(response.get(0).getComplements()[0].getMessage(), "Expect question to have a complement with message")
        );
    }

    @Test
    @DisplayName("Shall set complement question as handled")
    void shallSetComplementQuestionAsHandled() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .sendCertificate()
            .complementQuestion()
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var handleQuestionRequestDTO = new HandleQuestionRequestDTO();
        handleQuestionRequestDTO.setHandled(true);

        final var response = testSetup
            .spec()
            .pathParam("questionId", testSetup.questionId())
            .contentType(ContentType.JSON)
            .body(handleQuestionRequestDTO)
            .expect().statusCode(200)
            .when()
            .post("api/question/{questionId}/handle")
            .then().extract().response().as(QuestionResponseDTO.class, getObjectMapperForDeserialization()).getQuestion();

        assertAll(
            () -> assertTrue(response.isHandled(), "Question should be handled")
        );
    }

    @Disabled("This test doesn't succeed in the pipeline, but works locally. Might be caused by Intygstjansten, but when using it locally it still works")
    @Test
    @DisplayName("Shall return complement question with answered by certificate")
    void shallReturnComplementQuestionWithAnsweredByCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .sendCertificate()
            .complementQuestion()
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("");

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertEquals(newCertificate.getCertificate().getMetadata().getId(),
                response.get(0).getAnsweredByCertificate().getCertificateId(),
                () -> String.format("Failed for certificate '%s'", testSetup.certificateId()))
        );
    }

    @Test
    @DisplayName("Shall return complement question for draft answering the complement question")
    void shallReturnComplementQuestionForDraftAnsweringTheComplement() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .sendCertificate()
            .complementQuestion()
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var newCertificate = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(new ComplementCertificateRequestDTO())
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/complement")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

        certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}/complements")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertEquals(QuestionType.COMPLEMENT, response.get(0).getType(),
                "Expect the complement draft to contain the complement question")
        );
    }

    @Test
    @DisplayName("Shall return complement question with answer")
    void shallReturnComplementQuestionWithAnswer() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
            .sendCertificate()
            .complementQuestion()
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
        complementCertificateRequestDTO.setMessage("Det går inte att komplettera detta intyg");

        testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(complementCertificateRequestDTO)
            .expect().statusCode(200)
            .when().post("api/certificate/{certificateId}/answercomplement");

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/question/{certificateId}")
            .then().extract().response().as(QuestionsResponseDTO.class, getObjectMapperForDeserialization()).getQuestions();

        assertAll(
            () -> assertEquals(complementCertificateRequestDTO.getMessage(), response.get(0).getAnswer().getMessage())
        );
    }
}
