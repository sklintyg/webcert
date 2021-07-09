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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;

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

//        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

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

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

}
