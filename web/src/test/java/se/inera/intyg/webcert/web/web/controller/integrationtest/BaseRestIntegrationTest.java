package se.inera.intyg.webcert.web.web.controller.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import com.jayway.restassured.path.json.JsonPath;
import org.junit.Before;


import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.FakeCredentials;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;

/**
 * Base class for "REST-ish" integrationTests using RestAssured.
 * <p/>
 * Created by marced on 19/11/15.
 */
public abstract class BaseRestIntegrationTest {

    private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
    private static final String FAKE_LOGIN_URI = "/fake";

    protected CustomObjectMapper objectMapper = new CustomObjectMapper();
    protected static FakeCredentials DEFAULT_LAKARE = new FakeCredentials.FakeCredentialsBuilder("IFV1239877878-1049", "rest", "testman",
            "IFV1239877878-1042").lakare(true).build();

    /**
     * Common setup for all tests
     */
    @Before
    public void setup() {
        RestAssured.reset();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl");
    }

    /**
     * Log in to webcert using the supplied credentials.
     *
     * @param fakeCredentials who to log in as
     * @return sessionId for the now authorized user session
     */
    protected String getAuthSession(FakeCredentials fakeCredentials) {
        String credentialsJson;
        try {
            credentialsJson = objectMapper.writeValueAsString(fakeCredentials);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Response response = given().redirects().follow(false).log().all().contentType(ContentType.URLENC)
                .formParam(USER_JSON_FORM_PARAMETER, credentialsJson).expect()
                .statusCode(HttpServletResponse.SC_FOUND).when()
                .post(FAKE_LOGIN_URI).then().log().all().extract().response();

        assertNotNull(response.sessionId());
        return response.sessionId();
    }
}
