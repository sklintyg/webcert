/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.base.Strings;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyFromCandidateRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;

/**
 * Basic test suite that verifies that the endpoint (‘/moduleapi/utkast) is available and respond according to
 * specification.
 *
 * Created by marhes on 18/01/16.
 */
public class UtkastModuleApiControllerIT extends BaseRestIntegrationTest {

    public static final String TESTABILITY_BASE = "/testability/intyg";

    public static final String MODULEAPI_UTKAST_BASE = "moduleapi/utkast";
    public static final String GRPAPI_STUBBE_BASE = "services/grp-api";
    public static final String PUAPI_BASE = "services/api/pu-api";

    public static final String PUAPI_SEKRETESS = PUAPI_BASE + "/person/%s/sekretessmarkerad?value=%s";

    public static final int HTTP_OK = 200;
    public static final int HTTP_FOUND = 302;
    public static final int HTTP_ISE = 500; // Internal Server Error

    @Before
    public void setup() {
        // Försäkrar att patienten inte är sekretessmarkerad
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(String.format(PUAPI_SEKRETESS, DEFAULT_PATIENT_PERSONNUMMER, "false"));
    }

    @Test
    public void testGetDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"));
    }

    @Test
    public void testThatGetDraftBelongingToDifferentCareUnitFails() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .expect().statusCode(HTTP_FOUND)
            .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        spec()
            .expect().statusCode(HTTP_ISE)
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
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");
        Map<String, String> content = model.getJsonObject("content");

        spec()
            .body(content)
            .expect().statusCode(HTTP_OK)
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
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        Map<String, String> content = model.getJsonObject("content");

        spec()
            .body(content).pathParams("intygsTyp", intygsTyp, "intygsId", intygsId)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/{intygsTyp}/{intygsId}/validate")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-validate-draft-response-schema.json"));
    }

    @Test
    public void testDiscardDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        spec()
            .expect().statusCode(HTTP_OK)
            .when().delete(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version);

        spec()
            .expect().statusCode(HTTP_ISE)
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
            .body(UtkastStatus.DRAFT_LOCKED.name()).pathParams("intygsId", intygsId)
            .expect().statusCode(HTTP_OK)
            .when().put(TESTABILITY_BASE + "/{intygsId}/status");

        // Check that draft is locked
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("status", equalTo(UtkastStatus.DRAFT_LOCKED.name()));

        // Revoke locked draft
        RevokeSignedIntygParameter parameter = new RevokeSignedIntygParameter();
        parameter.setReason("A good reason");
        parameter.setMessage("A message for the drawer");

        spec()
            .body(parameter)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/aterkalla");

        // Check if draft is revoked
        Response response = spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        // Get the JsonPath object instance from the Response interface
        JsonPath jsonPathEvaluator = response.jsonPath();

        // Query the JsonPath object to get a String value of the node
        String aterkalladDatum = jsonPathEvaluator.get("aterkalladDatum");

        assertFalse(Strings.isNullOrEmpty(aterkalladDatum));
        assertEquals(LocalDate.now(), LocalDateTime.parse(aterkalladDatum).toLocalDate());
    }

    @Test
    public void testCopyUtkastFromAnotherUtkastWithStatusLocked() {
        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        CreateUtkastRequest request = createUtkastRequest(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        String intygsId = createUtkast(request);

        // Update draft via testability-api
        spec()
            .body(UtkastStatus.DRAFT_LOCKED.name()).pathParams("intygsId", intygsId)
            .expect().statusCode(HTTP_OK)
            .when().put(TESTABILITY_BASE + "/{intygsId}/status");

        // Check that draft is locked
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("status", equalTo(UtkastStatus.DRAFT_LOCKED.name()));

        CopyIntygResponse copyIntygResponse = spec()
            .pathParams("intygsTyp", intygsTyp, "intygsId", intygsId)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/{intygsTyp}/{intygsId}/copy")
            .then().extract().response().as(CopyIntygResponse.class);

        assertEquals(intygsTyp, copyIntygResponse.getIntygsTyp());
        assertEquals(LUSE_BASE_INTYG_TYPE_VERSION, copyIntygResponse.getIntygTypeVersion());

        // Check draft that was created during the copy
        Response createdDraft = spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + copyIntygResponse.getIntygsUtkastId())
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .extract().response();

        // Assert content in created draft
        JsonPath model = new JsonPath(createdDraft.body().asString());

        assertEquals(UtkastStatus.DRAFT_INCOMPLETE.name(), model.getString("status"));
        assertEquals(DEFAULT_LAKARE.getHsaId(), model.getString("content.grundData.skapadAv.personId"));
        assertEquals("Jan Nilsson", model.getString("content.grundData.skapadAv.fullstandigtNamn"));
        assertEquals(request.getPatientPersonnummer().getPersonnummer(), model.getString("content.grundData.patient.personId"));
        assertEquals(getFullName(request), model.getString("content.grundData.patient.fullstandigtNamn"));
    }

    @Test
    public void testCopyUtkastFromAnotherUtkastWithStatusLockedAndInvalidOrigin() {
        // Set up auth precondition
        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "luse";
        CreateUtkastRequest request = createUtkastRequest(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        String intygsId = createUtkast(request);

        // Update draft via testability-api
        spec()
            .body(UtkastStatus.DRAFT_LOCKED.name()).pathParams("intygsId", intygsId)
            .expect().statusCode(HTTP_OK)
            .when().put(TESTABILITY_BASE + "/{intygsId}/status");

        // Check that draft is locked
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("status", equalTo(UtkastStatus.DRAFT_LOCKED.name()));

        changeOriginTo("UTHOPP");

        spec()
            .pathParams("intygsTyp", intygsTyp, "intygsId", intygsId)
            .expect().statusCode(HTTP_ISE)
            .when().post(MODULEAPI_UTKAST_BASE + "/{intygsTyp}/{intygsId}/copy")
            .then()
            .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
            .body("message", containsString("KOPIERA_LAST_UTKAST"));
    }

    @Test
    public void testCopyFromCandidateWhenUserIsLakare() {
        String intygType = "lisjp";
        String utkastType = "ag7804";

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        // Create the candidate
        String intygId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        // Change users origin
        changeOriginTo("DJUPINTEGRATION");

        String utkastId = createUtkast("ag7804", DEFAULT_PATIENT_PERSONNUMMER);

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygId);
        request.setCandidateType(intygType);

        spec()
            .body(request)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + utkastType + "/" + utkastId + "/copyfromcandidate")
            .then()
            .body("status", equalTo(UtkastStatus.DRAFT_INCOMPLETE.name()))
            .body("version",  equalTo(0));
    }

    @Test
    public void testCopyFromCandidateWhenUserIsLakareAndPatientHasSekretessmarkering() {
        String intygType = "lisjp";
        String utkastType = "ag7804";

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        // Create the candidate
        String intygId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        // Change users origin
        changeOriginTo("DJUPINTEGRATION");

        String utkastId = createUtkast("ag7804", DEFAULT_PATIENT_PERSONNUMMER);

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygId);
        request.setCandidateType(intygType);

        // Sätter patient till sekretessmarkerad
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(String.format(PUAPI_SEKRETESS, DEFAULT_PATIENT_PERSONNUMMER, "true"));

        spec()
            .body(request)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + utkastType + "/" + utkastId + "/copyfromcandidate")
            .then()
            .body("status", equalTo(UtkastStatus.DRAFT_INCOMPLETE.name()))
            .body("version",  equalTo(0));
    }

    @Test
    public void testCopyFromCandidateWhenUserIsNotLakare() {
        String intygType = "lisjp";
        String utkastType = "ag7804";

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        // Create the candidate
        String intygId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        // Change users origin and role
        changeOriginTo("DJUPINTEGRATION");
        changeRoleTo(AuthoritiesConstants.ROLE_ADMIN);

        String utkastId = createUtkast("ag7804", DEFAULT_PATIENT_PERSONNUMMER);

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygId);
        request.setCandidateType(intygType);

        spec()
            .body(request)
            .expect().statusCode(HTTP_OK)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + utkastType + "/" + utkastId + "/copyfromcandidate")
            .then()
            .body("status", equalTo(UtkastStatus.DRAFT_INCOMPLETE.name()))
            .body("version",  equalTo(0));
    }

    @Test
    public void testCopyFromCandidateWhenUserIsNotLakareAndPatientHasSekretessmarkering() {
        String intygType = "lisjp";
        String utkastType = "ag7804";

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        // Create the candidate
        String intygId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        // Change users origin and role
        changeOriginTo("DJUPINTEGRATION");
        changeRoleTo(AuthoritiesConstants.ROLE_ADMIN);

        String utkastId = createUtkast("ag7804", DEFAULT_PATIENT_PERSONNUMMER);

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygId);
        request.setCandidateType(intygType);

        // Sätter patient till sekretessmarkerad
        spec()
            .expect().statusCode(HTTP_OK)
            .when().get(String.format(PUAPI_SEKRETESS, DEFAULT_PATIENT_PERSONNUMMER, "true"));

        spec()
            .body(request)
            .expect().statusCode(HTTP_ISE)
            .when().post(MODULEAPI_UTKAST_BASE + "/" + utkastType + "/" + utkastId + "/copyfromcandidate")
            .then()
            .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING.name()))
            .body("message", not(isEmptyString()));
    }

    private String getFullName(CreateUtkastRequest request) {
        return getFullName(
            request.getPatientFornamn(),
            request.getPatientMellannamn(),
            request.getPatientEfternamn());
    }

    private String getFullName(String... strings) {
        return Arrays.stream(strings)
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(" "));
    }

}
