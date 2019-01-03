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

import com.google.common.base.Strings;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic test suite that verifies that the endpoint (/moduleapi/utkast) is available and respond according to
 * specification.
 *
 * Created by marhes on 18/01/16.
 */
public class UtkastModuleApiControllerIT extends BaseRestIntegrationTest {

    public static final String MODULEAPI_UTKAST_BASE = "moduleapi/utkast";
    public static final String GRPAPI_STUBBE_BASE = "services/grp-api";
    public static final String TESTABILITY_BASE = "/testability/intyg";

    @Test
    public void testGetDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        spec()
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
        spec()
            .redirects().follow(false)
            .expect().statusCode(302)
            .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        spec()
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

        spec()
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

        Response responseIntyg = spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");
        Map<String, String> content = model.getJsonObject("content");

        spec()
            .body(content)
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

        Response responseIntyg = spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        Map<String, String> content = model.getJsonObject("content");

        spec()
            .body(content).pathParams("intygsTyp", intygsTyp, "intygsId", intygsId)
            .expect().statusCode(200)
            .when().post(MODULEAPI_UTKAST_BASE + "/{intygsTyp}/{intygsId}/validate")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-validate-draft-response-schema.json"));
    }

    @Test
    public void testDiscardDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        spec()
            .expect().statusCode(200)
            .when().delete(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version);

        spec()
            .expect().statusCode(500)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json"))
            .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND.name()))
            .body("message", not(isEmptyString()));
    }

    @Test
    public void testRevokeLockedDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        // Update draft via testability-api
        spec()
            .body(UtkastStatus.DRAFT_LOCKED.name()).pathParams( "intygsId", intygsId)
            .expect().statusCode(200)
            .when().put(TESTABILITY_BASE + "/{intygsId}/status");

        // Check that draft is locked
        spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("status", equalTo(UtkastStatus.DRAFT_LOCKED.name()));

        // Revoke locked draft
        RevokeSignedIntygParameter parameter = new RevokeSignedIntygParameter();
        parameter.setReason("A good reason");
        parameter.setMessage("A message for the drawer");

        spec()
            .body(parameter)
            .expect().statusCode(200)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/aterkalla");

        // Check if draft is revoked
        Response response = spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        // Get the JsonPath object instance from the Response interface
        JsonPath jsonPathEvaluator = response.jsonPath();

        // Query the JsonPath object to get a String value of the node
        String aterkalladDatum = jsonPathEvaluator.get("aterkalladDatum");

        assertFalse(Strings.isNullOrEmpty(aterkalladDatum));
        assertEquals(LocalDate.now(), LocalDateTime.parse(aterkalladDatum).toLocalDate());
    }

}
