package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.auth.FakeCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DiagnosParameter;

public class DiagnosModuleApiControllerIT extends BaseRestIntegrationTest {

    protected static FakeCredentials LAKARE = new FakeCredentials.FakeCredentialsBuilder("eva", "Eva", "Holgersson",
            "centrum-vast").lakare(true).build();

    @Test
    public void testSearchDiagnosisByCode() {
        RestAssured.sessionId = getAuthSession(LAKARE);

        DiagnosParameter body = new DiagnosParameter();
        body.setCodeFragment("A01");
        body.setCodeSystem(Diagnoskodverk.ICD_10_SE.name());
        body.setNbrOfResults(4);

        given().
            contentType(ContentType.JSON).
            and().
            body(body).
        expect().
            statusCode(200).
        when().
            post("moduleapi/diagnos/kod/sok").
        then().
            body(matchesJsonSchemaInClasspath("jsonschema/webcert-diagnos-sok-schema.json")).
            body("diagnoser", hasSize(4)).
            body("moreResults", equalTo(true)).
            body("resultat", equalTo("OK"));
    }
}
