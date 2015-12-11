package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Verify that the API framework handles Rest framework problems, such as an nonexisting endpoint method in such a
 * way that our custom exception handler is used and responds with our custom error json response.
 *
 * @see se.inera.intyg.webcert.web.web.handlers.WebcertRestExceptionHandler
 * <p/>
 * Created by marced on 01/12/15.
 */
public class RestExceptionHandlerIT extends BaseRestIntegrationTest {


    @Test
    public void testGetNonExistingEndpointMethod() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().expect().statusCode(500).when().get("api/anvandare/non-existing-endpoint}").
                then().
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM.name())).
                body("message", not(isEmptyString()));
    }

}
