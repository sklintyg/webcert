/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;

public class DoiIT {

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

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

    @Nested
    class Draft {

        @Test
        void shallCreateDoiDraft() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = given()
                .pathParam("certificateType", DoiModuleEntryPoint.MODULE_ID)
                .pathParam("patientId", ATHENA_ANDERSSON.getPersonId().getId())
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateType}/{patientId}")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getTestDraft(certificateId);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        private CertificateDTO getTestDraft(String certificateId) {
            return given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }
    }
}
