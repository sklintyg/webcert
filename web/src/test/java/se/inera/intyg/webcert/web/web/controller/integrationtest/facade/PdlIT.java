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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
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
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;

public class PdlIT {

    @Before
    public void setupBase() throws FileNotFoundException {
        final var logconfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.config = RestAssured.config()
            .logConfig(logconfig)
            .sessionConfig(new SessionConfig("SESSION", null));
    }

    @After
    public void tearDown() {
        // Remove all utkast after each test
        given().expect().statusCode(200).when().delete("testability/intyg");
        RestAssured.reset();
    }

    // TODO: Testa angående valideringsperson.

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

        given()
            .pathParam("certificateType", LisjpEntryPoint.MODULE_ID)
            .contentType(ContentType.JSON)
            .body(createUtkastRequest)
            .when()
            .post("api/utkast/{certificateType}")
            .then()
            .assertThat().statusCode(HttpStatus.OK.value());

        assertNumberOfPdlMessages(1);
        assertPdlLogMessage(ActivityType.CREATE);
    }

    @Test
    public void shallPdlLogReadActivityWhenFetchingDraft() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_BETA,
                BETA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .sjf()
            .setup();

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
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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

    private void incrementVersion(Certificate certificate) {
        certificate.getMetadata().setVersion(certificate.getMetadata().getVersion() + 1);
    }

    @Test
    public void shallPdlLogPrintActivityWhenPrintingDraft() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
            )
            .clearPdlLogMessages()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();

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
    public void shallPdlLogCreateActivityWhenRenewCertificate() {
        fail();
    }

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

        final var replaceCertificateRequest = new ReplaceCertificateRequestDTO();
        replaceCertificateRequest.setPatientId(testSetup.certificate().getMetadata().getPatient().getPersonId());
        replaceCertificateRequest.setCertificateType(testSetup.certificate().getMetadata().getType());

        given()
            .pathParam("certificateId", testSetup.certificateId())
            .contentType(ContentType.JSON)
            .body(replaceCertificateRequest)
            .when().post("api/certificate/{certificateId}/replace")
            .then()
            .assertThat().statusCode(HttpStatus.OK.value());

        assertNumberOfPdlMessages(1);
        assertPdlLogMessage(ActivityType.CREATE);
    }

    @Test
    public void shallPdlLogDeleteActivityWhenDeleteDraft() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                ATHENA_ANDERSSON.getPersonId().getId(),
                DR_AJLA,
                ALFA_VARDCENTRAL
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

    private void assertPdlLogMessage(ActivityType expectedActivityType) {
        assertPdlLogMessage(expectedActivityType, false);
    }

    private void assertPdlLogMessage(ActivityType expectedActivityType, boolean sjf) {
        final var pdlLogMessage = getPdlLogMessageFromQueue();
        assertNotNull(pdlLogMessage);
        assertEquals(expectedActivityType, pdlLogMessage.getActivityType());
        if (sjf) {
            assertEquals("Läsning i enlighet med sammanhållen journalföring", pdlLogMessage.getActivityArgs());
        }
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
}
