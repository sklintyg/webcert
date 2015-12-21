package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.sessionId;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class FragaSvarModuleApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testFragaSvarForIntyg() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().pathParameters("intygsTyp", DEFAULT_INTYGSTYP, "intygsId", intygId).expect().statusCode(200).when()
                .get("moduleapi/fragasvar/{intygsTyp}/{intygsId}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-for-intyg-schema.json"))
                .body("$", hasSize(1));

        deleteQuestion(internId);
    }

    @Test
    public void testAnswer() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        // Setup a question that we can use to answer
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().contentType(ContentType.JSON).pathParams("intygsTyp", DEFAULT_INTYGSTYP, "fragasvarId", internId).body("svarsText")
                .expect().statusCode(200).when().put("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/besvara");

        deleteQuestion(internId);
    }

    // @Test
    public void testSetDispatchState() {
        given().expect().statusCode(200).when().put("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/hanterad");
    }

    // @Test
    public void createQuestion() {
        given().expect().statusCode(200).when().post("moduleapi/fragasvar/{intygsTyp}/{intygsId}");
    }

    // @Test
    public void closeAsHandled() {
        given().expect().statusCode(200).when().get("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/stang");
    }

    // @Test
    public void closeQAsAsHandled() {
        given().expect().statusCode(200).when().put("moduleapi/fragasvar/stang");
    }

    // @Test
    public void openAsUnhandled() {
        given().expect().statusCode(200).when().get("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/oppna");
    }

}
