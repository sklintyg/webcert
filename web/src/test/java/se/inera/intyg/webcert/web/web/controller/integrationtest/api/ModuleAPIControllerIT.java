package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * Simple test to verify that the modules-map api andpoint responds according to jsonSchema
 *
 * Created by marced on 01/12/15.
 */
public class ModuleAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetModulesMap() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().expect().statusCode(200).when().get("api/modules/map").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-module-map-response-schema.json")).
                body("", hasSize(3)).
                body("id", hasItems("fk7263", "ts-bas", "ts-diabetes"));
    }
}
