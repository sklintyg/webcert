/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Status;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;

/**
 * Base class for "REST-ish" integrationTests using RestAssured.
 * <p/>
 * Created by marced on 19/11/15.
 */
public abstract class BaseRestIntegrationTest {

    private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
    private static final String FAKE_LOGIN_URI = "/fake";

    protected static FakeCredentials DEFAULT_LAKARE = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-1049", "rest", "testman",
            "IFV1239877878-1042").lakare(true).build();
    protected static final String DEFAULT_FRAGE_TEXT = "TEST_FRAGA";
    protected static final String DEFAULT_INTYGSTYP = "fk7263";

    protected final String DEFAULT_PATIENT_PERSONNUMMER = "19121212-1212";
    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * Common setup for all tests
     */
    @Before
    public void setup() {
        RestAssured.reset();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl");
    }

    /**
     * Common teardown for all tests
     */
    @After
    public void tearDown() {
        // Remove all utkast after each test
        given().expect().statusCode(200).when().delete("testability/intyg");
    }

    /**
     * Log in to webcert using the supplied FakeCredentials.
     *
     * @param fakeCredentials
     *            who to log in as
     * @return sessionId for the now authorized user session
     */
    protected String getAuthSession(FakeCredentials fakeCredentials) {
        String credentialsJson;
        try {
            credentialsJson = objectMapper.writeValueAsString(fakeCredentials);
            return getAuthSession(credentialsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Log in to webcert as a private practitioner using the supplied FakeElegCredentials.
     *
     * @param fakeElegCredentials
     *            who to log in as
     * @return sessionId for the now authorized user session
     */
    protected String getAuthSession(FakeElegCredentials fakeElegCredentials) {
        String credentialsJson;
        try {
            credentialsJson = objectMapper.writeValueAsString(fakeElegCredentials);
            return getAuthSession(credentialsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAuthSession(String credentialsJson) {
        Response response = given().contentType(ContentType.URLENC).and().redirects().follow(false).and()
                .formParam(USER_JSON_FORM_PARAMETER, credentialsJson).expect()
                .statusCode(HttpServletResponse.SC_FOUND).when()
                .post(FAKE_LOGIN_URI).then().extract().response();

        assertNotNull(response.sessionId());
        return response.sessionId();
    }

    /**
     * Change user's role for the current session.
     * This method require that a session is already established.
     *
     * @param newRole
     */
    protected void changeRoleTo(String newRole) {
        given().pathParam("role", newRole).expect().statusCode(200).when().get("authtestability/user/role/{role}");
    }

    /**
     * Change user's request origin for the current session.
     * This method require that a session is already established.
     *
     * @param newOrigin
     */
    protected void changeOriginTo(String newOrigin) {
        given().pathParam("origin", newOrigin).expect().statusCode(200).when().get("authtestability/user/origin/{origin}");
    }

    /**
     * Helper method to create an utkast of a given type for a given patient.
     * The request will be made with the current auth session.
     *
     * @param intygsTyp
     *            Type to create
     * @param patientPersonNummer
     *            the patient to create the utkast for
     * @return Id for the new utkast
     */
    protected String createUtkast(String intygsTyp, String patientPersonNummer) {
        CreateUtkastRequest utkastRequest = createUtkastRequest(intygsTyp, patientPersonNummer);

        Response response = given().pathParam("intygstyp", intygsTyp).contentType(ContentType.JSON).body(utkastRequest).expect().statusCode(200)
                .when().post("api/utkast/{intygstyp}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json"))
                .body("intygsTyp", equalTo(utkastRequest.getIntygType())).extract().response();

        // The type-specific model is a serialized json within the model property, need to extract that first.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertEquals(patientPersonNummer, model.getString("grundData.patient.personId"));

        final String utkastId = model.getString("id");
        assertTrue(utkastId.length() > 0);

        return utkastId;
    }

    /**
     * Create a intyg with status SIGNED
     *
     * @param intygsTyp
     * @param patientPersonNummer
     * @return
     */
    protected String createSignedIntyg(String intygsTyp, String patientPersonNummer) {

        // First create the draft
        final String utkastId = createUtkast(intygsTyp, patientPersonNummer);

        // ..then "fake" it to be signed. Maybe we should set more signature related metadata?
        given().pathParam("intygsId", utkastId).expect().statusCode(200).when().put("testability/intyg/{intygsId}/signerat");

        return utkastId;
    }

    /**
     * Create Utkast Request with default values for all but type and patient
     *
     * @param intygsType
     *            type to create
     * @param patientPersonNummer
     *            patient to create it for
     * @return a new CreateUtkastRequest
     */
    protected CreateUtkastRequest createUtkastRequest(String intygsType, String patientPersonNummer) {
        CreateUtkastRequest utkastRequest = new CreateUtkastRequest();
        utkastRequest.setIntygType(intygsType);
        utkastRequest.setPatientFornamn("Api");
        utkastRequest.setPatientEfternamn("Restman");
        utkastRequest.setPatientPersonnummer(new Personnummer(patientPersonNummer));
        utkastRequest.setPatientPostadress("Blåbärsvägen 14");
        utkastRequest.setPatientPostort("Molnet");
        utkastRequest.setPatientPostnummer("44837");
        return utkastRequest;
    }

    /**
     * Inserts a question for an existing certificate
     * 
     * @param intygsType
     *            type to create
     * @param patientPersonNummer
     *            patient to create it for
     * @return
     */
    protected int createQuestion(String typ, String intygId, String personnummer) {
        FragaSvar fs = createTestQuestion(typ, intygId, personnummer);

        Response response = given().contentType(ContentType.JSON)
                .body(fs).expect().statusCode(200).when()
                .post("testability/questions").then().extract().response();

        JsonPath model = new JsonPath(response.body().asString());
        return model.get("internReferens");
    }

    /**
     * Removes a question after using it for a test
     * 
     * @param internId
     *            internal id of the question to remove
     */
    protected void deleteQuestion(int internId) {
        given().pathParam("id", internId).expect().statusCode(200).when().delete("testability/questions/{id}");
    }

    /**
     * Creates a test question with information specified in most fields.
     * 
     * @param typ
     *            Certificate type of which the question refers to
     * @param intygId
     *            Certificate id of which the question refers to
     * @param personnummer
     *            Social security number of the patient the certificate is made out to
     * @return
     */
    private FragaSvar createTestQuestion(String typ, String intygId, String personnummer) {
        LocalDateTime now = LocalDateTime.now();
        FragaSvar fs = new FragaSvar();
        fs.setAmne(Amne.ARBETSTIDSFORLAGGNING);
        fs.setFrageText(DEFAULT_FRAGE_TEXT);
        fs.setIntygsReferens(new IntygsReferens(intygId, typ, new Personnummer(personnummer), "Api Restman", now));
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
        vardperson.setNamn(DEFAULT_LAKARE.getFornamn() + " " + DEFAULT_LAKARE.getEfternamn());
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

}
