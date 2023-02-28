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
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALEXA_VALFRIDSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BETA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_BEATA;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromCandidateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public class PdlIT extends BaseFacadeIT {

    private static final String ACTIVITY_ARGS_DRAFT_PRINTED = "Utkastet utskrivet";
    private static final String ACTIVITY_ARGS_CERTIFICATE_PRINTED = "Intyg utskrivet";
    private static final String ACTIVITY_ARGS_READ_SJF = "Läsning i enlighet med sammanhållen journalföring";

    private void assertPdlLogMessage(ActivityType expectedActivityType, String certificateId) {
        assertPdlLogMessage(expectedActivityType, certificateId, null);
    }

    private void assertPdlLogMessage(ActivityType expectedActivityType, String certificateId, String activityArgs) {
        final var pdlLogMessage = getPdlLogMessageFromQueue();
        assertNotNull(pdlLogMessage, "Pdl message was null!");
        assertAll(
            () -> assertEquals(expectedActivityType, pdlLogMessage.getActivityType()),
            () -> assertEquals("SE5565594230-B8N", pdlLogMessage.getSystemId()),
            () -> assertEquals("Webcert", pdlLogMessage.getSystemName()),
            () -> assertEquals(ActivityPurpose.CARE_TREATMENT, pdlLogMessage.getPurpose()),
            () -> assertEquals(certificateId, pdlLogMessage.getActivityLevel()),
            () -> assertEquals("Intyg", pdlLogMessage.getPdlResourceList().get(0).getResourceType()),
            () -> {
                if (activityArgs != null) {
                    assertEquals(activityArgs, pdlLogMessage.getActivityArgs());
                }
            }
        );
    }

    private void assertNumberOfPdlMessages(int expectedPdlLogMessageCount) {
        final var pdlLogMessageCount = getPdlLogMessageCountFromQueue();
        assertEquals(expectedPdlLogMessageCount, pdlLogMessageCount);
    }

    private PdlLogMessage getPdlLogMessageFromQueue() {
        return given().when().get("testability/logMessages").then().extract().response().as(PdlLogMessage.class);
    }

    private int getPdlLogMessageCountFromQueue() {
        return Integer.parseInt(given().when().get("testability/logMessages/count").then().extract().response().body().asString());
    }

    private void incrementVersion(Certificate certificate) {
        certificate.getMetadata().setVersion(certificate.getMetadata().getVersion() + 1);
    }

    @Nested
    class PdlTestRelatedToDraft {

        @Test
        public void shallPdlLogCreateActivityWhenCreatingDraft() {
            final var testSetup = TestSetup.create()
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallNotPdlLogWhenWorkingWithATestIndicatedPatient() {
            final var testSetup = TestSetup.create()
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogReadActivityWhenFetchingDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogReadActivityWithSjfWhenFetchingDraftOnDifferentCareProvider() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_BEATA,
                    BETA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
            assertPdlLogMessage(ActivityType.READ, testSetup.certificateId(), ACTIVITY_ARGS_READ_SJF);
        }

        @Test
        public void shallNotPdlLogWhenValidatingDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogUpdateActivityWhenSavingDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogUpdateActivityOnceWhenSavingDraftMultipleTimesInASession() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogPrintActivityWhenPrintingDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.MINIMAL,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
        public void shallPdlLogSignActivityWhenSigningDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.MINIMAL,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.SIGN, testSetup.certificateId());
        }

        @Test
        public void shallPdlLogDeleteActivityWhenDeleteDraft() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.3",
                    CreateCertificateFillType.EMPTY,
                    DR_AJLA,
                    ALFA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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

    @Nested
    class PdlTestRelatedToCertificate {


        @Test
        public void shallPdlLogReadActivityWhenFetchingCertificate() {
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
        public void shallPdlLogReadActivityWithSjfWhenFetchingCertificateOnDifferentCareProvider() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    BETA_VARDCENTRAL,
                    DR_BEATA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
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
            assertPdlLogMessage(ActivityType.READ, testSetup.certificateId(), ACTIVITY_ARGS_READ_SJF);
        }

        @Test
        public void shallPdlLogSendActivityWhenSendingCertificate() {
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

            testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .when().post("api/certificate/{certificateId}/send")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.SEND, testSetup.certificateId());
        }

        @Test
        public void shallPdlLogPrintActivityWhenPrintingCertificate() {
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
        public void shallPdlLogCreateActivityWhenRenewCertificate() {
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

            final var certificateId = testSetup
                .spec()
                .pathParam("certificateId", testSetup.certificateId())
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/renew")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE, certificateId);
        }

        @Test
        public void shallPdlLogCreateActivityWhenCopyFromTemplate() {
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

        @Test
        public void shallPdlLogCreateActivityWhenFillFromCandidate() {
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
                .useDjupIntegratedOrigin()
                .setup();

            final var draftId = createAg7804Draft();
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

        @Test
        void shallPdlLogCreateActivityWhenComplementCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ALFA_VARDCENTRAL,
                    DR_AJLA,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .complementQuestion()
                .clearPdlLogMessages()
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

            assertNumberOfPdlMessages(2);
            assertPdlLogMessage(ActivityType.CREATE, newCertificate.getCertificate().getMetadata().getId());
            assertPdlLogMessage(ActivityType.READ, newCertificate.getCertificate().getMetadata().getId());
        }

        @Test
        public void shallPdlLogCreateActivityWhenReplaceCertificate() {
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
        public void shallPdlLogRevokeActivityWhenRevokeCertificate() {
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

    @Nested
    class PdlTestRelatedToLockedDraft {

        @Test
        public void shallPdlLogReadActivityWhenFetchingLockedDraft() {
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
        public void shallPdlLogCreateActivityWhenCopyLockedDraft() {
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

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE, certificateId);
        }

        @Test
        public void shallPdlLogRevokeActivityWhenRevokeLockedDraft() {
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
