/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus.DRAFT_INCOMPLETE;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi.UtkastModuleApiControllerIT.MODULEAPI_UTKAST_BASE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;

/**
 * Integration test for {@link se.inera.intyg.webcert.web.web.controller.moduleapi.IntygModuleApiController}.
 *
 * Due to the nature of these integration tests - i.e. running without Intygstjänsten, some corners are cut using
 * testability APIs in order to set up test data properly.
 */
public class IntygModuleApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetFk7263Intyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsBasIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-bas";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsDiabetesIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-diabetes";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetUnknownFk7263() {
        testGetUnknownIntyg("fk7263");
    }

    @Test
    public void testGetUnknownTsBas() {
        testGetUnknownIntyg("ts-bas");
    }

    @Test
    public void testGetUnknownTsDiabetes() {
        testGetUnknownIntyg("ts-diabetes");
    }

    @Test
    public void testSendSignedIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        SendSignedIntygParameter sendParam = new SendSignedIntygParameter();
        sendParam.setRecipient("FKASSA");
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).body(sendParam)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/skicka")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

        deleteUtkast(intygsId);
    }

    @Test
    public void testRevokeSignedIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setMessage("Makulera!");
        revokeParam.setReason("FELAKTIGT_INTYG");
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).body(revokeParam)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkalla")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

        deleteUtkast(intygsId);
    }

    @Test
    public void testRevokeSignedIntygWithoutPrivilegeFails() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        // Change role to admin - which does not have sign privilege..
        changeRoleTo(AuthoritiesConstants.ROLE_ADMIN);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setMessage("Makulera!");
        revokeParam.setReason("FELAKTIGT_INTYG");
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).body(revokeParam)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkalla")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetIntygFromDifferentCareUnitWithCoherentJournalingFlagSuccess() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .redirects().follow(false).and().pathParam("intygsId", intygsId)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().get("moduleapi/intyg/fk7263/{intygsId}")
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));
    }

    @Test
    public void testGetIntygFromDifferentCareUnitWithoutCoherentJournalingFlagFail() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .redirects().follow(false).and().pathParam("intygsId", intygsId)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().get("moduleapi/intyg/fk7263/{intygsId}")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkast() {
        final String personnummer = "19121212-1212";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", personnummer);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(utkastId)))
                .body("intygsTyp", equalTo("fk7263"));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastFailsForMissingPriveliges() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Lakare role has the KOPIERA_INTYG priviledge, but the privilege is restricted to origintype=NORMAL /
        // DJUPINTEGRERAD.
        // We change the current users origin to be uthop which should trigger an auth exception response.
        changeOriginTo(WebCertUserOriginType.UTHOPP.name());

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));

    }

    /**
     * Check that trying to copy utkast with bad input gives error response.
     */
    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastWithInvalidPatientpersonNummer() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(null);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM.name()))
                .body("message", not(isEmptyString()));

    }

    @Test
    public void testCreateNewUtkastCopyBasedOnIntygFromDifferentCareUnitWithCoherentJournalingSuccess() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        // Set coherentJournaling=true in copyIntygRequest, this is normally done in the js using the copyService.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        String newIntygsId = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo("fk7263"))
                .extract()
                .path("intygsUtkastId");

        // Check that the copy contains the correct stuff
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/fk7263/" + newIntygsId)
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(LEONIE_KOEHL.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnIntygFromDifferentCareUnitWithCoherentJournalingFail() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Then logout
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // coherentJournaling defaults to false, so don't set it here.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testReplaceIntyg() {
        final String personnummer = "19121212-1212";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("fk7263", personnummer);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        final Response response = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/ersatt")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo("fk7263")).extract().response();

        JsonPath intygJson = new JsonPath(response.body().asString());

        String utkastId = intygJson.getString("intygsUtkastId");

        // Verify that the new draft has correct relations
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/fk7263/" + utkastId).then()
                .body("relations[0].intygsId", equalTo(utkastId))
                .body("relations[0].kod", equalTo(RelationKod.ERSATT.name()))
                .body("relations[0].status", equalTo(DRAFT_INCOMPLETE.name()))

                .body("relations[1].intygsId", equalTo(intygsId))
                .body("relations[1].status", equalTo(CertificateState.RECEIVED.name()));

    }

    private void signeraUtkastWithTestabilityApi(String intygsId) {
        String completePath = "testability/intyg/" + intygsId + "/komplett";
        String signPath = "testability/intyg/" + intygsId + "/signerat";
        given().contentType(ContentType.JSON).expect().statusCode(200).when().put(completePath);
        given().contentType(ContentType.JSON).expect().statusCode(200).when().put(signPath);
    }

    private void testGetUnknownIntyg(String intygsTyp) {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = "unknown-1";
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(500)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId);
    }

    private void deleteUtkast(String id) {
        given().contentType(ContentType.JSON).expect().statusCode(200).when().delete("testability/intyg/" + id);
    }

}
