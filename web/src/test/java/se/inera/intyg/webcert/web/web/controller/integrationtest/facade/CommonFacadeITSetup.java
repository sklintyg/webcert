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
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.ATHENA_ANDERSSON;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.BETA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_AJLA_ALFA_VARDCENTRAL;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest.DR_BEATA;

import io.restassured.http.ContentType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.TestSetup.TestSetupBuilder;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.BaseFacadeIT;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public abstract class CommonFacadeITSetup extends BaseFacadeIT {

    protected static final String ACTIVITY_ARGS_DRAFT_PRINTED = "Utkastet utskrivet";
    protected static final String ACTIVITY_ARGS_CERTIFICATE_PRINTED = "Intyg utskrivet";
    protected static final String ACTIVITY_ARGS_READ_SJF = "Läsning i enlighet med sammanhållen journalföring";

    protected TestSetup getLoginTestSetup() {
        return TestSetup.create().login(DR_AJLA_ALFA_VARDCENTRAL).setup();
    }

    protected TestSetupBuilder getCertificateTestSetupBuilder(String moduleId, String typeVersion) {
        return TestSetup.create()
            .certificate(
                moduleId,
                typeVersion,
                ALFA_VARDCENTRAL,
                DR_AJLA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL);
    }

    protected TestSetupBuilder getDraftTestSetupBuilder(CreateCertificateFillType fillType, String moduleId,
        String typeVersion) {
        return TestSetup.create()
            .draft(
                moduleId,
                typeVersion,
                fillType,
                DR_AJLA,
                ALFA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL);
    }

    protected TestSetupBuilder getDraftTestSetupBuilder(CreateCertificateFillType fillType, String moduleId,
        String typeVersion, Patient patient) {
        return TestSetup.create()
            .draft(
                moduleId,
                typeVersion,
                fillType,
                DR_AJLA,
                ALFA_VARDCENTRAL,
                patient.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL);
    }

    protected TestSetupBuilder getLockedCertificateTestSetupBuilder(String moduleId, String typeVersion) {
        return TestSetup.create()
            .lockedDraft(
                moduleId,
                typeVersion,
                DR_AJLA,
                ALFA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL);
    }

    protected TestSetup getDraftTestSetupForPdlWithSjfDifferentCareProvider(CreateCertificateFillType fillType, String moduleId,
        String typeVersion) {
        return TestSetup.create()
            .draft(
                moduleId,
                typeVersion,
                fillType,
                DR_BEATA,
                BETA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .clearPdlLogMessages()
            .sjf()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
    }

    protected TestSetup getCertificateTestSetupForPdlWithSjfDifferentCareProvider(String moduleId,
        String typeVersion) {
        return TestSetup.create()
            .certificate(
                moduleId,
                typeVersion,
                BETA_VARDCENTRAL,
                DR_BEATA,
                ATHENA_ANDERSSON.getPersonId().getId()
            )
            .clearPdlLogMessages()
            .sjf()
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
    }

    protected TestSetup getDraftWithValuesTestSetup(String moduleId, String typeVersion, Map<String, CertificateDataValue> valueMap) {
        return TestSetup.create()
            .draftWithValues(
                moduleId,
                typeVersion,
                DR_AJLA,
                ALFA_VARDCENTRAL,
                ATHENA_ANDERSSON.getPersonId().getId(),
                valueMap
            )
            .login(DR_AJLA_ALFA_VARDCENTRAL)
            .setup();
    }

    protected CertificateDTO getCertificate(TestSetup testSetup) {
        return testSetup
            .spec()
            .pathParam("certificateId", testSetup.certificateId())
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
    }

    protected CertificateDTO getCertificate(TestSetup testSetup, String certificateId) {
        return testSetup
            .spec()
            .pathParam("certificateId", certificateId)
            .expect().statusCode(200)
            .when()
            .get("api/certificate/{certificateId}")
            .then().extract().response().as(CertificateResponseDTO.class, getObjectMapperForDeserialization()).getCertificate();
    }


    protected Patient getPatientWithAddress(String expectedZipCode, String expectedStreet, String expectedCity, Patient currentPatient) {
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

    protected String createDraftAndReturnCertificateId(String moduleId, Patient patient) {
        final var createUtkastRequest = new CreateUtkastRequest();
        createUtkastRequest.setIntygType(moduleId);
        createUtkastRequest
            .setPatientPersonnummer(Personnummer.createPersonnummer(patient.getPersonId().getId()).orElseThrow());
        createUtkastRequest.setPatientFornamn(patient.getFirstName());
        createUtkastRequest.setPatientEfternamn(patient.getLastName());

        return given()
            .pathParam("certificateType", moduleId)
            .contentType(ContentType.JSON)
            .body(createUtkastRequest)
            .expect().statusCode(200)
            .when()
            .post("api/utkast/{certificateType}")
            .then().extract().path("intygsId").toString();
    }

    protected void assertNumberOfPdlMessages(int expectedPdlLogMessageCount) {
        final var pdlLogMessageCount = getPdlLogMessageCountFromQueue();
        assertEquals(expectedPdlLogMessageCount, pdlLogMessageCount);
    }

    protected int getPdlLogMessageCountFromQueue() {
        return Integer.parseInt(given().when().get("testability/logMessages/count").then().extract().response().body().asString());
    }

    protected void assertPdlLogMessage(ActivityType expectedActivityType, String certificateId) {
        assertPdlLogMessage(expectedActivityType, certificateId, null);
    }

    protected void assertPdlLogMessage(ActivityType expectedActivityType, String certificateId, String activityArgs) {
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

    protected PdlLogMessage getPdlLogMessageFromQueue() {
        return given().when().get("testability/logMessages").then().extract().response().as(PdlLogMessage.class);
    }

    protected void incrementVersion(Certificate certificate) {
        certificate.getMetadata().setVersion(certificate.getMetadata().getVersion() + 1);
    }

    protected static String getLatestMinorTextVersion(String certificateType, String majorVersion) {
        return getTextVersionForType(certificateType, majorVersion, 0);
    }

    protected static String getPreviousMinorTextVersion(String certificateType, String majorVersion) {
        return getTextVersionForType(certificateType, majorVersion, 1);
    }

    private static String getTextVersionForType(String certificateType, String majorVersion, int skip) {
        return getSupportedCertificateTypes().stream()
            .filter(type -> type.getInternalType().equals(certificateType))
            .limit(1)
            .flatMap(f -> f.getVersions().stream())
            .filter(majorVersion != null ? version -> version.split("\\.")[0].equals(majorVersion) : version -> true)
            .map(Double::parseDouble)
            .sorted(Comparator.reverseOrder())
            .skip(skip)
            .max(Comparator.naturalOrder()).orElseThrow().toString();
    }

    private static List<CertificateType> getSupportedCertificateTypes() {
        return given()
            .when().get("/testability/certificate/types")
            .then().statusCode(HttpStatus.OK.value())
            .extract().body().jsonPath().getList("certificateTypes", CertificateType.class);
    }
}
