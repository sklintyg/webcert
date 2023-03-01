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
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;

import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromCandidateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;

public abstract class CommonCertificateIT extends CommonFacadeITSetup {

    protected abstract String moduleId();

    protected abstract String typeVersion();

    protected abstract List<String> typeVersionList();

    protected Stream<String> typeVersionStream() {
        return typeVersionList().stream();
    }

    @DisplayName("Shall contain required fields with correct type version")
    @Test
    void certificateShouldContainRequiredFieldsWithCorrectTypeVersion() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion()).setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var response = getCertificate(testSetup);

        assertAll(
            () -> assertTrue(response.getData().size() > 0, "Expect draft to include data"),
            () -> assertTrue(response.getLinks().length > 0, "Expect draft to include data"),
            () -> assertNotNull(response.getMetadata(), "Expect draft to include data"),
            () -> assertEquals(typeVersion(), response.getMetadata().getTypeVersion())
        );
    }

    @DisplayName("Shall be able to print certificate")
    @Test
    public void shallBeAbleToPrintCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion()).setup();

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

    @DisplayName("Shall be able to revoke certificate")
    @Test
    public void shallBeAbleToRevokeCertificate() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion()).setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
        revokeCertificateRequest.setReason("reason");
        revokeCertificateRequest.setMessage("message");

        final var response = testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(revokeCertificateRequest)
            .when().post("api/certificate/{certificateId}/revoke")
            .then().extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("typeVersionStream")
    @DisplayName("Shall be able to replace certificate and get correct type version")
    void shallBeAbleToReplaceCertificate(String typeVersion) {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion).setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var expectedPersonId = testSetup.certificate().getMetadata().getPatient().getPersonId();
        final var expectedType = testSetup.certificate().getMetadata().getType();

        final var newCertificateRequestDTO = new NewCertificateRequestDTO();
        newCertificateRequestDTO.setPatientId(expectedPersonId);
        newCertificateRequestDTO.setCertificateType(expectedType);

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

        assertEquals(typeVersion(), response.getMetadata().getTypeVersion());
    }

    @Nested
    class PdlTestRelatedToCertificate {

        @Test
        @DisplayName("Shall pdl log read activity when fetching certificate")
        public void shallPdlLogReadActivityWhenFetchingCertificate() {
            final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
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
        @DisplayName("Shall pdl log read activity when fetching certificate on different care provider")
        public void shallPdlLogReadActivityWithSjfWhenFetchingCertificateOnDifferentCareProvider() {
            final var testSetup = getCertificateTestSetupForPdlWithSjfDifferentCareProvider(moduleId(), typeVersion());

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
        @DisplayName("Shall pdl log print activity when printing certificate")
        public void shallPdlLogPrintActivityWhenPrintingCertificate() {
            final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
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
            assertPdlLogMessage(ActivityType.PRINT, testSetup.certificateId(), ACTIVITY_ARGS_CERTIFICATE_PRINTED);
        }

        @Test
        @DisplayName("Shall pdl log create activity when copy certificate from template")
        public void shallPdlLogCreateActivityWhenCopyFromTemplate() {
            if (moduleId().equals(LisjpEntryPoint.MODULE_ID)) {
                final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
                    .clearPdlLogMessages()
                    .setup();

                certificateIdsToCleanAfterTest.add(testSetup.certificateId());

                final var response = testSetup
                    .spec()
                    .pathParam("certificateId", testSetup.certificateId())
                    .contentType(ContentType.JSON)
                    .expect().statusCode(200)
                    .when().post("api/certificate/{certificateId}/template")
                    .then().extract().response().as(CreateCertificateFromCandidateResponseDTO.class, getObjectMapperForDeserialization());

                certificateIdsToCleanAfterTest.add(response.getCertificateId());

                assertNumberOfPdlMessages(2);
                assertPdlLogMessage(ActivityType.READ, testSetup.certificateId());
                assertPdlLogMessage(ActivityType.CREATE, response.getCertificateId());
            }
        }

        @Test
        @DisplayName("Shall pdl log create activity when fill certificate from candidate")
        public void shallPdlLogCreateActivityWhenFillFromCandidate() {
            if (moduleId().equals(LisjpEntryPoint.MODULE_ID)) {
                final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
                    .clearPdlLogMessages()
                    .useDjupIntegratedOrigin()
                    .setup();

                final var draftId = createDraftAndReturnCertificateId(Ag7804EntryPoint.MODULE_ID, ATHENA_ANDERSSON);
                certificateIdsToCleanAfterTest.add(testSetup.certificateId());
                certificateIdsToCleanAfterTest.add(draftId);

                final var response = testSetup
                    .spec()
                    .pathParam("certificateId", draftId)
                    .contentType(ContentType.JSON)
                    .expect().statusCode(200)
                    .when().post("api/certificate/{certificateId}/candidate")
                    .then().extract().response().as(CreateCertificateFromTemplateResponseDTO.class, getObjectMapperForDeserialization());

                certificateIdsToCleanAfterTest.add(response.getCertificateId());

                assertNumberOfPdlMessages(4);
                assertPdlLogMessage(ActivityType.CREATE, response.getCertificateId());
                assertPdlLogMessage(ActivityType.READ, testSetup.certificateId());
                assertPdlLogMessage(ActivityType.READ, testSetup.certificateId());
                assertPdlLogMessage(ActivityType.UPDATE, response.getCertificateId());
            }
        }

        @Test
        @DisplayName("Shall pdl log create activity when replacing certificate")
        public void shallPdlLogCreateActivityWhenReplaceCertificate() {
            final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
                .clearPdlLogMessages()
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

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE, certificateId);
        }

        @Test
        @DisplayName("Shall pdl log revoke activity when revoking certificate")
        public void shallPdlLogRevokeActivityWhenRevokeCertificate() {
            final var testSetup = getCertificateTestSetupBuilder(moduleId(), typeVersion())
                .clearPdlLogMessages()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
            revokeCertificateRequest.setReason("Reason");
            revokeCertificateRequest.setMessage("Message");

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.REVOKE, testSetup.certificateId());
        }
    }
}
