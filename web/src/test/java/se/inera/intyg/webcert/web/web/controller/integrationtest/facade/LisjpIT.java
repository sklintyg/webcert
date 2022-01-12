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
import static org.junit.jupiter.api.Assertions.assertAll;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class LisjpIT {

    private static final String CURRENT_VERSION = "1.3";
    private static final String AG7804_CURRENT_VERSION = "1.2";

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
    class LisjpITPreviousVersions {

        @Test
        @DisplayName("Shall return certificate with version 1.0")
        void shallReturnCertificateOfVersion10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(testSetup.certificateId(), response.getMetadata().getId()),
                () -> assertEquals("1.0", response.getMetadata().getTypeVersion()),
                () -> assertEquals(CertificateStatus.SIGNED, response.getMetadata().getStatus()),
                () -> assertNotNull(response.getData(), "Expect certificate to include data"),
                () -> assertNotNull(response.getLinks(), "Expect certificate to include links")
            );
        }

        @Test
        @DisplayName("Shall be able to print draft with version 1.0")
        public void shallBeAbleToPrintCertificateOfVersion10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when renewing 1.0")
        void shallReturnCertificateOfCurrentVersionWhenRenewing10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when creating from template of 1.0")
        void shallReturnCertificateOfCurrentVersionWhenCreatingFromTemplate10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(AG7804_CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when replace 1.0")
        void shallReturnCertificateOfCurrentVersionWhenReplace10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
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

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when complementing 1.0")
        void shallReturnCertificateOfCurrentVersionWhenComplement10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .complementQuestion()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
            complementCertificateRequestDTO.setMessage("");

            final var newCertificate = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(complementCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/complement")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

            certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

            assertAll(
                () -> assertEquals(CURRENT_VERSION, newCertificate.getCertificate().getMetadata().getTypeVersion(),
                    () -> String.format("Failed for certificate '%s'", testSetup.certificateId()))
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when copy locked draft 1.0")
        void shallReturnCertificateOfCurrentVersionWhenCopy10() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.0",
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

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return certificate with version 1.1")
        void shallReturnCertificateOfVersion11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(testSetup.certificateId(), response.getMetadata().getId()),
                () -> assertEquals("1.1", response.getMetadata().getTypeVersion()),
                () -> assertNotNull(response.getData(), "Expect certificate to include data"),
                () -> assertNotNull(response.getLinks(), "Expect certificate to include links")
            );
        }

        @Test
        @DisplayName("Shall be able to print draft with version 1.1")
        public void shallBeAbleToPrintCertificateOfVersion11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when renewing 1.1")
        void shallReturnCertificateOfCurrentVersionWhenRenewing11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when replace 1.1")
        void shallReturnCertificateOfCurrentVersionWhenReplace11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
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

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when complementing 1.1")
        void shallReturnCertificateOfCurrentVersionWhenComplement11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .complementQuestion()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
            complementCertificateRequestDTO.setMessage("");

            final var newCertificate = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(complementCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/complement")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

            certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

            assertAll(
                () -> assertEquals(CURRENT_VERSION, newCertificate.getCertificate().getMetadata().getTypeVersion(),
                    () -> String.format("Failed for certificate '%s'", testSetup.certificateId()))
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when copy locked draft 1.1")
        void shallReturnCertificateOfCurrentVersionWhenCopy11() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
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

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when creating from template of version 1.1")
        void shallReturnCertificateOfCurrentVersionWhenCreatingFromTemplate11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(AG7804_CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return certificate with version 1.2")
        void shallReturnCertificateOfVersion12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(testSetup.certificateId(), response.getMetadata().getId()),
                () -> assertEquals("1.2", response.getMetadata().getTypeVersion()),
                () -> assertNotNull(response.getData(), "Expect certificate to include data"),
                () -> assertNotNull(response.getLinks(), "Expect certificate to include links")
            );
        }

        @Test
        @DisplayName("Shall be able to print draft with version 1.2")
        public void shallBeAbleToPrintCertificateOfVersion12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when renewing 1.2")
        void shallReturnCertificateOfCurrentVersionWhenRenewing12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when replace 1.2")
        void shallReturnCertificateOfCurrentVersionWhenReplace12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
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

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when complementing 1.2")
        void shallReturnCertificateOfCurrentVersionWhenComplement12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .complementQuestion()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
            complementCertificateRequestDTO.setMessage("");

            final var newCertificate = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(complementCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/complement")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

            certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

            assertAll(
                () -> assertEquals(CURRENT_VERSION, newCertificate.getCertificate().getMetadata().getTypeVersion(),
                    () -> String.format("Failed for certificate '%s'", testSetup.certificateId()))
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when copy locked draft 1.2")
        void shallReturnCertificateOfCurrentVersionWhenCopy12() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
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

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }

        @Test
        @DisplayName("Shall return draft with current version when creating from template of version 1.2")
        void shallReturnCertificateOfCurrentVersionWhenCreatingFromTemplate12() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(AG7804_CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }
    }

    @Nested
    class LisjpITCurrentVersion {

        @Test
        @DisplayName("Shall create draft with version current version")
        public void shallCreateDraftOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var createUtkastRequest = new CreateUtkastRequest();
            createUtkastRequest.setIntygType(LisjpEntryPoint.MODULE_ID);
            createUtkastRequest
                .setPatientPersonnummer(Personnummer.createPersonnummer(ATHENA_ANDERSSON.getPersonId().getId()).orElseThrow());
            createUtkastRequest.setPatientFornamn(ATHENA_ANDERSSON.getFirstName());
            createUtkastRequest.setPatientEfternamn(ATHENA_ANDERSSON.getLastName());

            final var certificateId = given()
                .pathParam("certificateType", LisjpEntryPoint.MODULE_ID)
                .contentType(ContentType.JSON)
                .body(createUtkastRequest)
                .expect().statusCode(200)
                .when()
                .post("api/utkast/{certificateType}")
                .then().extract().path("intygsId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertAll(
                () -> assertTrue(certificateId != null && certificateId.trim().length() > 0, "Expected id of the created certificate")
            );
        }

        @Test
        @DisplayName("Shall return draft with current version")
        void shallReturnDraftOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(testSetup.certificateId(), response.getMetadata().getId()),
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion()),
                () -> assertEquals(CertificateStatus.UNSIGNED, response.getMetadata().getStatus()),
                () -> assertNotNull(response.getData(), "Expect certificate to include data"),
                () -> assertNotNull(response.getLinks(), "Expect certificate to include links")
            );
        }

        @Test
        @DisplayName("Shall return draft with FMB warning")
        void shallReturnDraftWithFMBWarning() {
            final var icd10Codes = new String[]{"F500"};
            final var diagnosisValue = getValueDiagnosisList();
            final var sickLeaveValue = getValueDateRangeList();
            final var valueMap = getValueMap(diagnosisValue, sickLeaveValue);
            final ValidateSickLeavePeriodRequestDTO validateSickLeavePeriodRequest = getValidateSickLeavePeriodRequest(
                icd10Codes, sickLeaveValue);

            final var testSetup = TestSetup.create()
                .draftWithValues(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    valueMap
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .contentType(ContentType.JSON)
                .body(validateSickLeavePeriodRequest)
                .expect().statusCode(200)
                .when()
                .post("api/fmb/validateSickLeavePeriod")
                .then().extract().response().as(ValidateSickLeavePeriodResponseDTO.class, getObjectMapperForDeserialization());

            assertAll(
                () -> assertTrue(response.getMessage().length() > 0)
            );
        }

        @Test
        @DisplayName("Shall return validation errors for draft with current version")
        public void shallReturnValidationErrors() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateId}/validate")
                .then().extract().response().as(ValidateCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertAll(
                () -> assertNotNull(response.getValidationErrors(), "Expect response to include validation errors")
            );
        }

        @Test
        @DisplayName("Shall return new version (revision) when saving draft with current version")
        public void shallReturnNewVersionWhenSaving() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .expect().statusCode(200)
                .when()
                .put("api/certificate/{certificateId}")
                .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertAll(
                () -> assertTrue(response.getVersion() > testSetup.certificate().getMetadata().getVersion(),
                    "Expect version after save to be incremented")
            );
        }

        @Test
        @DisplayName("Shall be able to print draft with current version")
        public void shallBeAbleToPrintCurrentVersion() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.MINIMAL,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall be able to delete draft with current version")
        public void shallBeAbleToDeleteCurrentVersion() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.MINIMAL,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("version", 1)
                .when()
                .delete("api/certificate/{certificateId}/{version}")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall be able to renew certificate with current version")
        void shallBeAbleToRenewCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertAll(
                () -> assertNotNull(certificateId, "Expect certificate id to have a value")
            );
        }

        @Test
        @DisplayName("Shall be able to replace certificate with current version")
        void shallBeAbleToReplaceCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
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

            assertAll(
                () -> assertNotNull(certificateId, "Expect certificate id to have a value")
            );
        }

        @Test
        @DisplayName("Shall be able to copy locked draft with current version")
        void shallBeAbleToCopyCurrentVersion() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LisjpEntryPoint.MODULE_ID,
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

            assertAll(
                () -> assertNotNull(certificateId, "Expect certificate id to have a value")
            );
        }

        @Test
        @DisplayName("Shall be able to create certificate from template with current version")
        void shallBeAbleToCreateCertificateFromTemplateCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertAll(
                () -> assertNotNull(certificateId, "Expect certificate id to have a value")
            );
        }

        @Test
        @DisplayName("Shall be able to complement certificate with current version")
        void shallBeAbleToComplementCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .complementQuestion()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var complementCertificateRequestDTO = new ComplementCertificateRequestDTO();
            complementCertificateRequestDTO.setMessage("");

            final var newCertificate = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(complementCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/complement")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization());

            certificateIdsToCleanAfterTest.add(newCertificate.getCertificate().getMetadata().getId());

            assertAll(
                () -> assertEquals(testSetup.certificateId(),
                    newCertificate.getCertificate().getMetadata().getRelations().getParent().getCertificateId(),
                    () -> String.format("Failed for certificate '%s'", testSetup.certificateId()))
            );
        }

        @Test
        @DisplayName("Shall be able to revoke certificate with current version")
        public void shallBeAbleToRevokeCertificateOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
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

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall be able to revoke locked draft with current version")
        public void shallBeAbleToRevokeLockedDraftOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
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

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall be able to send certificate with current version")
        public void shallBeAbleToSendCertificateOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .when().post("api/certificate/{certificateId}/send")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }

        @Test
        @DisplayName("Shall return icf request with icf codes")
        void shallReturnIcfRequestWithIcfCodes() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var icd10Codes = new String[]{"A02"};
            final IcfRequestDTO icfRequestDTO = new IcfRequestDTO();
            icfRequestDTO.setIcdCodes(icd10Codes);

            final var response = given()
                .contentType(ContentType.JSON)
                .body(icfRequestDTO)
                .expect().statusCode(200)
                .when()
                .post("api/icf")
                .then().extract().response().as(IcfResponseDTO.class, getObjectMapperForDeserialization());

            assertAll(
                () -> assertTrue(response.getActivityLimitation().getUniqueCodes().size() > 0),
                () -> assertTrue(response.getDisability().getUniqueCodes().size() > 0)
            );
        }
    }

    private ValidateSickLeavePeriodRequestDTO getValidateSickLeavePeriodRequest(String[] icd10Codes,
        CertificateDataValueDateRangeList sickLeaveValue) {
        final var validateSickLeavePeriodRequest = new ValidateSickLeavePeriodRequestDTO();
        validateSickLeavePeriodRequest.setPersonId(ATHENA_ANDERSSON.getPersonId().getId());
        validateSickLeavePeriodRequest.setIcd10Codes(icd10Codes);
        validateSickLeavePeriodRequest.setDateRangeList(sickLeaveValue);
        return validateSickLeavePeriodRequest;
    }

    private CertificateDataValueDiagnosisList getValueDiagnosisList() {
        var diagnosisValue = CertificateDataValueDiagnosisList.builder()
            .list(Arrays.asList(
                CertificateDataValueDiagnosis.builder()
                    .code("F500")
                    .build()
                )
            )
            .build();
        return diagnosisValue;
    }

    private CertificateDataValueDateRangeList getValueDateRangeList() {
        var sickLeaveValue = CertificateDataValueDateRangeList.builder()
            .list(Arrays.asList(
                CertificateDataValueDateRange.builder()
                    .id("HALFTEN")
                    .from(LocalDate.now())
                    .to(LocalDate.now().plusYears(5))
                    .build()
                )
            )
            .build();
        return sickLeaveValue;
    }

    private Map<String, CertificateDataValue> getValueMap(
        CertificateDataValue diagnosisValue, CertificateDataValue sickLeaveValue) {
        Map<String, CertificateDataValue> valueMap = new HashMap<>();
        valueMap.put("6", diagnosisValue);
        valueMap.put("32", sickLeaveValue);
        return valueMap;
    }
}
