package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Very basic test of the FMB API that requests an icd10 code known to have fmb info and one that doesn't.
 */
public class FmbAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void getFmbForKnownIcd10() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        //J22 is added programatically by the fmbStub ad also used by other tests.
        given().pathParam("icd10", "J22").expect().statusCode(200).when().get("api/fmb/{icd10}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fmb-query-response-schema.json")).
                body("icd10Code", equalTo("J22")).
                body("forms", hasSize(4));
    }

    @Test
    public void getFmbForUnKnownIcd10() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().pathParam("icd10", "X1337").expect().statusCode(200).when().get("api/fmb/{icd10}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fmb-query-response-schema.json")).
                body("icd10Code", equalTo("X1337")).
                body("forms", hasSize(0));
    }

}
