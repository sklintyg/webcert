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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.notification.NotificationServicesIT.AnswersEnum.DIAGNOS;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.notification.NotificationServicesIT.AnswersEnum.SICK_LEAVE;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.notification.NotificationServicesIT.AnswersEnum.SMITTSKYDD;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType.WITH_VALUES;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.TypeRef;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.TestSetup;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.dto.NotificationRequestDTO;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

@TestMethodOrder(OrderAnnotation.class)
public class NotificationServicesIT {

    private static final int HTTP_OK = 200;

    private static final List<String> LAKARE = Collections.singletonList("Läkare");

    private static final FakeCredentials JOURNA_LA_SYSTEM = new FakeCredentials.FakeCredentialsBuilder("SE4815162344-1B02",
        "SE4815162344-1A03").legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.DJUPINTEGRATION.name()).build();

    private static final TypeRef<List<CertificateStatusUpdateForCareType>> LIST_NOTIFICATIONS = new TypeRef<>(){};

    private List<String> certificateIdsToCleanAfterTest;
    private final List<NotificationRequestDTO> updateDraftRequests = new ArrayList<>();

    protected enum AnswersEnum { SMITTSKYDD, DIAGNOS, SICK_LEAVE }

    @BeforeEach
    public void setupBase() {
        final var logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.config = RestAssured.config()
            .logConfig(logConfig)
            .sessionConfig(new SessionConfig("SESSION", null));
        certificateIdsToCleanAfterTest = new ArrayList<>();
        RestAssured.objectMapper(new Jackson2Mapper(((type, charset) -> new CustomObjectMapper())));
        clearNotifications();
    }

    @AfterEach
    public void tearDown() {
        clearNotifications();
        setNotificationStubError(0);
        updateDraftRequests.clear();
        deleteEvents(certificateIdsToCleanAfterTest);
        certificateIdsToCleanAfterTest.forEach(certificateId ->
            given()
                .pathParam("certificateId", certificateId)
                .expect().statusCode(HTTP_OK)
                .when()
                .delete("testability/intyg/{certificateId}")
        );
        RestAssured.reset();
    }

    @Test
    @Order(1)
    @DisplayName("Status updates should have expected event types")
    public void statusUpdatesShouldHaveExpectedEventTypes() {

        final var testSetup = createEmptyDraft(LisjpEntryPoint.MODULE_ID, true);

        addAnswer(testSetup.certificate(), SMITTSKYDD);
        addAnswer(testSetup.certificate(), DIAGNOS);
        addAnswer(testSetup.certificate(), SICK_LEAVE);
        sign(testSetup.certificate());
        updateDraft(updateDraftRequests);

        waitForMessages(1, 3, 10, 2);

        final var statusUpdates = getNotifications();

        assertEquals(2, statusUpdates.size());
        assertEquals(1L, getNumberOfEvents(HandelsekodEnum.SKAPAT, statusUpdates));
        assertEquals(1L, getNumberOfEvents(HandelsekodEnum.SIGNAT, statusUpdates));
        assertEquals(0L, getNumberOfEvents(HandelsekodEnum.ANDRAT, statusUpdates));
    }

    @Test
    @Order(2)
    @DisplayName("Status updates should have expected basic certificate properties")
    public void statusUpdatesShouldHaveExpectedBasicCertificateProperties() {

        final var testSetup = createEmptyDraft(Af00213EntryPoint.MODULE_ID, true);

        waitForMessages(1, 3, 10, 1);

        final var statusUpdates = getNotifications();
        final var metadata = testSetup.certificate().getMetadata();
        final var certtificate = statusUpdates.get(0).getIntyg();

        assertEquals(1, statusUpdates.size());
        assertEquals(testSetup.certificateId(), certtificate.getIntygsId().getExtension());
        assertEquals(metadata.getIssuedBy().getPersonId(), certtificate.getSkapadAv().getPersonalId().getExtension());
        assertEquals(metadata.getPatient().getPersonId().getId(), certtificate.getPatient().getPersonId().getExtension());
    }

    @Test
    @Order(3)
    @DisplayName("Should send expected number of status updates when multiple certificates and events")
    public void shouldSendExpectedNumberOfStatusUpdatesWhenMultipleCertificatesAndEvents() {

        final var testSetup1 = createEmptyDraft(LisjpEntryPoint.MODULE_ID, true);
        addAnswer(testSetup1.certificate(), SMITTSKYDD);
        addAnswer(testSetup1.certificate(), DIAGNOS);

        final var testSetup2 = createEmptyDraft(LisjpEntryPoint.MODULE_ID, false);
        addAnswer(testSetup1.certificate(), SICK_LEAVE);
        addAnswer(testSetup2.certificate(), DIAGNOS);

        final var testSetup3 = createEmptyDraft(LisjpEntryPoint.MODULE_ID, false);
        sign(testSetup1.certificate());
        addAnswer(testSetup3.certificate(), SICK_LEAVE);
        addAnswer(testSetup3.certificate(), DIAGNOS);
        addAnswer(testSetup2.certificate(), SICK_LEAVE);
        addAnswer(testSetup3.certificate(), SMITTSKYDD);
        sign(testSetup3.certificate());
        addAnswer(testSetup2.certificate(), SMITTSKYDD);
        sign(testSetup2.certificate());

        updateDraft(updateDraftRequests);

        waitForMessages(2, 4, 15, 6);

        final var statusUpdates = getNotifications();

        assertEquals(6, statusUpdates.size());
        assertEquals(3L, getNumberOfEvents(HandelsekodEnum.SKAPAT, statusUpdates));
        assertEquals(3L, getNumberOfEvents(HandelsekodEnum.SIGNAT, statusUpdates));
        assertEquals(0L, getNumberOfEvents(HandelsekodEnum.ANDRAT, statusUpdates));
    }

    @Test
    @Order(4)
    @DisplayName("Successfully sent status updates should have delivery status SUCCESS")
    public void successfullySentStatusUpdatesShouldHaveDeliveryStatusSuccess() {
        clearNotifications();

        final var testSetup = createEmptyDraft(Af00213EntryPoint.MODULE_ID, true);

        waitForMessages(1, 3, 10, 1);

        final var statusUpdates = getNotifications();

        assertEquals(1, statusUpdates.size());
        assertEquals(NotificationDeliveryStatusEnum.SUCCESS, getNotificationDeliveryStatus(testSetup.certificateId()));
    }

    @Test
    @Order(5)
    @DisplayName("Status updates with result Application Error should have delivery status FAILURE")
    public void statusUpdatesWithResultApplicationErrorShouldHaveDeliveryStatusFailure() {
        setNotificationStubError(6);

        final var testSetup = createEmptyDraft(Af00213EntryPoint.MODULE_ID, true);

        waitForMessages(1, 3, 10, 1);

        final var statusUpdates = getNotifications();

        assertEquals(1, statusUpdates.size());
        assertEquals(NotificationDeliveryStatusEnum.FAILURE, getNotificationDeliveryStatus(testSetup.certificateId()));
    }

    @Test
    @Order(6)
    @DisplayName("Status updates with result Technical Error should have delivery status RESEND")
    public void statusUpdatesWithResultTechnicalErrorShouldHaveDeliveryStatusResend() {
        setNotificationStubError(4);

        final var testSetup = createEmptyDraft(Af00213EntryPoint.MODULE_ID, true);

        waitForMessages(1, 3, 10, 1);

        final var statusUpdates = getNotifications();

        assertEquals(1, statusUpdates.size());
        assertEquals(NotificationDeliveryStatusEnum.RESEND, getNotificationDeliveryStatus(testSetup.certificateId()));
    }

    private long getNumberOfEvents(HandelsekodEnum eventType, List<CertificateStatusUpdateForCareType> statusUpdates) {
        return statusUpdates.stream().filter(statusUpdate -> statusUpdate.getHandelse().getHandelsekod().getCode()
            .equals(eventType.name())).count();
    }

    private void updateDraft(List<NotificationRequestDTO> updateDraftRequest) {
        given()
            .contentType(ContentType.JSON).body(updateDraftRequest)
            .when().post("/testability/notification/updateandnotify")
            .then().statusCode(HTTP_OK);
    }

    private List<CertificateStatusUpdateForCareType> getNotifications() {
       return
            given()
                .when().get("/services/api/notification-api/notifieringar/v3")
                .then().statusCode(HTTP_OK)
                .extract().response().getBody().as(LIST_NOTIFICATIONS);
    }

    private void setNotificationStubError(int errorCode) {
        given().pathParam("errorCode", errorCode)
            .when().get("/services/api/notification-api/notifieringar/v3/emulateError/{errorCode}")
            .then().statusCode(HTTP_OK);
    }

    private void waitForMessages(int pollDelay, int pollInterval, int atMost, int numberOfMessages) {
        with()
            .pollDelay(Duration.ofSeconds(pollDelay)).and().pollInterval(Duration.ofSeconds(pollInterval))
            .await().atMost(Duration.ofSeconds(atMost)).until(messagesArrived(numberOfMessages));
    }

    private NotificationDeliveryStatusEnum getNotificationDeliveryStatus(String certificateId) {
        return
            given().pathParam("certificateId", certificateId)
            .when().get("/testability/notification/deliverystatus/{certificateId}")
            .then().statusCode(HTTP_OK)
            .extract().response().getBody().as(NotificationDeliveryStatusEnum.class);
    }

    private Callable<Boolean> messagesArrived(int numberOfMessages) {
        return () -> given()
            .when().get("/services/api/notification-api/notifieringar/v3/stats")
            .then().statusCode(HTTP_OK)
            .extract().response().getBody().asString().split("\n").length >= numberOfMessages + 2;
    }

    private void clearNotifications() {
        given()
            .when().post("/services/api/notification-api/clear")
            .then().statusCode(204);
    }

    private void deleteEvents(List<String> certificateIds) {
        given()
            .contentType(ContentType.JSON).body(certificateIds)
            .when().post("/testability/notification/clearevents")
            .then().statusCode(HTTP_OK);
    }

    private TestSetup createEmptyDraft(String certificateType, boolean login) {
        final var testSetup = TestSetup.create()
            .draft(
                certificateType,
                "1.0",
                CreateCertificateFillType.EMPTY,
                JOURNA_LA_SYSTEM.getHsaId(),
                JOURNA_LA_SYSTEM.getEnhetId(),
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(login ? JOURNA_LA_SYSTEM : null)
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());
        return testSetup;
    }

    private CreateCertificateRequestDTO createCertificateRequestDTO(Certificate certificate) {
        final var certificateRequest = new CreateCertificateRequestDTO();
        certificateRequest.setCertificateType(certificate.getMetadata().getType());
        certificateRequest.setCertificateTypeVersion(certificate.getMetadata().getTypeVersion());
        certificateRequest.setPatientId(ATHENA_ANDERSSON.getPersonId().getId());
        certificateRequest.setPersonId(JOURNA_LA_SYSTEM.getHsaId());
        certificateRequest.setUnitId(JOURNA_LA_SYSTEM.getEnhetId());
        certificateRequest.setStatus(CertificateStatus.UNSIGNED);
        certificateRequest.setFillType(WITH_VALUES);
        certificateRequest.setValues(new HashMap<>());
        certificateRequest.setSent(false);
        return certificateRequest;
    }

    private void addAnswer(Certificate certificate, AnswersEnum answerEnum) {
        final var createCertificateRequest = createCertificateRequestDTO(certificate);
        createCertificateRequest.setValues(getPreviousAnswers(certificate.getMetadata().getId()));
        createCertificateRequest.getValues().putAll(getAnswer(answerEnum));

        createUpdateDraftRequest(certificate, createCertificateRequest);
    }

    private void sign(Certificate certificate) {
        final var createCertificateRequest = createCertificateRequestDTO(certificate);
        createCertificateRequest.setStatus(CertificateStatus.SIGNED);
        createCertificateRequest.setValues(getPreviousAnswers(certificate.getMetadata().getId()));
        createUpdateDraftRequest(certificate, createCertificateRequest);
    }

    private void createUpdateDraftRequest(Certificate certificate, CreateCertificateRequestDTO createCertificateRequest) {
        final var updateDraftrequest = new NotificationRequestDTO();
        updateDraftrequest.setCertificate(certificate);
        updateDraftrequest.setCertificateRequestDTO(createCertificateRequest);
        updateDraftRequests.add(updateDraftrequest);
    }


    private Map<String, CertificateDataValue> getPreviousAnswers(String certificateId) {
        final var previousAnswers = new HashMap<String, CertificateDataValue>();

        final var relatedUpdateRequests = updateDraftRequests.stream().filter(req -> req.getCertificate()
            .getMetadata().getId().equals(certificateId)).collect(Collectors.toList());
        if (relatedUpdateRequests.isEmpty()) {
            return previousAnswers;
        }
        previousAnswers.putAll(relatedUpdateRequests.get(relatedUpdateRequests.size() - 1).getCertificateRequestDTO().getValues());
        return previousAnswers;
    }

    private Map<String, CertificateDataValue> getAnswer(AnswersEnum answerEnum) {
        switch (answerEnum) {
            case SMITTSKYDD:
                return getAnswerSmittskydd();
            case DIAGNOS:
                return getAnswerDiagnos();
            default:
                return getAnswerSickLeave();
        }
    }

    private Map<String, CertificateDataValue> getAnswerSmittskydd() {
        final var valueMap = new HashMap<String, CertificateDataValue>();
        final var certificateDataValue = CertificateDataValueBoolean.builder()
            .id(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27)
            .selected(true)
            .build();
        valueMap.put(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_ID_27, certificateDataValue);
        return valueMap;
    }

    private Map<String, CertificateDataValue> getAnswerDiagnos() {
        final var valueMap = new HashMap<String, CertificateDataValue>();
        final var certificateDataValue =  CertificateDataValueDiagnosisList.builder()
            .list(
                Collections.singletonList(
                    CertificateDataValueDiagnosis.builder()
                        .id("1")
                        .terminology("ICD_10_SE")
                        .code("J09")
                        .description("Influensa orsakad av identifierat zoonotiskt eller pandemiskt influensavirus")
                        .build()
                )
            )
            .build();
        valueMap.put(RespConstants.DIAGNOS_SVAR_ID_6, certificateDataValue);
        return valueMap;
    }

    private Map<String, CertificateDataValue> getAnswerSickLeave() {
        final var valueMap = new HashMap<String, CertificateDataValue>();
        final var certificateDataValue =  CertificateDataValueDateRangeList.builder()
            .list(
                Collections.singletonList(
                    CertificateDataValueDateRange.builder()
                        .id("HELT_NEDSATT")
                        .from(LocalDate.now())
                        .to(LocalDate.now().plusDays(21))
                        .build()
                )
            )
            .build();
        valueMap.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, certificateDataValue);
        return valueMap;
    }
}
