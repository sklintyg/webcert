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
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

/**
 * Requires that you run Intygstjanst & Webcert
 */
public class DodsbevisIT {

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
        void shallCreateDraftWithResourceLinks() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup);

            assertNotNull(response.getLinks(), "Expect draft to include resource links");
        }

        @Test
        void shallCreateDraftWithData() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup);

            assertNotNull(response.getData(), "Expect draft to include data");
        }

        @Test
        void shallCreateDraftWithMetaData() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void shallIncludePatientAddressIfExistsInPU() {
            final var testSetup = createTestDraft(ATHENA_ANDERSSON);

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup);

            assertAll(
                () -> assertEquals(true, response.getMetadata().getPatient().isAddressFromPU()),
                () -> assertNotNull(response.getMetadata().getPatient().getStreet(), "Expect draft to include street"),
                () -> assertNotNull(response.getMetadata().getPatient().getCity(), "Expect draft to include city"),
                () -> assertNotNull(response.getMetadata().getPatient().getZipCode(), "Expect draft to include zipCode")
            );
        }

        @Test
        void shallExcludePatientAddressIfMissingInPU() {
            final var testSetup = TestSetup.create()
                .draft(
                    DbModuleEntryPoint.MODULE_ID,
                    "1.0",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    BOSTADSLOSE_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestDraft(testSetup);

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

            final var originalDraft = getTestDraft(testSetup);
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

            final var updatedDraft = getTestDraft(testSetup);

            assertAll(
                () -> assertEquals(false, updatedDraft.getMetadata().getPatient().isAddressFromPU()),
                () -> assertEquals(expectedStreet, updatedDraft.getMetadata().getPatient().getStreet()),
                () -> assertEquals(expectedCity, updatedDraft.getMetadata().getPatient().getCity()),
                () -> assertEquals(expectedZipCode, updatedDraft.getMetadata().getPatient().getZipCode())
            );
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

        private CertificateDTO getTestDraft(TestSetup testSetup) {
            return given()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }

        private TestSetup createTestDraft(Patient patient) {
            return TestSetup.create()
                .draft(
                    DbModuleEntryPoint.MODULE_ID,
                    "1.0",
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
        @DisplayName("Shall create certificate with resource links")
        void shallCreateCertificateWithResourceLinks() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getLinks(), "Expect certificate to include resource links");
        }

        @Test
        @DisplayName("Shall create certificate with data")
        void shallCreateCertificateWithData() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getData(), "Expect certificate to include data");
        }

        @Test
        @DisplayName("Shall create certificate with meta data")
        void shallCreateCertificateWithMetaData() {
            final var testSetup = createTestCertificate();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getTestCertificate(testSetup);

            assertNotNull(response.getMetadata(), "Expect certificate to include meta data");
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
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();
        }
    }
}
