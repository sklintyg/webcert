package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.auth.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by marced on 17/11/15.
 */
public class FragaSvarApiControllerIT extends BaseRestIntegrationTest {
    protected static FakeCredentials LAKARE_MED_FRAGASVAR = new FakeCredentials.FakeCredentialsBuilder("eva", "Eva", "Holgersson",
            "centrum-vast").lakare(true).build();

    /**
     * Verify that no results are returned for a query that can not match anything
     */
    @Test
    public void testQueryFragaSvarNoResults() {
        RestAssured.sessionId = getAuthSession(LAKARE_MED_FRAGASVAR);

        given().param("hsaId", "finnsEj").expect().statusCode(200).
                when().
                get("api/fragasvar/sok").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-query-response-schema.json")).
                body("totalCount", equalTo(0));
    }

    /**
     * Verify that at least one of our bootstrapped fragasvar results are returned for a query
     * that should match match every bootstrapped fragasvar.
     */
    @Test
    public void testQueryFragaSvarAllResults() {

        RestAssured.sessionId = getAuthSession(LAKARE_MED_FRAGASVAR);

        given().expect().statusCode(200).
                when().
                get("api/fragasvar/sok").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-query-response-schema.json")).
                body("totalCount", greaterThan(0));
    }

}
