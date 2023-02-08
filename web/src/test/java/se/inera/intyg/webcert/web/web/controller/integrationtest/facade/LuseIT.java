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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class LuseIT extends BaseFacadeIT {

    private static final String CURRENT_VERSION = "1.3";

    @Nested
    class Draft {

        @Test
        void draftShouldContainData() {
            final var testSetup = TestSetup.create()
                .draft(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertTrue(response.getData().size() > 0, "Expect draft to include data");
        }

        @Test
        void draftShouldContainMetaData() {
            final var testSetup = TestSetup.create()
                .draft(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void draftShouldContainResourceLinks() {
            final var testSetup = TestSetup.create()
                .draft(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertTrue(response.getLinks().length > 0, "Expect draft to include resourceLinks");
        }

        @Test
        void shallBeAbleToSaveDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificate = getCertificate(testSetup);

            final var response = testSetup
                .spec()
                .pathParam("certificateId", certificate.getMetadata().getId())
                .contentType(ContentType.JSON)
                .body(certificate)
                .expect().statusCode(200)
                .when()
                .put("api/certificate/{certificateId}")
                .then().extract().response().as(SaveCertificateResponseDTO.class, getObjectMapperForDeserialization());

            assertTrue(response.getVersion() > certificate.getMetadata().getVersion(),
                "Expect version after save to be incremented");
        }

        @Test
        public void shallBeAbleToDeleteDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LuseEntryPoint.MODULE_ID,
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
                final var testSetup = TestSetup.create()
                    .draft(
                        LuseEntryPoint.MODULE_ID,
                        CURRENT_VERSION,
                        CreateCertificateFillType.EMPTY,
                        DR_AJLA,
                        ALFA_VARDCENTRAL,
                        ATHENA_ANDERSSON.getPersonId().getId()
                    )
                    .login(DR_AJLA_ALFA_VARDCENTRAL)
                    .setup();

                certificateIdsToCleanAfterTest.add(testSetup.certificateId());

                final var response = getCertificate(testSetup);

                final var validation = testSetup
                    .spec()
                    .pathParam("certificateId", testSetup.certificateId())
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
                final var testSetup = TestSetup.create()
                    .draft(
                        LuseEntryPoint.MODULE_ID,
                        CURRENT_VERSION,
                        CreateCertificateFillType.MINIMAL,
                        DR_AJLA,
                        ALFA_VARDCENTRAL,
                        ATHENA_ANDERSSON.getPersonId().getId()
                    )
                    .login(DR_AJLA_ALFA_VARDCENTRAL)
                    .setup();

                certificateIdsToCleanAfterTest.add(testSetup.certificateId());

                final var response = getCertificate(testSetup);

                final var validation = testSetup
                    .spec()
                    .pathParam("certificateId", testSetup.certificateId())
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
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertTrue(response.getData().size() > 0, "Expect draft to include data");
        }

        @Test
        void certificateShouldContainMetaData() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertNotNull(response.getMetadata(), "Expect draft to include meta data");
        }

        @Test
        void certificateShouldContainResourceLinks() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var response = getCertificate(testSetup);

            assertTrue(response.getLinks().length > 0, "Expect draft to include resourceLinks");
        }

        @Test
        public void shallBeAbleToPrintCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
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
                    LuseEntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    LuseEntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then().extract().response();

            assertEquals(200, response.getStatusCode());
        }

        @Test
        public void shallBeAbleToRevokeLockedCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
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

            final var response = testSetup
                .spec()
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
                    LuseEntryPoint.MODULE_ID,
                    CURRENT_VERSION,
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
                .expect().statusCode(500)
                .when().post("api/certificate/{certificateId}/template")
                .then().extract().statusCode();

            assertEquals(response, 500);
        }

        @Test
        void shallBeAbleToComplementCurrentVersion() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LuseEntryPoint.MODULE_ID,
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
    }

    @Nested
    class LockedCertificate {

        @Test
        void shallBeAbleToCopyCurrentVersion() {
            final var testSetup = TestSetup.create()
                .lockedDraft(
                    LuseEntryPoint.MODULE_ID,
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

            assertNotNull(certificateId, "Expect certificate id to have a value");
        }
    }

    private CertificateDTO getCertificate(TestSetup testSetup) {
        return testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
    }
}
