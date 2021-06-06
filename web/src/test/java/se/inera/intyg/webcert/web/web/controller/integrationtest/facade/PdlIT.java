/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.fail;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALEXA_VALFRIDSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BETA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_BETA;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.CreateCertificateFillType;

public class PdlIT {

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
            createUtkastRequest.setPatientPersonnummer(Personnummer.createPersonnummer(ATHENA_ANDERSSON.getPersonId().getId()).get());
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

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE);
        }

        @Test
        public void shallNotPdlLogWhenWorkingWithATestIndicatedPatient() {
            final var testSetup = TestSetup.create()
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            final var createUtkastRequest = new CreateUtkastRequest();
            createUtkastRequest.setIntygType(LisjpEntryPoint.MODULE_ID);
            createUtkastRequest.setPatientPersonnummer(Personnummer.createPersonnummer(ALEXA_VALFRIDSSON.getPersonId().getId()).get());
            createUtkastRequest.setPatientFornamn(ALEXA_VALFRIDSSON.getFirstName());
            createUtkastRequest.setPatientEfternamn(ALEXA_VALFRIDSSON.getLastName());

            final var certificateId = given()
                .pathParam("certificateType", LisjpEntryPoint.MODULE_ID)
                .contentType(ContentType.JSON)
                .body(createUtkastRequest)
                .when()
                .post("api/utkast/{certificateType}")
                .then().extract().path("intygsId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            given()
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ);
        }

        @Test
        public void shallPdlLogReadActivityWithSjfWhenFetchingDraftOnDifferentCareProvider() {
            final var testSetup = TestSetup.create()
                .draft(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    CreateCertificateFillType.EMPTY,
                    DR_BETA,
                    BETA_VARDCENTRAL,
                    ATHENA_ANDERSSON.getPersonId().getId()
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .sjf()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ, true);
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

            given()
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.UPDATE);
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            incrementVersion(testSetup.certificate());

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when()
                .put("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.UPDATE);
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            // Log.activity.activityArg = "Intyg utskrivet" eller "Utkastet utskrivet"
            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.PRINT);
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(testSetup.certificate())
                .when().post("api/certificate/{certificateId}/sign")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.SIGN);
        }

        @Test
        public void shallPdlLogDeleteActivityWhenDeleteDraft() {
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

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("version", 1)
                .when()
                .delete("api/certificate/{certificateId}/{version}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.DELETE);
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
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    DR_AJLA,
                    ALFA_VARDCENTRAL
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .sjf()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ);
        }

        @Test
        public void shallPdlLogReadActivityWithSjfWhenFetchingCertificateOnDifferentCareProvider() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    DR_BETA,
                    BETA_VARDCENTRAL
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .sjf()
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .when()
                .get("api/certificate/{certificateId}")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.READ, true);
        }

        @Disabled("Until it has been implemented")
        @Test
        public void shallPdlLogSendActivityWhenSendingCertificate() {
//        Log.activity.activityType = "Utskrift"
//        Log.activity.activityArg = "Intyg skickat till mottagare [mottagare]"
            fail();
        }

        @Test
        public void shallPdlLogPrintActivityWhenPrintingCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    DR_AJLA,
                    ALFA_VARDCENTRAL
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .pathParam("certificateType", testSetup.certificate().getMetadata().getType())
                .when()
                .get("/moduleapi/intyg/{certificateType}/{certificateId}/pdf")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            // Log.activity.activityArg = "Intyg utskrivet" eller "Utkastet utskrivet"
            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.PRINT);
        }

        @Disabled("Until it has been implemented")
        @Test
        public void shallPdlLogCreateActivityWhenRenewCertificate() {
            fail();
        }

        @Disabled("Until it has been implemented")
        @Test
        public void shallPdlLogCreateActivityWhenCopyFromCertificate() {
            fail();
        }

        @Test
        public void shallPdlLogCreateActivityWhenReplaceCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    DR_AJLA,
                    ALFA_VARDCENTRAL
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var replaceCertificateRequest = new ReplaceCertificateRequestDTO();
            replaceCertificateRequest.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
            replaceCertificateRequest.setCertificateType(testSetup.certificate().getMetadata().getType());

            final var certificateId = given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(replaceCertificateRequest)
                .expect().statusCode(200)
                .when().post("api/certificate/{certificateId}/replace")
                .then().extract().path("certificateId").toString();

            certificateIdsToCleanAfterTest.add(certificateId);

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.CREATE);
        }

        @Test
        public void shallPdlLogRevokeActivityWhenRevokeCertificate() {
            final var testSetup = TestSetup.create()
                .certificate(
                    LisjpEntryPoint.MODULE_ID,
                    "1.2",
                    ATHENA_ANDERSSON.getPersonId().getId(),
                    DR_AJLA,
                    ALFA_VARDCENTRAL
                )
                .clearPdlLogMessages()
                .login(DR_AJLA_ALFA_VARDCENTRAL)
                .setup();

            certificateIdsToCleanAfterTest.add(testSetup.certificateId());

            final var revokeCertificateRequest = new RevokeCertificateRequestDTO();
            revokeCertificateRequest.setReason("Reason");
            revokeCertificateRequest.setMessage("Message");

            given()
                .pathParam("certificateId", testSetup.certificateId())
                .contentType(ContentType.JSON)
                .body(revokeCertificateRequest)
                .when().post("api/certificate/{certificateId}/revoke")
                .then()
                .assertThat().statusCode(HttpStatus.OK.value());

            assertNumberOfPdlMessages(1);
            assertPdlLogMessage(ActivityType.REVOKE);
        }
    }

    private void assertPdlLogMessage(ActivityType expectedActivityType) {
        assertPdlLogMessage(expectedActivityType, false);
    }

    private void assertPdlLogMessage(ActivityType expectedActivityType, boolean sjf) {
        final var pdlLogMessage = getPdlLogMessageFromQueue();
        assertNotNull(pdlLogMessage, "Pdl message was null!");
        assertAll(
            () -> assertEquals(expectedActivityType, pdlLogMessage.getActivityType()),
            () -> assertEquals("SE5565594230-B8N", pdlLogMessage.getSystemId()),
            () -> assertEquals("Webcert", pdlLogMessage.getSystemName()),
            () -> assertEquals(ActivityPurpose.CARE_TREATMENT, pdlLogMessage.getPurpose()),
            () -> assertEquals("Intyg", pdlLogMessage.getPdlResourceList().get(0).getResourceType()),
            () -> {
                if (sjf) {
                    assertEquals("Läsning i enlighet med sammanhållen journalföring", pdlLogMessage.getActivityArgs());
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
        return Integer.parseInt(
            given().when().get("testability/logMessages/count").then().extract().response().body().asString()
        );
    }

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

    private void incrementVersion(Certificate certificate) {
        certificate.getMetadata().setVersion(certificate.getMetadata().getVersion() + 1);
    }
}
