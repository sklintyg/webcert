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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALEXA_VALFRIDSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;

import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public abstract class CommonDraftIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    private final List<String> uniqueSignedCertificates = List.of(
        DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID
    );

    @Test
    @DisplayName("Shall contain required fields with correct type version")
    void draftShouldContainRequiredFieldsWithCorrectTypeVersion() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion()).setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = getCertificate(testSetup);

        assertAll(
            () -> assertTrue(response.getData().size() > 0, "Expect draft to include data"),
            () -> assertTrue(response.getLinks().length > 0, "Expect draft to include data"),
            () -> assertNotNull(response.getMetadata(), "Expect draft to include data"),
            () -> assertEquals(typeVersion(), response.getMetadata().getTypeVersion())
        );
    }

    @Test
    @DisplayName("Shall be able to sign draft")
    void shallBeAbleToSignDraft() {
        if (!uniqueSignedCertificates.contains(moduleId())) {

            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.MINIMAL, moduleId(), typeVersion()).setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var certificate = getCertificate(testSetup);

            final var response = testSetup
                .spec()
                .pathParam("certificateId", certificate.getMetadata().getId())
                .contentType(ContentType.JSON)
                .body(certificate)
                .expect().statusCode(200)
                .when()
                .post("api/certificate/{certificateId}/sign")
                .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();

            assertEquals(CertificateStatus.SIGNED, response.getMetadata().getStatus(),
                "Expect certificate status to be signed");
        }
    }

    @Test
    @DisplayName("Shall be able to save draft")
    void shallBeAbleToSaveDraft() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion()).setup();

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
    @DisplayName("Shall be able to remove draft")
    public void shallBeAbleToRemoveDraft() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion()).setup();

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

    @Test
    @DisplayName("Shall be return validation errors for empty draft")
    void shallReturnValidationErrorsForEmptyDraft() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion()).setup();

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
    @DisplayName("Shall be able to print draft")
    void shallBeAbleToPrintDraft() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion()).setup();

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

    @Nested
    class PdlTestRelatedToDraft {

        @Test
        @DisplayName("Shall pdl log create activity when creating draft")
        public void shallPdlLogCreateActivityWhenCreatingDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            final var createUtkastRequest = new CreateUtkastRequest();
            createUtkastRequest.setIntygType(LisjpEntryPoint.MODULE_ID);
            createUtkastRequest.setPatientPersonnummer(
                Personnummer.createPersonnummer(ATHENA_ANDERSSON.getPersonId().getId()).orElseThrow());
            createUtkastRequest.setPatientFornamn(ATHENA_ANDERSSON.getFirstName());
            createUtkastRequest.setPatientEfternamn(ATHENA_ANDERSSON.getLastName());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateType", LisjpEntryPoint.MODULE_ID)
                .contentType(ContentType.JSON)
                .body(createUtkastRequest)
                .expect().statusCode(200)
                .when()
                .post("api/utkast/{certificateType}")
                .then().extract().path("intygsId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE, certificateId);
        }

        @Test
        @DisplayName("Shall not pdl log when working with a test indicated patient")
        public void shallNotPdlLogWhenWorkingWithATestIndicatedPatient() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            final var createUtkastRequest = new CreateUtkastRequest();
            createUtkastRequest.setIntygType(LisjpEntryPoint.MODULE_ID);
            createUtkastRequest.setPatientPersonnummer(
                Personnummer.createPersonnummer(ALEXA_VALFRIDSSON.getPersonId().getId()).orElseThrow());
            createUtkastRequest.setPatientFornamn(ALEXA_VALFRIDSSON.getFirstName());
            createUtkastRequest.setPatientEfternamn(ALEXA_VALFRIDSSON.getLastName());

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateType", LisjpEntryPoint.MODULE_ID)
                .contentType(ContentType.JSON)
                .body(createUtkastRequest)
                .when()
                .post("api/utkast/{certificateType}")
                .then().extract().path("intygsId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            testSetup
                .spec()
                .pathParam("certificateId", certificateId)
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(0);
        }

        @Test
        @DisplayName("Shall pdl log read activity when feting draft")
        public void shallPdlLogReadActivityWhenFetchingDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .sjf()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ, testSetup.certificateId());
        }

        @Test
        @DisplayName("Shall pdl log read activity with sjf, when feting draft on different care provider")
        public void shallPdlLogReadActivityWithSjfWhenFetchingDraftOnDifferentCareProvider() {
            final var testSetup = getDraftTestSetupForPdlWithSjfDifferentCareProvider(CreateCertificateFillType.EMPTY, moduleId(),
                typeVersion());

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ, testSetup.certificateId(), ACTIVITY_ARGS_READ_SJF);
        }

        @Test
        @DisplayName("Shall not pdl log when validating draft")
        public void shallNotPdlLogWhenValidatingDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .post("api/certificate/{certificateId}/validate")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(0);
        }

        @Test
        @DisplayName("Shall pdl log update activity when saving draft")
        public void shallPdlLogUpdateActivityWhenSavingDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.UPDATE, testSetup.certificateId());
        }

        @Test
        @DisplayName("Shall pdl log update activity when saving draft multiple times in a session")
        public void shallPdlLogUpdateActivityOnceWhenSavingDraftMultipleTimesInASession() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            incrementVersion(testSetup.certificate());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.UPDATE, testSetup.certificateId());
        }

        @Test
        @DisplayName("Shall pdl log print activity when printing draft")
        public void shallPdlLogPrintActivityWhenPrintingDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.PRINT, testSetup.certificateId(), ACTIVITY_ARGS_DRAFT_PRINTED);
        }

        @Test
        @DisplayName("Shall pdl log sign activity when signing draft")
        public void shallPdlLogSignActivityWhenSigningDraft() {
            if (!uniqueSignedCertificates.contains(moduleId())) {

                final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.MINIMAL, moduleId(), typeVersion())
                    .clearPdlLogMessages()
                    .setup();

                certificateIdsToCleanAfterTest.add(testSetup.certificateId());

                testSetup
                    .spec()
                    .pathParam("certificateId", testSetup.certificateId())
                    .contentType(ContentType.JSON)
                    .body(testSetup.certificate())
                    .when().post("api/certificate/{certificateId}/sign")
                    .then()
                    .assertThat().statusCode(HttpStatus.OK.value());

                assertNumberOfPdlMessages(moduleId().equals(Af00213EntryPoint.MODULE_ID) ? 2 : 1);
                assertPdlLogMessage(ActivityType.SIGN, testSetup.certificateId());
            }
        }

        @Test
        @DisplayName("Shall pdl log delete activity when deleting draft")
        public void shallPdlLogDeleteActivityWhenDeleteDraft() {
            final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.EMPTY, moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("version", 1)
                .when()
                .delete("api/certificate/{certificateId}/{version}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.DELETE, testSetup.certificateId());
        }
    }
}
