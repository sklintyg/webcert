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

package se.inera.intyg.webcert.web.web.controller.integrationtest.notification;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.KFSIGN;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.MAKULE;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYSVFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.RADERA;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKICKA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.TypeRef;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.TestSetup;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;
import se.inera.intyg.webcert.web.web.controller.testability.dto.NotificationRequest;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

@TestInstance(Lifecycle.PER_CLASS)
public class NotificationServicesIT {

    private static final int HTTP_OK = 200;
    private static final int OK = 0;
    private static final int TECHNICAL_ERROR = 3;
    private static final int APPLICATION_ERROR = 6;
    private static final List<String> LAKARE = Collections.singletonList("Läkare");
    private static final TypeRef<List<CertificateStatusUpdateForCareType>> LIST_NOTIFICATIONS = new TypeRef<>(){};

    private static final FakeCredentials JOURNA_LA_SYSTEM = new FakeCredentials.FakeCredentialsBuilder("SE4815162344-1B02",
        "SE4815162344-1A03").legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.DJUPINTEGRATION.name()).build();

    private List<String> certificateIdsToCleanAfterTest;

    @BeforeAll
    public void initiate() {
        getRestAssuredSession();
        registerIntegratedUnit();
        clearNotificationStub();
        setNotificationStubResponse(0);
        RestAssured.reset();
    }

    @AfterAll
    public void cleanup() {
        getRestAssuredSession();
        unregisterIntegratedUnit();
        RestAssured.reset();
    }

    @BeforeEach
    public void setupBase() {
        getRestAssuredSession();
        certificateIdsToCleanAfterTest = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        clearNotificationStub();
        setNotificationStubResponse(OK);
        deleteCreatedEvents();
        deleteCreatedArenden();
        deleteCreatedCertificates();
        RestAssured.reset();
    }

    @Test
    @DisplayName("Status update for create draft should have event type SKAPAT")
    public void statusUpdateForCreateDraftShouldHaveEventTypeSkapat() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertEquals(1L, getNumberOfEvents(HandelsekodEnum.SKAPAT, statusUpdates))
        );
    }

    @Test
    @DisplayName("Status update for sign draft should have event type SIGNAT")
    public void statusUpdateForSignDraftShouldHaveEventTypeSignat() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.MINIMAL,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), SIGNAT);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(SIGNAT, statusUpdates) >= 1)
        );
    }

    @Test
    @DisplayName("Status update for draft ready to sign should have event type KFSIGN")
    public void statusUpdateForDraftReadyToSignShouldHaveEventTypeKfsign() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.MINIMAL,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), KFSIGN);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(KFSIGN, statusUpdates) >= 1)
        );
    }

    @Test
    @DisplayName("Status update for delete draft should have event type RADERA")
    public void statusUpdateForDeleteDraftShouldHaveEventTypeKfsign() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), RADERA);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(RADERA, statusUpdates) >= 1)
        );
    }

    @Test
    @DisplayName("Status update for revoke certificate should have event type MAKULE")
    public void statusUpdateForRevokeCertificateShouldHaveEventTypeKfsign() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), MAKULE);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(MAKULE, statusUpdates) >= 1)
        );
    }

    @Test
    @DisplayName("Status update for send certificate should have event type SKICKA")
    public void statusUpdateForSendCertificateShouldHaveEventTypeSkicka() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), SKICKA);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(SKICKA, statusUpdates) >= 1)
        );
    }

    @Test
    @DisplayName("Status update for new question from care should have event type NYFRFV")
    public void statusUpdateForNewQuestionFromCareShouldHaveEventTypeNyfrfv() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), NYFRFV);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(NYFRFV, statusUpdates) >= 1),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getTotalt()),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getEjBesvarade())
        );
    }

    @Test
    @DisplayName("Status update for new answer from recipient should have event type NYSVFM")
    public void statusUpdateForNewAnswerFromRecipientShouldHaveEventTypeNysvfm() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), NYSVFM);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(NYSVFM, statusUpdates) >= 1),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getTotalt()),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getBesvarade())
        );
    }

    @Test
    @DisplayName("Status update for handled question from care should have event type HANFRFV")
    public void statusUpdateForHandledQuestionFromCareShouldHaveEventTypeHanfrfv() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), HANFRFV);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(HANFRFV, statusUpdates) >= 1),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getTotalt()),
            () -> assertEquals(1, statusUpdates.get(0).getSkickadeFragor().getHanterade())
        );
    }

    @Test
    @DisplayName("Status update for new question from recipient should have event type NYFRFM")
    public void statusUpdateForNewQuestionFromRecipientShouldHaveEventTypeNyfrfm() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), NYFRFM);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(NYFRFM, statusUpdates) >= 1),
            () -> assertEquals(1, statusUpdates.get(0).getMottagnaFragor().getTotalt()),
            () -> assertEquals(1, statusUpdates.get(0).getMottagnaFragor().getEjBesvarade())
        );
    }

    @Test
    @DisplayName("Status update for answered question from recipient should have event type HANFRFM")
    public void statusUpdateForAnsweredQuestionFromRecipientShouldHaveEventTypeHanfrfm() {
        final var testSetup = TestSetup.create()
            .certificate(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                JOURNA_LA_SYSTEM.getEnhetId(),
                JOURNA_LA_SYSTEM.getHsaId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();
        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        clearNotificationStub();
        sendNotification(testSetup.certificateId(), HANFRFM);
        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();

        assertAll(
            () -> assertTrue(getNumberOfEvents(HANFRFM, statusUpdates) >= 1),
            () -> assertEquals(1, statusUpdates.get(0).getMottagnaFragor().getTotalt()),
            () -> assertEquals(1, statusUpdates.get(0).getMottagnaFragor().getHanterade())
        );
    }

    @Test
    @DisplayName("Status updates should have expected key certificate properties")
    public void statusUpdatesShouldHaveExpectedKeyCertificateProperties() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        waitForMessages(1, 3, 15, 1);

        final var statusUpdates = getNotifications();
        final var metadata = testSetup.certificate().getMetadata();
        final var certtificate = statusUpdates.get(0).getIntyg();

        assertAll(
            () -> assertEquals(testSetup.certificateId(), certtificate.getIntygsId().getExtension()),
            () -> assertEquals(metadata.getIssuedBy().getPersonId(), certtificate.getSkapadAv().getPersonalId().getExtension()),
            () -> assertEquals(metadata.getPatient().getPersonId().getId(), certtificate.getPatient().getPersonId().getExtension())
        );
    }

    @Test
    @DisplayName("Successfully sent status updates should have delivery status SUCCESS")
    public void successfullySentStatusUpdatesShouldHaveDeliveryStatusSuccess() {
        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();

        waitForMessages(1, 3, 15, 1);

        assertAll(
            () -> assertEquals(NotificationDeliveryStatusEnum.SUCCESS, getNotificationDeliveryStatus(testSetup.certificateId()))
        );
    }

    @Test
    @DisplayName("Status updates with result Application Error should have delivery status FAILURE")
    public void statusUpdatesWithResultApplicationErrorShouldHaveDeliveryStatusFailure() {
        setNotificationStubResponse(APPLICATION_ERROR);

        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();

        waitForMessages(1, 3, 15, 1);

        assertAll(
            () -> assertEquals(NotificationDeliveryStatusEnum.FAILURE, getNotificationDeliveryStatus(testSetup.certificateId()))
        );
    }

    @Test
    @DisplayName("Status updates with result Technical Error should have delivery status RESEND")
    public void statusUpdatesWithResultTechnicalErrorShouldHaveDeliveryStatusResend() {
        setNotificationStubResponse(TECHNICAL_ERROR);

        final var testSetup = TestSetup.create()
            .draft(
                LisjpEntryPoint.MODULE_ID,
                "1.2",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(JOURNA_LA_SYSTEM)
            .setup();

        waitForMessages(1, 3, 15, 1);

        assertAll(
            () -> assertEquals(NotificationDeliveryStatusEnum.RESEND, getNotificationDeliveryStatus(testSetup.certificateId()))
        );
    }

    private void getRestAssuredSession() {
        final var logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.objectMapper(new Jackson2Mapper(((type, charset) -> new CustomObjectMapper())));
        RestAssured.config = RestAssured.config()
            .logConfig(logConfig)
            .sessionConfig(new SessionConfig("SESSION", null));
    }

    private void registerIntegratedUnit() {
        final var integratedUnit = new IntegreradEnhetEntryWithSchemaVersion();
        integratedUnit.setEnhetsId(JOURNA_LA_SYSTEM.getEnhetId());
        integratedUnit.setVardgivareId("SE4815162344-1A01");
        integratedUnit.setSchemaVersion("2.0"); // This results in SchemaVersion.VERSION_3
        integratedUnit.setEnhetsNamn("Webcert-Integration Enhet 2");
        integratedUnit.setVardgivareNamn("Webcert-Integration Vårdgivare 1");
        given()
            .contentType(ContentType.JSON).body(integratedUnit)
            .when().post("/testability/integreradevardenheter")
            .then().statusCode(HTTP_OK);
    }

    private void unregisterIntegratedUnit() {
        given().pathParam("unitId", JOURNA_LA_SYSTEM.getEnhetId())
            .when().delete("/testability/integreradevardenheter/{unitId}")
            .then().statusCode(HTTP_OK);
    }

    private void sendNotification(String certificateId, HandelsekodEnum eventEnum) {
        final var notificationRequest = new NotificationRequest();
        notificationRequest.setCertificateId(certificateId);
        notificationRequest.setEventCode(eventEnum);
        given()
            .contentType(ContentType.JSON).body(notificationRequest)
            .when().post("/testability/notification/updateandnotify")
            .then().statusCode(HTTP_OK);
    }

    private void deleteCreatedEvents() {
        given()
            .contentType(ContentType.JSON).body(certificateIdsToCleanAfterTest)
            .when().post("/testability/notification/clearevents")
            .then().statusCode(HTTP_OK);
    }

    private void deleteCreatedArenden() {
        given()
            .contentType(ContentType.JSON).body(certificateIdsToCleanAfterTest)
            .when().post("/testability/notification/cleararenden")
            .then().statusCode(HTTP_OK);
    }

    private void deleteCreatedCertificates() {
        certificateIdsToCleanAfterTest.forEach(certificateId ->
            given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(HTTP_OK)
                .when()
                .delete("testability/intyg/{certificateId}")
        );
    }

    private void waitForMessages(int pollDelay, int pollInterval, int atMost, int expectedNumberOfMessages) {
        with()
            .pollDelay(Duration.ofSeconds(pollDelay)).and().pollInterval(Duration.ofSeconds(pollInterval))
            .await().atMost(Duration.ofSeconds(atMost)).until(messagesArrived(expectedNumberOfMessages));
    }

    private Callable<Boolean> messagesArrived(int expectedNumberOfMessages) {
        return () -> given()
            .when().get("/services/api/notification-api/notifieringar/v3")
            .then().statusCode(HTTP_OK)
            .extract().response().getBody().as(LIST_NOTIFICATIONS).size() >= expectedNumberOfMessages;
    }

    private long getNumberOfEvents(HandelsekodEnum eventType, List<CertificateStatusUpdateForCareType> statusUpdates) {
        return statusUpdates.stream().filter(statusUpdate -> statusUpdate.getHandelse().getHandelsekod().getCode()
            .equals(eventType.name())).count();
    }

    private List<CertificateStatusUpdateForCareType> getNotifications() {
       return
            given()
                .when().get("/services/api/notification-api/notifieringar/v3")
                .then().statusCode(HTTP_OK)
                .extract().response().getBody().as(LIST_NOTIFICATIONS);
    }

    private void setNotificationStubResponse(int errorCode) {
        given().pathParam("errorCode", errorCode)
            .when().get("/services/api/notification-api/notifieringar/v3/emulateError/{errorCode}")
            .then().statusCode(HTTP_OK);
    }

    private NotificationDeliveryStatusEnum getNotificationDeliveryStatus(String certificateId) {
        return
            given().pathParam("certificateId", certificateId)
            .when().get("/testability/notification/deliverystatus/{certificateId}")
            .then().statusCode(HTTP_OK)
            .extract().response().getBody().as(NotificationDeliveryStatusEnum.class);
    }

    private void clearNotificationStub() {
        given()
            .when().post("/services/api/notification-api/clear")
            .then().statusCode(204);
    }
}
