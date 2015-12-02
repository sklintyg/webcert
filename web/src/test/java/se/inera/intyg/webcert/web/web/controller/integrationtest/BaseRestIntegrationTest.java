package se.inera.intyg.webcert.web.web.controller.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.fake.FakeCredentials;

import javax.servlet.http.HttpServletResponse;

/**
 * Base class for "REST-ish" integrationTests using RestAssured.
 * 
 * Created by marced on 19/11/15.
 */
public abstract class BaseRestIntegrationTest {

    private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";
    private static final String FAKE_LOGIN_URI = "/fake";

    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

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
     * @return sessionId for the now authorized user
     */
    public String getAuthSession(FakeCredentials fakeCredentials) {
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
