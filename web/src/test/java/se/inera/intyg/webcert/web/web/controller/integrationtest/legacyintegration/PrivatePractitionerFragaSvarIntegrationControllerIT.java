package se.inera.intyg.webcert.web.web.controller.integrationtest.legacyintegration;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import javax.servlet.http.HttpServletResponse;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

/**
 * Check that private-practitioner-links are redirected correctly.
 */
public class PrivatePractitionerFragaSvarIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_INTYGSID = "abcd123-abcd123-abcd123";

    @Test
    public void testRedirectSuccess() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        changeRoleTo("ROLE_PRIVATLAKARE");

        given().redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
                expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
                when().get("webcert/web/user/pp-certificate/{intygsId}/questions").
                then().
                header(HttpHeaders.LOCATION, endsWith("/fragasvar/fk7263/" + DEFAULT_INTYGSID));
    }

    @Test
    public void testRedirectFailsWithInvalidRole() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
                expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                when().get("webcert/web/user/pp-certificate/{intygsId}/questions");
    }
}
