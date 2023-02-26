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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class Ag7804IT extends BaseFacadeIT {

    private static final String CURRENT_VERSION = "1.2";

    @Nested
    class Ag7804ITPreviousVersions {

        @Test
        @DisplayName("Shall return certificate with version 1.0")
        void shallReturnCertificateOfVersion10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag7804EntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    "1.0",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
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
        @DisplayName("Shall return draft with current version when replacing 1.0")
        void shallReturnCertificateOfCurrentVersionWhenReplace10() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/replace")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
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
        @DisplayName("Shall return draft with current version when copy locked draft 1.0")
        void shallReturnCertificateOfCurrentVersionWhenCopy10() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/copy")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(testSetup.certificateId(), response.getMetadata().getId()),
                () -> assertEquals("1.1", response.getMetadata().getTypeVersion()),
                () -> assertEquals(CertificateStatus.SIGNED, response.getMetadata().getStatus()),
                () -> assertNotNull(response.getData(), "Expect certificate to include data"),
                () -> assertNotNull(response.getLinks(), "Expect certificate to include links")
            );
        }

        @Test
        @DisplayName("Shall be able to print draft with version 1.1")
        public void shallBeAbleToPrintCertificateOfVersion11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag7804EntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    "1.1",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
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
        @DisplayName("Shall return draft with current version when replacing 1.1")
        void shallReturnCertificateOfCurrentVersionWhenReplace11() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/replace")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
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
        @DisplayName("Shall return draft with current version when copy locked draft 1.1")
        void shallReturnCertificateOfCurrentVersionWhenCopy11() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(newCertificateRequestDTO)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/copy")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            final var response = testSetup
                .spec()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(200)
                .when()
                .get("api/certificate/{certificateId}")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertAll(
                () -> assertEquals(CURRENT_VERSION, response.getMetadata().getTypeVersion())
            );
        }
    }

    @Nested
    class Ag7804ITCurrentVersion {

        @Test
        @DisplayName("Shall create draft with version current version")
        public void shallCreateDraftOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var createUtkastRequest = new CreateUtkastRequest();
            createUtkastRequest.setIntygType(Ag7804EntryPoint.MODULE_ID);
            createUtkastRequest
                .setPatientPersonnummer(Personnummer.createPersonnummer(ATHENA_ANDERSSON.getPersonId().getId()).orElseThrow());
            createUtkastRequest.setPatientFornamn(ATHENA_ANDERSSON.getFirstName());
            createUtkastRequest.setPatientEfternamn(ATHENA_ANDERSSON.getLastName());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateType", Ag7804EntryPoint.MODULE_ID)
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
                    Ag7804EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = testSetup
                .spec()
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
        @DisplayName("Shall return validation errors for draft with current version")
        public void shallReturnValidationErrors() {
            final var testSetup = TestSetup.create()
                .draft(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateId = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
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

            final var certificateId = testSetup
                .spec()
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
        @DisplayName("Shall be able to revoke certificate with current version")
        public void shallBeAbleToRevokeCertificateOfCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    Ag7804EntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    Ag7804EntryPoint.MODULE_ID,
                    "1.1",
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

            final var response = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then().extract().response();

            assertAll(
                () -> assertEquals(200, response.getStatusCode())
            );
        }
    }

    @Test
    @DisplayName("Shall fill draft from lisjp candidate certificate")
    public void shallCreateDraftFromCandidate() {

        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .useDjupIntegratedOrigin()
            .setup();

        final var draftId = createAg7804Draft();

        final var response = testSetup
            .spec()
            .pathParam("certificateId", draftId)
            .contentType(ContentType.JSON)
            .expect().statusCode(200)
            .when()
            .post("api/certificate/{certificateId}/candidate")
            .then().extract().response();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());
        certificateIdsToCleanAfterTest.add(draftId);

        assertAll(
            () -> assertEquals(200, response.getStatusCode())
        );
    }

    @Nested
    class UpdateDraftsToLatestMinorVersion {

        private String latestMinorTextVersion;
        private String previousMinorTextVersion;
        private final String certificateType = Ag7804EntryPoint.MODULE_ID;
        private final String patientAthena = ATHENA_ANDERSSON.getPersonId().getId();

        @BeforeEach
        void setup() {
            latestMinorTextVersion = TestSetup.getLatestMinorTextVersion(certificateType, "1");
            previousMinorTextVersion = TestSetup.getPreviousMinorTextVersion(certificateType, "1");
        }

        @Test
        public void shouldOpenSavedDraftWithLatestTextVersion() {
            final var testSetup = TestSetup.create()
                .draft(certificateType, previousMinorTextVersion, CreateCertificateFillType.MINIMAL, DR_AJLA, ALFA_VARDCENTRAL,
                    patientAthena)
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .useDjupIntegratedOrigin()
                .setup();
            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateResponse = getCertificate(testSetup);

            assertEquals(latestMinorTextVersion, certificateResponse.getMetadata().getTypeVersion());
        }

        @Test
        public void shouldOpenSignedCertificateWithOriginalTextVersion() {
            final var testSetup = TestSetup.create()
                .certificate(certificateType, previousMinorTextVersion, ALFA_VARDCENTRAL, DR_AJLA, patientAthena)
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .useDjupIntegratedOrigin()
                .setup();
            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificateResponse = getCertificate(testSetup);

            assertEquals(previousMinorTextVersion, certificateResponse.getMetadata().getTypeVersion());
        }

        private CertificateDTO getCertificate(TestSetup testSetup) {
            return testSetup.spec()
                .pathParam("certificateId", testSetup.certificateId())
                .when().get("api/certificate/{certificateId}")
                .then().statusCode(200)
                .extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
        }
    }

    private String createAg7804Draft() {
        final var createUtkastRequest = new CreateUtkastRequest();
        createUtkastRequest.setIntygType(Ag7804EntryPoint.MODULE_ID);
        createUtkastRequest
            .setPatientPersonnummer(Personnummer.createPersonnummer(ATHENA_ANDERSSON.getPersonId().getId()).orElseThrow());
        createUtkastRequest.setPatientFornamn(ATHENA_ANDERSSON.getFirstName());
        createUtkastRequest.setPatientEfternamn(ATHENA_ANDERSSON.getLastName());

        return given()
            .pathParam("certificateType", Ag7804EntryPoint.MODULE_ID)
            .contentType(ContentType.JSON)
            .body(createUtkastRequest)
            .expect().statusCode(200)
            .when()
            .post("api/utkast/{certificateType}")
            .then().extract().path("intygsId").toString();
    }
}

