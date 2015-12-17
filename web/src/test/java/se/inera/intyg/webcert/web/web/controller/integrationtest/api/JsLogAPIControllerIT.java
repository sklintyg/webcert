package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by marced on 01/12/15.
 */
public class JsLogAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testPostDebugLog() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().contentType(ContentType.JSON).and().body("rest-api-integrationtest-message").
                expect().statusCode(200).when().post("api/jslog/debug");
    }


}
