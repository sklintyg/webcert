package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import com.jayway.restassured.RestAssured;

/**
 * Created by marced on 17/11/15.
 */
public class UserApiControllerIT extends BaseRestIntegrationTest {

    private static FakeCredentials defaultLakare = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-1049", "rest", "testman",
            "IFV1239877878-1042").lakare(true).build();

    @Test
    public void testGetAnvandare() {

        // set up auth precondition
        RestAssured.sessionId = getAuthSession(defaultLakare);

        given().expect().statusCode(200).when().get("api/anvandare").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-schema.json")).
                body("hsaId", equalTo(defaultLakare.getHsaId())).
                body("valdVardenhet.id", equalTo(defaultLakare.getEnhetId())).
                body("namn", equalTo(defaultLakare.getFornamn() + " " + defaultLakare.getEfternamn()));

        /*
         * One could also use the more verbose old-school unit-test assertionstyle...
         * 
         * JsonPath jsonPath = new JsonPath(response.body().asString());
         * 
         * assertEquals(defaultLakare.getHsaId(), jsonPath.getString("hsaId"));
         * assertEquals(defaultLakare.getEnhetId(), jsonPath.getString("valdVardenhet.id"));
         * assertEquals(defaultLakare.getFornamn() + " " + defaultLakare.getEfternamn(), jsonPath.getString("namn"));
         * //We COULD do more asserts, but a bit problematic since most user properties are actually set by the hsastub
         * //and asserting such values bind this test to stub testdata configuration..
         * 
         * assertTrue(matchesJsonSchemaInClasspath("jsonschema/webcert-user-schema.json").matches(response.body().asString
         * ()));
         * // Got problems when deserialising the userRoles enum, otherwise this is a nicer way when we have a
         * // dto backing the response:
         * // WebCertUser webCertUser =
         * given().expect().statusCode(200).when().get("api/anvandare").as(WebCertUser.class);
         */
    }

    @Test
    public void testGetAnvandareNotLoggedIn() {

        // set up auth precondition
        RestAssured.sessionId = null;

        given().expect().statusCode(403).when().get("api/anvandare");
    }
}
