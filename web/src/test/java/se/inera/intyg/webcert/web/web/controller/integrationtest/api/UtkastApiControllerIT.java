package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import junit.framework.Assert;
import org.junit.Test;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by marced on 17/11/15.
 */
public class UtkastApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetFk7263Utkast() {
        testGetUtkast("fk7263");
    }

    @Test
    public void testGetTsBasUtkast() {
        testGetUtkast("ts-bas");
    }

    @Test
    public void testGetTsDiabetesUtkast() {
        testGetUtkast("ts-diabetes");
    }

    /**
     * Generic method that created an utkast of given type and validates basic generic model properties
     *
     * @param utkastType The type of utkast to create
     */
    private void testGetUtkast(String utkastType) {

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        CreateUtkastRequest utkastRequest = createUtkastRequest(utkastType, DEFAULT_PATIENT_PERSONNUMMER);

        Response response = given().contentType(ContentType.JSON).body(utkastRequest).expect().statusCode(200).when().post("api/utkast/fk7263").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json")).
                body("intygsTyp", equalTo(utkastRequest.getIntygType())).
                body("skapadAv.hsaId", equalTo(DEFAULT_LAKARE.getHsaId())).
                body("enhetsId", equalTo(DEFAULT_LAKARE.getEnhetId())).
                body("version", equalTo(0)).
                body("skapadAv.namn", equalTo(DEFAULT_LAKARE.getFornamn() + " " + DEFAULT_LAKARE.getEfternamn())).extract().response();

        //The type-specific model is a serialized json "within" the model property, need to extract that first and then we can assert some basic things.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertTrue(model.getString("id").length() > 0);

        assertEquals(utkastRequest.getPatientPersonnummer().getPersonnummer(), model.getString("grundData.patient.personId"));
        assertEquals(utkastRequest.getPatientFornamn(), model.getString("grundData.patient.fornamn"));
        assertEquals(utkastRequest.getPatientEfternamn(), model.getString("grundData.patient.efternamn"));
    }

    /**
     * Verify that a lakare with a saved utkast is returned when querying for that.
     */
    @Test
    public void testGetLakareWithDraftsByEnheter() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        Lakare[] lakareWithUtkast =
                given().expect().statusCode(200).when().get("api/utkast/lakare").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-lakare-med-utkast-response-schema.json")).extract().response().as(Lakare[].class);

        Assert.assertEquals(1, lakareWithUtkast.length);

        Assert.assertEquals(DEFAULT_LAKARE.getHsaId(), lakareWithUtkast[0].getHsaId());
    }

    /**
     * Verify that filtering by enhetId and hsaId returns expected results.
     */
    @Test
    public void testFilterDraftsForUnit() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);


        QueryIntygResponse queryResponse = given().param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId()).
                expect().statusCode(200).
                when().
                get("api/utkast").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json")).
                body("totalCount", equalTo(1)).extract().response().as(QueryIntygResponse.class);

        //The only result should match the utkast we created in the setup
        Assert.assertEquals(utkastId, queryResponse.getResults().get(0).getIntygId());
        Assert.assertEquals("fk7263", queryResponse.getResults().get(0).getIntygType());
        Assert.assertEquals(DEFAULT_PATIENT_PERSONNUMMER, queryResponse.getResults().get(0).getPatientId().getPersonnummer());


    }
}
