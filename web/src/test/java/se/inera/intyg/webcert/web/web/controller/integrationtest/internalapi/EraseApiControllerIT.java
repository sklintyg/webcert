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
package se.inera.intyg.webcert.web.web.controller.integrationtest.internalapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType.MINIMAL;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.common.mapper.TypeRef;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.http.HttpStatus;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.IntegrationTest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.TestSetup;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;


@TestInstance(Lifecycle.PER_CLASS)
public class EraseApiControllerIT extends IntegrationTest {

    private static final String INTERNAL_BASE_URI = System.getProperty("integration.tests.actuatorUrl", "http://localhost:8120");
    private static final String ERASE_CERTIFICATES_URL = INTERNAL_BASE_URI + (INTERNAL_BASE_URI.endsWith("/") ? "" : "/")
        + "internalapi/v1/certificates/";
    private static final String VERSION = "1.3";

    private static final String EVENT_COUNT_URL = "testability/event/eventCount";
    private static final String ARENDE_COUNT_URL = "testability/arendetest/arendeCount";
    private static final String REFERENS_COUNT_URL = "testability/referens/referensCount";
    private static final String FRAGA_SVAR_COUNT_URL = "testability/fragasvar/fragaSvarCount";
    private static final String REDELIVERY_COUNT_URL = "testability/event/redeliveryCount";
    private static final String ARENDE_DRAFT_COUNT_URL = "testability/arendetest/arendeDraftCount";
    private static final String CERTIFICATE_EVENT_COUNT_URL = "testability/event/certificateEventCount";

    private static final String EVENTS_URL = "testability/event";
    private static final String ARENDEN_URL = "testability/arendetest/arende";
    private static final String FRAGA_SVAR_URL = "testability/fragasvar/byCertificateIds";
    private static final String REFERENSER_URL = "testability/referens";
    private static final String REDELIVERIES_URL = "testability/event/redelivery";
    private static final String ARENDE_DRAFTS_URL = "testability/arendetest/arendeDraft";
    private static final String CERTIFICATE_EVENTS_URL = "testability/event/certificateEvent";

    private static final int CERTIFICATE_COUNT = 10; // Must be an even number (e.g. 10, 12, 14...)
    private static final int INTEGRATED_UNIT_COUNT = 4;
    private static final int STATUS_UPDATE_RESULT_OK = 0;
    private static final int STATUS_UPDATE_RESULT_TECHNICAL_ERROR = 3;

    private static final TypeRef<List<IntegreradEnhetEntryWithSchemaVersion>> LIST_INTEGRATED_UNIT = new TypeRef<>() { };

    private static final Doctor ALFA = new Doctor(DR_AJLA, ALFA_REGIONEN, ALFA_REGIONEN_NAME, ALFA_VARDCENTRAL, ALFA_VARDCENTRAL_NAME,
        ATHENA_ANDERSSON, DR_AJLA_ALFA_VARDCENTRAL);
    private static final Doctor BETA = new Doctor(DR_BEATA, BETA_REGIONEN, BETA_REGIONEN_NAME, BETA_VARDCENTRAL, BETA_VARDCENTRAL_NAME,
        ALEXA_VALFRIDSSON, DR_BEATA_BETA_VARDCENTRAL);

    private final List<String> createdCertificateIds = new ArrayList<>();
    private final List<String> createdIntegratedUnitIds = new ArrayList<>();
    private final List<String> createdTermsApprovalsIds = new ArrayList<>();

    private List<IntegreradEnhetEntryWithSchemaVersion> rememberedIntegratedUnits = new ArrayList<>();

    @BeforeAll
    public void initiate() {
        configureRestAssured();
        rememberIntegratedUnits();
        resetNotificationStub();
        setNotificationStubResponse(STATUS_UPDATE_RESULT_TECHNICAL_ERROR);
        RestAssured.reset();
    }

    @BeforeEach
    public void setup() throws InterruptedException {
        configureRestAssured();
        setIntegratedUnits();
        resetNotificationStub();
        createTestData(ALFA);
    }

    @AfterEach
    public void tearDown() {
        clearCreatedCertificates();
        clearCreatedIntegratedUnits();
        clearCreatedTermsApprovals();
        clearCreated(REDELIVERIES_URL);
        clearCreated(EVENTS_URL);
        clearCreated(ARENDEN_URL);
        clearCreated(FRAGA_SVAR_URL);
        clearCreated(REFERENSER_URL);
        clearCreated(ARENDE_DRAFTS_URL);
        clearCreated(CERTIFICATE_EVENTS_URL);

        resetNotificationStub();

        createdCertificateIds.clear();
        createdIntegratedUnitIds.clear();
        createdTermsApprovalsIds.clear();

        RestAssured.reset();
    }

    @AfterAll
    public void cleanup() {
        configureRestAssured();
        restoreIntegratedUnits();
        setNotificationStubResponse(STATUS_UPDATE_RESULT_OK);
        RestAssured.reset();
    }

    @Test
    public void shouldEraseIntegratedUnits() {
        assertEquals(INTEGRATED_UNIT_COUNT + 1, getIntegratedUnitCount(ALFA));
        eraseCertificates(ALFA);
        assertEquals(0, getIntegratedUnitCount(ALFA));
    }

    @Test
    public void shouldEraseApprovedTerms() {
        assertTrue(getApprovedTerms(ALFA));
        eraseCertificates(ALFA);
        assertFalse(getApprovedTerms(ALFA));
    }

    @Test
    public void shouldEraseCertificates() {
        assertEquals(CERTIFICATE_COUNT, getCertificateCount(ALFA));
        eraseCertificates(ALFA);
        assertEquals(0, getCertificateCount(ALFA));
    }

    @Test
    public void shouldEraseRedeliveries() {
        assertEquals(CERTIFICATE_COUNT, getCount(ALFA, REDELIVERY_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, REDELIVERY_COUNT_URL));
    }

    @Test
    public void shouldEraseEvents() {
        assertEquals(CERTIFICATE_COUNT, getCount(ALFA, EVENT_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, EVENT_COUNT_URL));
    }

    @Test
    public void shouldEraseArendeDrafts() {
        assertEquals(CERTIFICATE_COUNT, getCount(ALFA, ARENDE_DRAFT_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, ARENDE_DRAFT_COUNT_URL));
    }

    @Test
    public void shouldEraseArenden() {
        assertEquals(CERTIFICATE_COUNT, getCount(ALFA, ARENDE_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, ARENDE_COUNT_URL));
    }

    @Test
    public void shouldEraseCertificateEvents() {
        assertEquals(CERTIFICATE_COUNT, getCount(ALFA, CERTIFICATE_EVENT_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, CERTIFICATE_EVENT_COUNT_URL));
    }

    @Test
    public void shouldEraseFragaSvar() {
        assertEquals(CERTIFICATE_COUNT / 2, getCount(ALFA, FRAGA_SVAR_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, FRAGA_SVAR_COUNT_URL));
    }

    @Test
    public void shouldEraseReferenser() {
        assertEquals(CERTIFICATE_COUNT / 2, getCount(ALFA, REFERENS_COUNT_URL));
        eraseCertificates(ALFA);
        assertEquals(0, getCount(ALFA, REFERENS_COUNT_URL));
    }

   @Test
    public void shouldNotEraseOtherCareProvider() {
        RestAssured.reset();
        configureRestAssured();
        createTestData(BETA);

        assertAll(
            () -> assertTrue(getApprovedTerms(BETA)),
            () -> assertEquals(CERTIFICATE_COUNT, getCertificateCount(BETA)),
            () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, REDELIVERY_COUNT_URL)),
            () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, EVENT_COUNT_URL)),
            () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, ARENDE_DRAFT_COUNT_URL)),
            () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, ARENDE_COUNT_URL)),
            () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, CERTIFICATE_EVENT_COUNT_URL)),
            () -> assertEquals(INTEGRATED_UNIT_COUNT + 1, getIntegratedUnitCount(BETA)),
            () -> assertEquals(CERTIFICATE_COUNT / 2, getCount(BETA, REFERENS_COUNT_URL)),
            () -> assertEquals(CERTIFICATE_COUNT / 2, getCount(BETA, FRAGA_SVAR_COUNT_URL))
        );

        eraseCertificates(ALFA);

       assertAll(
           () -> assertTrue(getApprovedTerms(BETA)),
           () -> assertEquals(CERTIFICATE_COUNT, getCertificateCount(BETA)),
           () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, REDELIVERY_COUNT_URL)),
           () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, EVENT_COUNT_URL)),
           () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, ARENDE_DRAFT_COUNT_URL)),
           () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, ARENDE_COUNT_URL)),
           () -> assertEquals(CERTIFICATE_COUNT, getCount(BETA, CERTIFICATE_EVENT_COUNT_URL)),
           () -> assertEquals(INTEGRATED_UNIT_COUNT + 1, getIntegratedUnitCount(BETA)),
           () -> assertEquals(CERTIFICATE_COUNT / 2, getCount(BETA, REFERENS_COUNT_URL)),
           () -> assertEquals(CERTIFICATE_COUNT / 2, getCount(BETA, FRAGA_SVAR_COUNT_URL))
       );
    }

    @Test
    public void shouldReturnOkResponseWhenNoCertificates() {
        assertEquals(0, getCertificateCount(BETA));
        eraseCertificates(BETA);
        assertEquals(0, getCertificateCount(BETA));
    }


    private void eraseCertificates(Doctor doctor) {
        given().pathParam("id", doctor.careProviderId)
            .when().delete(ERASE_CERTIFICATES_URL + "{id}")
            .then().statusCode(204);
    }

    private void configureRestAssured() {
        final var logConfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        RestAssured.config = RestAssured.config()
            .logConfig(logConfig)
            .sessionConfig(new SessionConfig("SESSION", null));
    }

    private void setIntegratedUnits() {
        final var integratedTestUnits =
            List.of(getIntegratedUnit(ALFA, null, null), getIntegratedUnit(BETA, null, null));

        deleteIntegratedUnits(integratedTestUnits);

        integratedTestUnits.forEach(testUnit -> given()
            .contentType(ContentType.JSON).body(testUnit)
            .when().post("/testability/integreradevardenheter")
            .then().statusCode(HttpStatus.OK.value()));
    }

    private void createTestData(Doctor doctor) {
        createCertificates(doctor);
        createTermsApproval(doctor);
        createIntegratedUnits(doctor);
    }

    private void createTermsApproval(Doctor doctor) {
        createdTermsApprovalsIds.add(doctor.careProviderId);
        given().pathParam("hsaId", doctor.careProviderId)
            .when().put("testability/anvandare/godkannavtal/{hsaId}")
            .then().statusCode(HttpStatus.OK.value());
    }

    private void createIntegratedUnits(Doctor doctor) {
        final var startIdExtension = doctor == ALFA ? 1000 : 2000;
        for (int i = startIdExtension; i < startIdExtension + INTEGRATED_UNIT_COUNT; i++) {
            final var unitId =  doctor.unitName + "-" + i;
            final var integratedUnit = getIntegratedUnit(null, doctor.careProviderId, unitId);
            createdIntegratedUnitIds.add(unitId);

            given()
                .contentType(ContentType.JSON).body(integratedUnit)
                .when().post("testability/integreradevardenheter")
                .then().statusCode(HttpStatus.OK.value());
        }
    }

    private void createCertificates(Doctor doctor) {
        TestSetup.create()
            .login(doctor.credentials)
            .setup();

        for (int i = 0; i < CERTIFICATE_COUNT / 2; i++) {
            createDraft(doctor);
            createCertificate(doctor);
        }
    }

    private void createDraft(Doctor doctor) {
        final var testSetup = TestSetup.create()
            .draft(LisjpEntryPoint.MODULE_ID, VERSION, MINIMAL, doctor.doctorId, doctor.unitId, doctor.patient.getPersonId().getId())
            .useDjupIntegratedOrigin()
            .questionWithAnswerDraft()
            .setup();

        createdCertificateIds.add(testSetup.certificateId());
        createReferens(testSetup.certificateId());
    }

    private void createCertificate(Doctor doctor) {
        final var testSetup = TestSetup.create()
            .certificate(LisjpEntryPoint.MODULE_ID, VERSION, doctor.unitId, doctor.doctorId, doctor.patient.getPersonId().getId())
            .useDjupIntegratedOrigin()
            .questionWithAnswerDraft()
            .sendCertificate()
            .setup();

        createdCertificateIds.add(testSetup.certificateId());
        createFragaSvar(doctor, testSetup);
    }

    private void createReferens(String certificateId) {
        final var referens = new Referens();
        referens.setIntygsId(certificateId);
        referens.setReferens("Referens for" + certificateId);
        given()
            .contentType(ContentType.JSON).body(referens)
            .when().post("testability/referens")
            .then().statusCode(HttpStatus.OK.value());
    }

    private void createFragaSvar(Doctor doctor, TestSetup testSetup) {
        final var fragaSvar = getFragaSvar(doctor, testSetup);
        given()
            .contentType(ContentType.JSON).body(fragaSvar)
            .when().post("/testability/fragasvar")
            .then().statusCode(HttpStatus.OK.value());
    }


    private Long getCount(Doctor doctor, String url) {
        final var cartificateIds = doctor == ALFA ? createdCertificateIds.subList(0, CERTIFICATE_COUNT)
            : createdCertificateIds.subList(CERTIFICATE_COUNT, CERTIFICATE_COUNT * 2);

        return given().contentType(ContentType.JSON).body(cartificateIds)
            .when().get(url).then().statusCode(HttpStatus.OK.value())
            .extract().body().as(Long.class);
    }

    private long getCertificateCount(Doctor doctor) {
        return given().pathParam("careProviderId", doctor.careProviderId)
            .when().get("testability/intyg/{careProviderId}/count")
            .then().statusCode(HttpStatus.OK.value()).extract().body().as(Long.class);
    }

    private long getIntegratedUnitCount(Doctor doctor) {
        return given()
            .when().get("testability/integreradevardenheter")
            .then().statusCode(HttpStatus.OK.value()).extract().body().as(LIST_INTEGRATED_UNIT)
            .stream().filter(unit -> unit.getVardgivareId().equals(doctor.careProviderId)).count();
    }

    private boolean getApprovedTerms(Doctor doctor) {
        return given().pathParam("hsaId", doctor.careProviderId)
            .when().get("testability/anvandare/approvedTerms/{hsaId}")
            .then().statusCode(HttpStatus.OK.value()).extract().body().as(Boolean.class);
    }


    private void clearCreated(String url) {
        given()
            .contentType(ContentType.JSON).body(createdCertificateIds)
            .when().delete(url)
            .then().statusCode(HttpStatus.OK.value());
    }

    private void clearCreatedTermsApprovals() {
        createdTermsApprovalsIds.forEach(id -> given()
            .pathParam("hsaId", id)
            .when().put("testability/anvandare/avgodkannavtal/{hsaId}")
            .then().statusCode(HttpStatus.OK.value())
        );
    }

    private void clearCreatedIntegratedUnits() {
        createdIntegratedUnitIds.forEach(id -> given()
            .pathParam("hsaId", id)
            .when().delete("testability/integreradevardenheter/{hsaId}")
            .then().statusCode(HttpStatus.OK.value())
        );
    }

    private void clearCreatedCertificates() {
        createdCertificateIds.forEach(certificateId ->
            given()
                .pathParam("certificateId", certificateId)
                .when().delete("/testability/intyg/{certificateId}")
                .then().statusCode(HttpStatus.OK.value())
        );
    }


    private void setNotificationStubResponse(int statusUpdateResponse) {
        given().pathParam("code", statusUpdateResponse)
            .when().get("/services/api/notification-api/notifieringar/v3/emulateError/{code}")
            .then().statusCode(HttpStatus.OK.value());
    }

    private void resetNotificationStub() {
        given()
            .when().post("/services/api/notification-api/clear")
            .then().statusCode(HttpStatus.NO_CONTENT.value());
    }


    private void rememberIntegratedUnits() {
        rememberedIntegratedUnits = given()
            .when().get("/testability/integreradevardenheter")
            .then().statusCode(HttpStatus.OK.value()).extract().body().as(LIST_INTEGRATED_UNIT)
            .stream().filter(unit -> unit.getEnhetsId().equals(ALFA.unitId) || unit.getEnhetsId().equals(BETA.unitId))
            .collect(Collectors.toList());

        if (!rememberedIntegratedUnits.isEmpty()) {
            deleteIntegratedUnits(rememberedIntegratedUnits);
        }
    }

    private void deleteIntegratedUnits(List<IntegreradEnhetEntryWithSchemaVersion> units) {
        units.forEach(unit -> given()
            .pathParam("unitId", unit.getEnhetsId())
            .when().delete("/testability/integreradevardenheter/{unitId}")
            .then().statusCode(HttpStatus.OK.value()));
    }

    private void restoreIntegratedUnits() {
        deleteIntegratedUnits(List.of(getIntegratedUnit(ALFA, null, null), getIntegratedUnit(BETA, null, null)));

        if (!rememberedIntegratedUnits.isEmpty()) {
            rememberedIntegratedUnits.forEach(unit -> given()
                .contentType(ContentType.JSON).body(unit)
                .when().post("/testability/integreradevardenheter")
                .then().statusCode(HttpStatus.OK.value()));
        }
    }


    private IntegreradEnhetEntryWithSchemaVersion getIntegratedUnit(Doctor doctor, String careProviderId, String unitId) {
        final var integratedUnit = new IntegreradEnhetEntryWithSchemaVersion();
        integratedUnit.setSchemaVersion("2.0");
        integratedUnit.setEnhetsId(doctor != null ? doctor.unitId : unitId);
        integratedUnit.setEnhetsNamn(doctor != null ? doctor.unitName : unitId);
        integratedUnit.setVardgivareId(doctor != null ? doctor.careProviderId : careProviderId);
        integratedUnit.setVardgivareNamn(doctor != null ? doctor.careProviderName : careProviderId);
        return integratedUnit;
    }

    private FragaSvar getFragaSvar(Doctor doctor, TestSetup testSetup) {
        FragaSvar fs = new FragaSvar();
        fs.setFrageSigneringsDatum(LocalDateTime.now());
        fs.setFrageSkickadDatum(LocalDateTime.now());
        fs.setStatus(Status.PENDING_INTERNAL_ACTION);
        fs.setExternaKontakter(new HashSet<>(Arrays.asList("Testperson1 FK", "Testperson2 FK")));
        fs.setExternReferens("Extern referens");
        fs.setSistaDatumForSvar(LocalDate.now().plusWeeks(2));
        fs.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fs.setFrageText("Detta är frågan");
        fs.setIntygsReferens(getIntygsReferens(testSetup));
        fs.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fs.setKompletteringar(new HashSet<>(List.of(getKomplettering())));
        fs.setVardperson(FragaSvarConverter.convert(getHoSPersonal(doctor)));
        fs.setMeddelandeRubrik("FragaSvar from EraseApiControllerIT");
        fs.setVidarebefordrad(false);
        return fs;
    }

    private Komplettering getKomplettering() {
        Komplettering kompl1 = new Komplettering();
        kompl1.setFalt("fält");
        kompl1.setText("kompletteringstext");
        return kompl1;
    }

    private IntygsReferens getIntygsReferens(TestSetup testSetup) {
        final var metadata = testSetup.certificate().getMetadata();
        return new IntygsReferens(testSetup.certificateId(), metadata.getType(),
            Personnummer.createPersonnummer(metadata.getPatient().getPersonId().getId()).orElseThrow(),
            metadata.getPatient().getFullName(), LocalDateTime.now());
    }

    private HoSPersonal getHoSPersonal(Doctor doctor) {
        final var hoSPerson = new HoSPersonal();
        hoSPerson.setFullstandigtNamn("Doctor Name");
        hoSPerson.setPersonId(doctor.doctorId);
        hoSPerson.setMedarbetarUppdrag("Vård och behandling");
        hoSPerson.setVardenhet(getCareUnit(doctor));
        return hoSPerson;
    }

    private Vardenhet getCareUnit(Doctor doctor) {
        final var careUnit = new Vardenhet();
        careUnit.setEnhetsid(doctor.unitId);
        careUnit.setEnhetsnamn(doctor.unitName);
        careUnit.setVardgivare(getCareProvider(doctor));
        return careUnit;
    }

    private Vardgivare getCareProvider(Doctor doctor) {
        final var careProvider = new Vardgivare();
        careProvider.setVardgivarid(doctor.careProviderId);
        careProvider.setVardgivarnamn(doctor.careProviderName);
        return careProvider;
    }

    private static final class Doctor {
        public final String doctorId;
        public final String careProviderId;
        public final String careProviderName;
        public final String unitId;
        public final String unitName;
        public final Patient patient;
        public final FakeCredentials credentials;

        Doctor(String doctorId, String careProviderId, String careProviderName, String unitId, String unitName, Patient patient,
            FakeCredentials credentials) {
            this.doctorId = doctorId;
            this.careProviderId = careProviderId;
            this.careProviderName = careProviderName;
            this.unitId = unitId;
            this.unitName = unitName;
            this.patient = patient;
            this.credentials = credentials;
        }
    }
}
