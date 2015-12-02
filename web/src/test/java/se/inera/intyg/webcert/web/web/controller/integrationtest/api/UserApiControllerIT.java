package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import se.inera.intyg.webcert.web.auth.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by marced on 17/11/15.
 */
public class UserApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetAnvandare() {

        // set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().expect().statusCode(200).when().get("api/anvandare").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-schema.json")).
                body("hsaId", equalTo(DEFAULT_LAKARE.getHsaId())).
                body("valdVardenhet.id", equalTo(DEFAULT_LAKARE.getEnhetId())).
                body("namn", equalTo(DEFAULT_LAKARE.getFornamn() + " " + DEFAULT_LAKARE.getEfternamn()));
    }

    @Test
    public void testGetAnvandareNotLoggedIn() {

        // set up auth precondition
        RestAssured.sessionId = null;

        given().expect().statusCode(403).when().get("api/anvandare");
    }

    @Test
    public void testAndraValdEnhet() {

        // Log in as user having medarbetaruppdrag at several vardenheter.
        FakeCredentials user = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-104B", "Åsa", "Multi-vardenheter",
                "IFV1239877878-1042").lakare(true).build();
        RestAssured.sessionId = getAuthSession(user);

        //An improvement of this would be to call hsaStub rest api to add testa data as we want it to aoid "magic" ids and the dependeny to bootstrapped data.
        final String vardEnhetToChangeTo = "IFV1239877878-1045";
        ChangeSelectedUnitRequest changeRequest = new ChangeSelectedUnitRequest();
        changeRequest.setId(vardEnhetToChangeTo);

        given().contentType(ContentType.JSON).and().body(changeRequest).when().post("api/anvandare/andraenhet").
                then().
                statusCode(200).
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-user-schema.json")).
                body("valdVardenhet.id", equalTo(vardEnhetToChangeTo));
    }

    @Test
    public void testGodkannAvtal() {

        // set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().contentType(ContentType.JSON).when().put("api/anvandare/godkannavtal").then().statusCode(200);
    }

    @Test
    public void testAtertaAvtalGodkannande() {

        // set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().contentType(ContentType.JSON).when().delete("api/anvandare/privatlakaravtal").then().statusCode(200);
    }

    @Test
    public void testHamtaSenasteAvtal() {

        // set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().contentType(ContentType.JSON).when().get("api/anvandare/latestavtal").
                then().
                statusCode(200).
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-avtal-schema.json"));
    }

}
