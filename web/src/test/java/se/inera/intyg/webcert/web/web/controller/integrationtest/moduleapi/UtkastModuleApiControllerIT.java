/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Basic test suite that verifies that the endpoint (/moduleapi/utkast) is available and repond according to
 * specification.
 *
 * Created by marhes on 18/01/16.
 */
public class UtkastModuleApiControllerIT extends BaseRestIntegrationTest {

    public static final String MODULEAPI_UTKAST_BASE = "moduleapi/utkast";
    public static final String GRPAPI_STUBBE_BASE = "services/grp-api";

    private static final String TOLVAN_PERSON_ID = "19121212-1212";

    @Test
    public void testGetDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"));
    }

    @Test
    public void testThatGetDraftIgnoresCoherentJournaling() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(302)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(500)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testThatGetDraftBelongingToDifferentCareUnitFails() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(302)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(500)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testSaveDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");
        Map<String, String> content = model.getJsonObject("content");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).body(content)
                .expect().statusCode(200)
                .when().put(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json"))
                .body("version", equalTo(Integer.parseInt(version) + 1));
    }

    @Test
    public void testValidateDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
                .extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        Map<String, String> content = model.getJsonObject("content");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).body(content).pathParams("intygsTyp", intygsTyp, "intygsId", intygsId)
                .expect().statusCode(200)
                .when().post(MODULEAPI_UTKAST_BASE + "/{intygsTyp}/{intygsId}/validate")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-validate-draft-response-schema.json"));
    }

    @Test
    public void testDiscardDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON)
                .expect().statusCode(200)
                .when().delete(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON)
                .expect().statusCode(500)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json"))
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND.name()))
                .body("message", not(isEmptyString()));
    }
}
