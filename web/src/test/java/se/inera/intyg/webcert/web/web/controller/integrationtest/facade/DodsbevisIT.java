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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BOSTADSLOSE_ANDERSSON;
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
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

/**
 * Requires that you run Intygstjanst & Webcert
 */
public class DodsbevisIT {

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
        void shallCreateDbDraft() {
            TestSetup.create()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var certificateId = given()
                .pathParam("certificateType", DbModuleEntryPoint.MODULE_ID)
                .pathParam("patientId", ATHENA_ANDERSSON.getPersonId().getId())
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateType}/{patientId}")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = getTestDraft(certificateId);

            assertEquals(response.getMetadata().getType(), "db", "Expect to create draft of type db");
        }

        @Test
        void shallCreateDraftWithResourceLinks() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup.certificateId());

            assertNotNull(response.getLinks(), "Expect draft to include resource links");
        }

        @Test
        void shallCreateDraftWithData() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup.certificateId());

            assertNotNull(response.getData(), "Expect draft to include data");
        }

        @Test
        void shallCreateDraftWithMetaData() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup.certificateId());

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void shallIncludePatientAddressIfExistsInPU() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup.certificateId());

            assertAll(
                () -> assertEquals(true, response.getMetadata().getPatient().isAddressFromPU()),
                () -> assertNotNull(response.getMetadata().getPatient().getStreet(), "Expect draft to include street"),
                () -> assertNotNull(response.getMetadata().getPatient().getCity(), "Expect draft to include city"),
                () -> assertNotNull(response.getMetadata().getPatient().getZipCode(), "Expect draft to include zipCode")
            );
        }

        @Test
        void shallExcludePatientAddressIfMissingInPU() {
            final var testSetup = createTestDraft(BOSTADSLOSE_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup.certificateId());

            assertAll(
                () -> assertEquals(false, response.getMetadata().getPatient().isAddressFromPU()),
                () -> assertNull(response.getMetadata().getPatient().getStreet(), "Expect draft to exclude street"),
                () -> assertNull(response.getMetadata().getPatient().getCity(), "Expect draft to exclude city"),
                () -> assertNull(response.getMetadata().getPatient().getZipCode(), "Expect draft to exclude zipCode")
            );
        }

        @Test
        void shallIncludePatientAddressIfMissingInPUAndEnteredByUser() {
            final var testSetup = createTestDraft(BOSTADSLOSE_ANDERSSON);

            final var expectedZipCode = "99999";
            final var expectedStreet = "New Street address";
            final var expectedCity = "New City";

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var originalDraft = getTestDraft(testSetup.certificateId());
            originalDraft.getMetadata().setPatient(
                getPatientWithAddress(expectedZipCode, expectedStreet, expectedCity, originalDraft.getMetadata().getPatient())
            );

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(originalDraft)
                .expect().statusCode(200)
                .when()
                .put("api/certificate/{certificateId}")
                .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

            final var updatedDraft = getTestDraft(testSetup.certificateId());

            assertAll(
                () -> assertEquals(false, updatedDraft.getMetadata().getPatient().isAddressFromPU()),
                () -> assertEquals(expectedStreet, updatedDraft.getMetadata().getPatient().getStreet()),
                () -> assertEquals(expectedCity, updatedDraft.getMetadata().getPatient().getCity()),
                () -> assertEquals(expectedZipCode, updatedDraft.getMetadata().getPatient().getZipCode())
            );
        }

        @Test
        public void shallReturnValidationErrorsForDraft() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateId}/validate")
                .then().extract().response().as(ValidateCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertNotNull(response.getValidationErrors(), "Expect response to include validation errors");
        }

        @Test
        public void shallBeAbleToDeleteDraft() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("version", 1)
                .when()
                .delete("api/certificate/{certificateId}/{version}")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }

        @Test
        public void shallReturnNewVersionWhenSaving() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .expect().statusCode(200)
                .when()
                .put("api/certificate/{certificateId}")
                .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertTrue(response.getVersion() > testSetup.certificate().getMetadata().getVersion(),
                "Expect version after save to be incremented");
        }

        private Patient getPatientWithAddress(String expectedZipCode, String expectedStreet, String expectedCity, Patient currentPatient) {
            return Patient.builder()
                .personId(currentPatient.getPersonId())
                .firstName(currentPatient.getFirstName())
                .middleName(currentPatient.getMiddleName())
                .lastName(currentPatient.getLastName())
                .fullName(currentPatient.getFullName())
                .zipCode(expectedZipCode)
                .street(expectedStreet)
                .city(expectedCity)
                .addressFromPU(currentPatient.isAddressFromPU())
                .testIndicated(currentPatient.isTestIndicated())
                .protectedPerson(currentPatient.isProtectedPerson())
                .deceased(currentPatient.isDeceased())
                .differentNameFromEHR(currentPatient.isDifferentNameFromEHR())
                .previousPersonId(currentPatient.getPreviousPersonId())
                .personIdChanged(currentPatient.isPersonIdChanged())
                .reserveId(currentPatient.isReserveId())
                .build();
        }

        private CertificateDTO getTestDraft(String certificateId) {
            return given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }

        private TestSetup createTestDraft(Patient patient) {
            return TestSetup.create()
                .draft(
                    DbModuleEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    patient.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();
        }
    }


    @Nested
    class Certificate {

        @Test
        void shallCreateCertificateWithResourceLinks() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getLinks(), "Expect certificate to include resource links");
        }

        @Test
        void shallCreateCertificateWithData() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getData(), "Expect certificate to include data");
        }

        @Test
        void shallCreateCertificateWithMetaData() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getMetadata(), "Expect certificate to include meta data");
        }

        @Test
        public void shallBeAbleToPrintCertificate() {
            final var testSetup = createTestCertificate();

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
            final var testSetup = createTestCertificate();
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
            final var testSetup = createTestCertificate();

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
        public void shallBeAbleToRevokeLockedCertificate() {
            final var testSetup = createLockedTestCertificate();

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
        void shallBeAbleToCreateCertificateFromTemplateCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    DbModuleEntryPoint.MODULE_ID,
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

        private CertificateDTO getTestCertificate(TestSetup testSetup) {
            return given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }

        private TestSetup createTestCertificate() {
            return TestSetup.create()
                .certificate(
                    DbModuleEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();
        }

        private TestSetup createLockedTestCertificate() {
            return TestSetup.create()
                .lockedDraft(
                    Ag7804EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();
        }
    }
}
