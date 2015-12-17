package se.inera.intyg.webcert.web.web.controller.integrationtest.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marced on 16/12/15.
 */
public class IntygIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_INTYGSID = "abcd123-abcd123-abcd123";

    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link and gets redirected to the correct url.
     */
    @Test
    public void testRedirectSuccessUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsId", utkastId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER);
        queryParams.put("responsibleHospName", "HrDoktor");


        given().redirects().follow(false).pathParam("intygsId", utkastId).queryParams(queryParams).
                expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
                when().get("/visa/intyg/{intygsId}").
                then().header(HttpHeaders.LOCATION, endsWith("/fk7263/edit/" + utkastId + "?patientId=" + queryParams.get("alternatePatientSSn") + "&hospName=" + queryParams.get("responsibleHospName")));
    }

    /**
     * Verify that a djupintegrerad lakare can use a intyg redirect link and gets redirected to the correct url (that is different from an utkast link).
     */
    @Test
    public void testRedirectSuccessSigneratIntyg() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        given().redirects().follow(false).and().pathParam("intygsId", intygsId).and().queryParam("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER).
                expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
                when().get("/visa/intyg/{intygsId}").
                then().header(HttpHeaders.LOCATION, endsWith("/intyg/fk7263/" + intygsId + "?patientId=" + DEFAULT_PATIENT_PERSONNUMMER));
    }

    /**
     * Verify that a lakare can't use a utkast redirect link.
     */
    @Test
    public void testRedirectFailsWithInvalidRole() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
                expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                when().get("visa/intyg/{intygsId}?alternatePatientSSn=x&responsibleHospName=x");
    }
}

