package se.inera.intyg.webcert.web.web.controller.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.FakeCredentials;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;

import javax.servlet.http.HttpServletResponse;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

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

    protected final String DEFAULT_PATIENT_PERSONNUMMER = "191212121212";
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
        //Remove all utkast after each test
        given().expect().statusCode(200).when().delete("testability/intyg");
    }

    /**
     * Log in to webcert using the supplied FakeCredentials.
     *
     * @param fakeCredentials who to log in as
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
     * @param fakeElegCredentials who to log in as
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
     * Change the role for the current session.
     * This method required that a session is already established.
     *
     * @param newRole
     */
    protected void changeRoleTo(String newRole) {
        given().pathParam("role", newRole).expect().statusCode(200).when().get("authtestability/roles/userrole/{role}");
    }

    /**
     * Helper method to create an utkast of a given type for a given patient.
     * The request will be made with the current auth session.
     *
     * @param intygsTyp           Type to create
     * @param patientPersonNummer the patient to create the utkast for
     * @return Id for the new utkast
     */
    protected String createUtkast(String intygsTyp, String patientPersonNummer) {
        CreateUtkastRequest utkastRequest = createUtkastRequest(intygsTyp, patientPersonNummer);

        Response response = given().pathParam("intygstyp", intygsTyp).contentType(ContentType.JSON).body(utkastRequest).
                expect().statusCode(200).when().post("api/utkast/{intygstyp}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json")).
                body("intygsTyp", equalTo(utkastRequest.getIntygType())).extract().response();

        //The type-specific model is a serialized json within the model property, need to extract that first.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertEquals(patientPersonNummer, model.getString("grundData.patient.personId"));

        final String utkastId = model.getString("id");
        assertTrue(utkastId.length() > 0);

        return utkastId;
    }

    /**
     * Create Utkast Request with default values for all but type and patient
     *
     * @param intygsType          type to create
     * @param patientPersonNummer patient to create it for
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

}
