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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class Ag114V1IT {

    private static final String CURRENT_VERSION = "1.0";
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
        void draftShouldContainData() {
            final var certificateId = TestSetup.create()
                .draft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertTrue(response.getData().size() > 0, "Expect draft to include data");
        }

        @Test
        void draftShouldContainMetaData() {
            final var certificateId = TestSetup.create()
                .draft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void draftShouldContainResourceLinks() {
            final var certificateId = TestSetup.create()
                .draft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertTrue(response.getLinks().length > 0, "Expect draft to include resourceLinks");
        }

        @Test
        void shallBeAbleToSaveDraft() {
            final var certificateId = TestSetup.create()
                .draft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var certificate = getCertificate(certificateId);

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
        public void shallBeAbleToDeleteDraft() {
            final var certificateId = TestSetup.create()
                .draft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .pathParam("version", 1)
                .when()
                .delete("api/certificate/{certificateId}/{version}")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }

        @Nested
        class Validation {

            @Test
            void shallValidateEmptyDraftAndReturnValidationErrors() {
                final var certificateId = TestSetup.create()
                    .draft(
                        Ag114EntryPoint.MODULE_ID,
                        CURRENT_VERSION,
                        CreateCertificateFillType.EMPTY,
                        DR_AJLA,
                        ALFA_VARDCENTRAL,
                        ATHENA_ANDERSSON.getPersonId().getId()
                    )
                    .login(DR_AJLA_ALFA_VARDCENTRAL)
                    .setup()
                    .certificateId();

                certificateIdsToCleanAfterTest.add(certificateId);

                final var response = getCertificate(certificateId);

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
            void shallValidateMinimalDraftAndReturnNoValidationErrors() {
                final var certificateId = TestSetup.create()
                    .draft(
                        Ag114EntryPoint.MODULE_ID,
                        CURRENT_VERSION,
                        CreateCertificateFillType.MINIMAL,
                        DR_AJLA,
                        ALFA_VARDCENTRAL,
                        ATHENA_ANDERSSON.getPersonId().getId()
                    )
                    .login(DR_AJLA_ALFA_VARDCENTRAL)
                    .setup()
                    .certificateId();

                certificateIdsToCleanAfterTest.add(certificateId);

                final var response = getCertificate(certificateId);

                final var validation = given()
                    .pathParam("certificateId", certificateId)
                    .contentType(ContentType.JSON)
                    .body(response)
                    .expect().statusCode(200)
                    .when()
                    .post("api/certificate/{certificateId}/validate")
                    .then().extract().response().as(ValidateCertificateResponseDTO.class, getObjectMapperForDeserialization());

                assertEquals(0, validation.getValidationErrors().length, "Expect draft to not include validationsErrors");
            }
        }
    }

    @Nested
    class Certificate {

        @Test
        void certificateShouldContainData() {
            final var certificateId = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertTrue(response.getData().size() > 0, "Expect draft to include data");
        }

        @Test
        void certificateShouldContainMetaData() {
            final var certificateId = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void certificateShouldContainResourceLinks() {
            final var certificateId = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup()
                .certificateId();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getCertificate(certificateId);

            assertTrue(response.getLinks().length > 0, "Expect draft to include resourceLinks");
        }

        @Test
        public void shallBeAbleToPrintCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }

        @Test
        void shallReturnCertificateOfCurrentVersionAfterReplace() {

            final var testSetup = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();
            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var newCertificateRequestDTO = new NewCertificateRequestDTO();
            newCertificateRequestDTO.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
            newCertificateRequestDTO.setCertificateType(testSetup.certificate().getMetadata().getType());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/replace")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion());

        }

        @Test
        public void shallBeAbleToRevokeCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
            revokeCertificateRequest.setReason("Reason");
            revokeCertificateRequest.setMessage("Message");

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }

        @Test
        void shallNotBeAbleToCreateCertificateFromTemplateCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(500)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().statusCode();

            assertEquals(response, 500);
        }
    }

    @Nested
    class LockedCertificate {

        @Test
        void shallBeAbleToCopyCurrentVersion() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var newCertificateRequestDTO = new NewCertificateRequestDTO();
            newCertificateRequestDTO.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
            newCertificateRequestDTO.setCertificateType(testSetup.certificate().getMetadata().getType());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/copy")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertNotNull(certificateId, "Expect certificate id to have a value");
        }

        @Test
        public void shallBeAbleToRevokeLockedCertificate() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    Ag114EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
            revokeCertificateRequest.setReason("Reason");
            revokeCertificateRequest.setMessage("Message");

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }
    }

    private CertificateDTO getCertificate(String certificateId) {
        return given()
            .pathParam("certificateId", certificateId)
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
    }
}
