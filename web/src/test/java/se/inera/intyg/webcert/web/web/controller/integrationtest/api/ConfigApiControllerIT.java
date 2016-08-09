package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.junit.Test;

import com.jayway.restassured.RestAssured;

import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class ConfigApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetConfig() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        given().expect().statusCode(200).when().get("api/config").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-config-response-schema.json"));
    }

}
