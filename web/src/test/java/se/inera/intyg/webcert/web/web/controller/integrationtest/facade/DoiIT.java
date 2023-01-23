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
package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;

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

            final var certificateId = createDoiDraft();

            final var response = getTestDraft(certificateId);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void shallCreateDraftWithData() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = createDoiDraft();

            final var response = getTestDraft(certificateId);

            assertTrue(response.getData().size() > 0, "Expect draft to include data");
        }

        @Test
        void shallSaveDraftWithData() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = createDoiDraft();

            final var certificate = getTestDraft(certificateId);

            final var unit = certificate.getMetadata().getUnit();
            certificate.getMetadata().setUnit(
                Unit.builder()
                    .unitId(unit.getUnitId())
                    .unitName(unit.getUnitName())
                    .build()
            );

            final var response = given()
                .pathParam("certificateId", certificate.getMetadata().getId())
                .contentType(ContentType.JSON)
                .body(certificate)
                .expect().statusCode(200)
                .when()
                .put("api/certificate/{certificateId}")
                .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertTrue(response.getVersion() > certificate.getMetadata().getVersion(),
                "Expect version after save to be incremented");
            ;
        }

        @Test
        void shallValidateDraft() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = createDoiDraft();

            final var response = getTestDraft(certificateId);

            final var validation = given()
                .pathParam("certificateId", certificateId)
                .contentType(ContentType.JSON)
                .body(response)
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateId}/validate")
                .then().extract().response().as(ValidateCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertTrue(validation.getValidationErrors().length > 0, "Expect draft to include validationsErrors");
        }

        @Test
        void shallCreateDraftWithResourceLinks() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = createDoiDraft();

            final var response = getTestDraft(certificateId);

            assertTrue(response.getLinks().length > 0, "Expect draft to include resourceLinks");
        }

        private CertificateDTO getTestDraft(String certificateId) {
            return given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }

        private String createDoiDraft() {
            final var certificateId = given()
                .pathParam("certificateType", DoiModuleEntryPoint.MODULE_ID)
                .pathParam("patientId", ATHENA_ANDERSSON.getPersonId().getId())
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateType}/{patientId}")
                .then().extract().path("certificateId").toString();
            certificateIdsToCleanAfterTest.add(certificateId);
            return certificateId;
        }
    }
}
