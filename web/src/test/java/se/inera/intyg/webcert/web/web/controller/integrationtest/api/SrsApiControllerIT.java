package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * A simple integration test of the webcert rest api that provides SRS services for webcert frontend.
 *
 * Created by carlf on 08/09/17.
 */
public class SrsApiControllerIT extends BaseRestIntegrationTest {

    private static final int OK = 200;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final String SUPPORTED_DIAGNOSIS_CODE = "J20";

    @Test
    public void getSrsSimpleTest() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String personnummer = "19121212-1212";
        String utkastId = createUtkast("luse", personnummer);


        List<SrsQuestionResponse> body = Arrays.asList(
                SrsQuestionResponse.create("questionId123123", "answerId321321"));

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("intygId", utkastId)
                .pathParam("personnummer", personnummer)
                .pathParam("diagnosisCode", SUPPORTED_DIAGNOSIS_CODE)
                .queryParam("prediktion", "true")
                .queryParam("atgard", "true")
                .queryParam("statistik", "true")
                .with().contentType(ContentType.JSON).and().body(body)
                .expect().statusCode(OK)
                .when().post("api/srs/{intygId}/{personnummer}/{diagnosisCode}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-srs-response-schema.json"));
    }

    @Test
    public void getSrsShouldRejectInvalidJson() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String body = "{adsfasdf";

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("intygId", "asdfasdf")
                .pathParam("personnummer", "asdfasdf")
                .pathParam("diagnosisCode", "asdfasdf")
                .with().contentType(ContentType.JSON).and().body(body)
                // Semantically incorrect, should be changed to 400, BAD_REQUEST.
                .expect().statusCode(INTERNAL_SERVER_ERROR)
                .when().post("api/srs/{intygId}/{personnummer}/{diagnosisCode}");
    }

    @Test
    public void getSrsShouldRejectIfMissingMandatoryHeaders() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        List<SrsQuestionResponse> body = Arrays.asList(
                SrsQuestionResponse.create("questionId123123", "answerId321321"));

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("intygId", "asdfasdf")
                .pathParam("personnummer", "asdfasdf")
                .pathParam("diagnosisCode", "asdfasdf")
                .with().contentType(ContentType.JSON).and().body(body)
                .expect().statusCode(BAD_REQUEST)
                .when().post("api/srs/{intygId}/{personnummer}/{diagnosisCode}");
    }

    @Test
    public void getSrsShouldRejectIfIntygDoesntExist() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        List<SrsQuestionResponse> body = Arrays.asList(
                SrsQuestionResponse.create("questionId123123", "answerId321321"));

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("intygId", "asdfasdf")
                .pathParam("personnummer", "asdfasdf")
                .pathParam("diagnosisCode", "asdfasdf")
                .with().contentType(ContentType.JSON).and().body(body)
                // Semantically incorrect, should be changed to 404, NOT_FOUND.
                .expect().statusCode(BAD_REQUEST)
                .when().post("api/srs/{intygId}/{personnummer}/{diagnosisCode}");
    }

    @Test
    public void getQuestionsSimpleTest() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("diagnosisCode", SUPPORTED_DIAGNOSIS_CODE)
                .expect().statusCode(OK)
                .when().get("api/srs/questions/{diagnosisCode}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-srs-question-schema.json"));
    }

    @Test
    public void getConsentSimpleTest() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("personnummer", "19121212-1212")
                .pathParam("hsaId", DEFAULT_LAKARE.getHsaId())
                .expect().statusCode(OK)
                .when().get("api/srs/consent/{personnummer}/{hsaId}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-srs-samtyckesstatus-schema.json"));
    }

    @Test
    public void setConsentSimpleTest() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String body = "\"true\"";

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParam("personnummer", "19121212-1212")
                .pathParam("hsaId", DEFAULT_LAKARE.getHsaId())
                .with().contentType(ContentType.JSON).and().body(body)
                .expect().statusCode(OK)
                .when().put("api/srs/consent/{personnummer}/{hsaId}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-srs-resultcodeenum-schema.json"));
    }

    @Test
    public void getDiagnosisCodesSimpleTest(){
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(OK)
                .when().get("api/srs/codes")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-srs-listofcodes-schema.json"));
    }
}
