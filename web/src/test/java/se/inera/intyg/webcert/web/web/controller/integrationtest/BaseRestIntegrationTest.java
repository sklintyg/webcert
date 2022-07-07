/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.auth.common.FakeCredential;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;

/**
 * Base class for "REST-ish" integrationTests using RestAssured.
 *
 * Created by marced on 19/11/15.
 */
public abstract class BaseRestIntegrationTest {

    // INTYG-4086 changes these to be the actual values stored for 190101010101 in the PU stub.
    protected static final String DEFAULT_UTKAST_PATIENT_FORNAMN = "Nollett";
    protected static final String DEFAULT_UTKAST_PATIENT_EFTERNAMN = "Nollettsson";
    protected static final String DEFAULT_FRAGE_TEXT = "TEST_FRAGA";
    protected static final String DEFAULT_INTYGSTYP = "lisjp";

    protected static final String FK7263_BASE_INTYG_TYPE_VERSION = "1.0";
    protected static final String TS_BAS_BASE_INTYG_TYPE_VERSION = "7.0";
    protected static final String LUSE_BASE_INTYG_TYPE_VERSION = "1.3";

    private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
    private static final String FAKE_LOGIN_URI = "/fake";

    private static final List<String> LAKARE = Collections.singletonList("Läkare");

    /**
     * Use to create a ROUTEID cookie to ensure the correct tomcat-node is used
     */
    public static String routeId;

    protected static String csrfToken;

    protected static FakeCredentials DEFAULT_LAKARE = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-1049",
        "IFV1239877878-1042").legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.NORMAL.name()).build();

    protected static FakeCredentials LEONIE_KOEHL = new FakeCredentials.FakeCredentialsBuilder("TSTNMT2321000156-103F",
        "TSTNMT2321000156-1039").legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.NORMAL.name()).build();


    /**
     * Has multiple vardenheter.
     */
    protected static FakeCredentials ASA_ANDERSSON = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-104B",
        "IFV1239877878-1046").legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.NORMAL.name()).build();

    protected static FakeCredentials STAFFAN_STAFETT = new FakeCredentials.FakeCredentialsBuilder("staffan", "")
        .legitimeradeYrkesgrupper(LAKARE).origin(UserOriginType.NORMAL.name()).build();

    protected final String DEFAULT_PATIENT_PERSONNUMMER = "19010101-0101";

    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * Common setup for all tests
     */
    @Before
    public void setupBase() throws FileNotFoundException {
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl", "http://localhost:8020");
        LogConfig logconfig = new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails().enablePrettyPrinting(true);
        RestAssured.config = RestAssured.config()
            .logConfig(logconfig)
            .sessionConfig(new SessionConfig("SESSION", null));
    }

    /**
     * Common teardown for all tests
     */
    @After
    public void tearDown() {
        // Remove all utkast after each test
        given().expect().statusCode(200).when().delete("testability/intyg");
        RestAssured.reset();
    }

    /**
     * Log in to webcert using the supplied FakeCredentials.
     *
     * @param fakeCredential who to log in as
     * @return sessionId for the now authorized user session
     */
    protected String getAuthSession(FakeCredential fakeCredential) {
        try {
            return getAuthSession(objectMapper.writeValueAsString(fakeCredential));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAuthSession(String credentialsJson) {
        Response response = given().contentType(ContentType.URLENC).and().redirects().follow(false).and()
            .formParam(USER_JSON_FORM_PARAMETER, credentialsJson)
            .expect().statusCode(HttpServletResponse.SC_FOUND)
            .when().post(FAKE_LOGIN_URI)
            .then().extract().response();

        assertNotNull(response.sessionId());
        routeId = response.getCookie("ROUTEID") != null ? response.getCookie("ROUTEID") : "nah";
        csrfToken = response.getCookie("XSRF-TOKEN");

        return response.sessionId();
    }


    /**
     * Change user's role for the current session.
     * This method require that a session is already established.
     */
    protected void changeRoleTo(String newRole) {
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParam("role", newRole)
            .expect().statusCode(200)
            .when().get("authtestability/user/role/{role}");
    }

    /**
     * Change user's request origin for the current session.
     * This method require that a session is already established.
     */
    protected void changeOriginTo(String newOrigin) {
        spec()
            .pathParam("origin", newOrigin)
            .expect().statusCode(200)
            .when().get("authtestability/user/origin/{origin}");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Sets the coherentJournaling flag in the integration parameters to true for the current session.
     */
    protected void setSjf() {
        spec()
            .expect()
            .statusCode(200)
            .when()
            .post("authtestability/user/parameters/sjf");
    }

    /**
     * Helper method to create an utkast of a given type for a given patient.
     * The request will be made with the current auth session.
     *
     * @param intygsTyp Type to create
     * @param patientPersonNummer the patient to create the utkast for
     * @return Id for the new utkast
     */
    protected String createUtkast(String intygsTyp, String patientPersonNummer) {
        CreateUtkastRequest utkastRequest = createUtkastRequest(intygsTyp, patientPersonNummer);
        return createUtkast(utkastRequest);
    }

    /**
     * Helper method to create an utkast of a given type for a given patient.
     * The request will be made with the current auth session.
     *
     * @return Id for the new utkast
     */
    protected String createUtkast(CreateUtkastRequest utkastRequest) {
        Response response = spec()
            .pathParam("intygstyp", utkastRequest.getIntygType()).contentType(ContentType.JSON).body(utkastRequest)
            .expect().statusCode(200)
            .when().post("api/utkast/{intygstyp}")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json"))
            .body("intygsTyp", equalTo(utkastRequest.getIntygType())).extract().response();

        // The type-specific model is a serialized json within the model property, need to extract that first.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertEquals(formatPersonnummer(utkastRequest.getPatientPersonnummer().getPersonnummer()),
            model.getString("grundData.patient.personId"));

        final String utkastId = model.getString("id");
        assertTrue(utkastId.length() > 0);

        return utkastId;
    }

    /**
     * Creates a new sent intyg, with a komplettering relation to the given intyg id. It is assumed that the specified
     * intyg already exists (and is signed & sent), otherwise setting the relation will fail.
     */
    protected String createSentIntygAsKompletteringToIntyg(String earlierSentIntygId, String intygTyp,
        String patientPersonnummer) {

        // Create Utkast not Intyg, since relation data needs to be set before signing with new certificates like luse
        String utkastId = createUtkast(intygTyp, patientPersonnummer);

        // Mark as komplettering for the given id
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .pathParam("intygsId", utkastId).body(earlierSentIntygId)
            .expect().statusCode(200)
            .when().put("testability/intyg/{intygsId}/kompletterarintyg");

        // Sign and send.
        signUtkast(utkastId);
        String intygId = utkastId;
        sendIntyg(intygId);

        return intygId;

    }

    /**
     * Create a intyg with status SIGNED
     */
    protected String createSignedIntyg(String intygsTyp, String patientPersonNummer) {

        // First create the draft
        final String utkastId = createUtkast(intygsTyp, patientPersonNummer);

        // ..then "fake" it to be signed.
        signUtkast(utkastId);

        return utkastId;
    }

    /**
     * Create Utkast Request with default values for all but type and patient
     *
     * @param intygsType type to create
     * @param patientPersonNummer patient to create it for
     * @return a new CreateUtkastRequest
     */
    protected CreateUtkastRequest createUtkastRequest(String intygsType, String patientPersonNummer) {
        CreateUtkastRequest utkastRequest = new CreateUtkastRequest();
        utkastRequest.setIntygType(intygsType);
        utkastRequest.setPatientFornamn(DEFAULT_UTKAST_PATIENT_FORNAMN);
        utkastRequest.setPatientEfternamn(DEFAULT_UTKAST_PATIENT_EFTERNAMN);
        utkastRequest.setPatientPersonnummer(Personnummer.createPersonnummer(patientPersonNummer).get());
        utkastRequest.setPatientPostadress("Blåbärsvägen 14");
        utkastRequest.setPatientPostort("Molnet");
        utkastRequest.setPatientPostnummer("44837");
        return utkastRequest;
    }

    /**
     * Inserts a question for an existing certificate
     *
     * @param typ type to create
     * @param intygId id of the intyg to create the question for
     * @param personnummer patient to create it for
     */
    protected int createQuestion(String typ, String intygId, String personnummer) {
        return createQuestion(typ, intygId, personnummer, Amne.ARBETSTIDSFORLAGGNING);
    }

    protected int createQuestion(final String typ, final String intygId, final String personnummer, final Amne amne) {
        FragaSvar fs = createTestQuestion(typ, intygId, personnummer, amne);

        Response response = spec()
            .contentType(ContentType.JSON).body(fs)
            .expect().statusCode(200)
            .when().post("testability/fragasvar")
            .then().extract().response();

        JsonPath model = new JsonPath(response.body().asString());
        return model.get("internReferens");
    }

    /**
     * Inserts a question of type arende for an existing certificate
     *
     * @param intygTyp type to create
     * @param intygsId id of the intyg to create the arende for
     * @param personnummer patient to create it for
     */
    protected String createArendeQuestion(String intygTyp, String intygsId, String personnummer, ArendeAmne messageType) {
        Arende arende;
        switch (messageType) {
            case AVSTMN:
                arende = createAvstamningArendeFromFktoWebcertUser(intygTyp, intygsId, personnummer);
                break;
            case KOMPLT:
                arende = createKompletteringArendeFromFkToWebcertUser(intygTyp, intygsId, personnummer);
                break;
            default:
                throw new IllegalArgumentException();
        }

        Response response = spec()
            .body(arende)
            .expect().statusCode(200)
            .when().post("testability/arendetest")
            .then().extract().response();

        JsonPath model = new JsonPath(response.body().asString());
        return model.get("meddelandeId");
    }

    /**
     * Removes a question after using it for a test
     *
     * @param internId internal id of the question to remove
     */
    protected void deleteQuestion(int internId) {
        spec().pathParam("id", internId)
            .expect().statusCode(200)
            .when().delete("testability/fragasvar/{id}");
    }

    protected void deleteQuestionsByEnhet(String enhetsId) {
        spec().pathParam("enhetsId", enhetsId)
            .expect().statusCode(200)
            .when().delete("testability/fragasvar/enhet/{enhetsId}");
    }

    /**
     * Creates a test question with information specified in most fields.
     *
     * @param typ Certificate type of which the question refers to
     * @param intygId Certificate id of which the question refers to
     * @param personnummer Social security number of the patient the certificate is made out to
     */
    private FragaSvar createTestQuestion(String typ, String intygId, String personnummer, Amne amne) {
        LocalDateTime now = LocalDateTime.now();
        FragaSvar fs = new FragaSvar();
        fs.setAmne(amne);
        fs.setFrageText(DEFAULT_FRAGE_TEXT);
        fs.setIntygsReferens(new IntygsReferens(intygId, typ,
            Personnummer.createPersonnummer(personnummer).get(), "Api Restman", now));
        fs.setStatus(Status.PENDING_INTERNAL_ACTION);
        fs.setFrageSkickadDatum(now);
        fs.setMeddelandeRubrik("Meddelanderubrik");
        fs.setFrageStallare("FK");
        fs.setFrageSigneringsDatum(now);
        fs.setVardAktorNamn("Vardaktor");
        fs.setVardAktorHsaId("Test-hsa-id");
        fs.setExternReferens("FK-REF-1");

        fs.setExternaKontakter(new HashSet<String>() {
            {
                add("kontakt-1");
                add("kontakt-2");
            }
        });

        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(DEFAULT_LAKARE.getEnhetId());
        vardperson.setArbetsplatsKod("0000000");
        vardperson.setEnhetsnamn("blub");
        vardperson.setHsaId(DEFAULT_LAKARE.getHsaId());
        vardperson.setVardgivarId("TESTVG");
        vardperson.setVardgivarnamn("VG TEST SYD");
        vardperson.setNamn(DEFAULT_LAKARE.getForNamn() + " " + DEFAULT_LAKARE.getEfterNamn());
        fs.setVardperson(vardperson);

        Komplettering komplettering = new Komplettering();
        komplettering.setFalt("test-falt-1");
        komplettering.setText("Detta är helt galet. Gör om, gör rätt.");
        fs.setKompletteringar(new HashSet<Komplettering>() {
            {
                add(komplettering);
            }
        });
        return fs;
    }

    /**
     * Creates a test question of type arende with information specified in most fields.
     *
     * @param intygTyp Certificate type of which the question refers to
     * @param intygsId Certificate id of which the question refers to
     * @param personnummer Social security number of the patient the certificate is made out to
     */
    private Arende createAvstamningArendeFromFktoWebcertUser(String intygTyp, String intygsId, String personnummer) {
        LocalDateTime now = LocalDateTime.now();
        Arende arende = new Arende();
        arende.setAmne(ArendeAmne.AVSTMN);
        arende.setMeddelande(DEFAULT_FRAGE_TEXT);
        arende.setIntygsId(intygsId);
        arende.setPatientPersonId(personnummer);
        arende.setIntygTyp(intygTyp);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setSenasteHandelse(now);
        arende.setSkickatTidpunkt(now);
        arende.setTimestamp(now);
        arende.setRubrik("Meddelanderubrik");
        arende.setSkickatAv("FK");
        arende.setSigneratAvName("Jan Nilsson");
        arende.setSigneratAv(DEFAULT_LAKARE.getHsaId());
        arende.setReferensId("FK-REF-1");
        arende.setEnhetId(DEFAULT_LAKARE.getEnhetId());
        arende.setEnhetName("Enhetsnamn");
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setKomplettering(new ArrayList<>());
        return arende;
    }

    private Arende createKompletteringArendeFromFkToWebcertUser(String intygTyp, String intygsId, String personnummer) {
        LocalDateTime now = LocalDateTime.now();
        Arende arende = new Arende();
        arende.setAmne(ArendeAmne.KOMPLT);
        arende.setMeddelande(DEFAULT_FRAGE_TEXT);
        arende.setIntygsId(intygsId);
        arende.setPatientPersonId(personnummer);
        arende.setIntygTyp(intygTyp);
        arende.setStatus(Status.CLOSED);
        arende.setSenasteHandelse(now);
        arende.setSkickatTidpunkt(now);
        arende.setTimestamp(now);
        arende.setRubrik("Komplettering, arende fran FK");
        arende.setSkickatAv("FK");
        arende.setSigneratAvName("Jan Nilsson");
        arende.setSigneratAv(DEFAULT_LAKARE.getHsaId());
        arende.setReferensId("FK-REF-2");
        arende.setEnhetId(DEFAULT_LAKARE.getEnhetId());
        arende.setEnhetName("Enhetsnamn");
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setKomplettering(Collections.emptyList());
        /*
         * Due to problem with (json) deserialization in the testability api because of
         *
         * @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
         * in Arende.java, this property is not set
         *
         */
        // arende.setSistaDatumForSvar(now.toLocalDate().plusDays(7));
        return arende;

    }

    /**
     * Return a personnummer on the form yyyyMMddNNNN
     *
     * @param patientPersonnummer the patient id
     * @return a formatted personnummer
     */
    protected String formatPersonnummer(String patientPersonnummer) {
        Personnummer personnummer = Personnummer
            .createPersonnummer(patientPersonnummer)
            .orElseThrow(() -> new IllegalArgumentException("Could not create personnummer with id: " + patientPersonnummer));

        return personnummer.getPersonnummer();
    }

    /**
     * Marks the intyg as sent
     *
     * @param intygId the internal reference to the intyg to be marked
     */
    protected void sendIntyg(String intygId) {
        spec()
            .pathParams("id", intygId)
            .expect().statusCode(200)
            .when().put("/testability/intyg/{id}/skickat");
    }

    /**
     * Creates a 'fake signature' for the given utkast, which is now for all (?) intents and purposes, an intyg.
     */
    protected void signUtkast(String utkastId) {
        // Maybe we should set more signature related metadata?
        spec()
            .pathParam("intygsId", utkastId).body(DEFAULT_LAKARE.getHsaId())
            .expect().statusCode(200)
            .when().put("testability/intyg/{intygsId}/signerat");
    }

    /**
     * Returns a request spec prefix with route and session cookies as well as content-type.
     *
     * @return the spec.
     */
    protected RequestSpecification spec() {
        RequestSpecification spec = given().cookie("ROUTEID", routeId)
            .contentType(ContentType.JSON);
        if (!Strings.isNullOrEmpty(csrfToken)) {
            spec
                .cookie("XSRF-TOKEN", csrfToken)
                .header("X-XSRF-TOKEN", csrfToken); // Usually set by angularjs, using value from cookie.
        }
        return spec;
    }

    void sleep(long milllis) {
        try {
            Thread.sleep(milllis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected RequestSpecification spec(long delayInMillis) {
        sleep(delayInMillis);
        return spec();
    }

}
