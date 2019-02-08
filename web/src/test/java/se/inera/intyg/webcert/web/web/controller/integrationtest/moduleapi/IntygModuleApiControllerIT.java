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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi.UtkastModuleApiControllerIT.MODULEAPI_UTKAST_BASE;

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

        String intygsTyp = "lisjp";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER)));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsBasIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-bas";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER)));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsDiabetesIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-diabetes";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER)));

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

        String intygsTyp = "lisjp";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        SendSignedIntygParameter sendParam = new SendSignedIntygParameter();
        sendParam.setRecipient("FKASSA");
        spec()
                .body(sendParam)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/skicka")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

        deleteUtkast(intygsId);
    }

    @Test
    public void testRevokeSignedIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "lisjp";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setMessage("Makulera!");
        revokeParam.setReason("FELAKTIGT_INTYG");
        spec()
                .body(revokeParam)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkalla")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

        deleteUtkast(intygsId);
    }

    @Test
    public void testRevokeSignedIntygWithoutPrivilegeFails() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "lisjp";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        // Change role to admin - which does not have sign privilege..
        changeRoleTo(AuthoritiesConstants.ROLE_ADMIN);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setMessage("Makulera!");
        revokeParam.setReason("FELAKTIGT_INTYG");
        spec()
                .body(revokeParam)
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
        String intygsId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        spec()
                .redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        spec()
                .redirects().follow(false).and().pathParam("intygsId", intygsId)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().get("moduleapi/intyg/lisjp/{intygsId}")
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER)));
    }

    @Test
    public void testGetIntygFromDifferentCareUnitWithoutCoherentJournalingFlagFail() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg("lisjp", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        spec()
                .redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");

        spec()
                .redirects().follow(false).and().pathParam("intygsId", intygsId)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().get("moduleapi/intyg/lisjp/{intygsId}")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testCreateRenewalBasedOnExistingUtkast() {
        final String personnummer = "19121212-1212";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        changeOriginTo("DJUPINTEGRATION");

        String utkastId = createUtkast("lisjp", personnummer);
        signUtkast(utkastId);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", utkastId);

        spec()
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/fornya")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(utkastId)))
                .body("intygsTyp", equalTo("lisjp"));
    }

    @Test
    public void testCreateRenewalBasedOnExistingUtkastFailsForMissingPriveliges() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        // Lakare role has the FORNYA_INTYG priviledge, but the privilege is restricted to origintype=NORMAL /
        // DJUPINTEGRERAD.
        // We change the current users origin to be uthop which should trigger an auth exception response.
        changeOriginTo(UserOriginType.UTHOPP.name());

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", utkastId);

        spec()
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/fornya")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));

    }

    /**
     * Check that trying to copy utkast with bad input gives error response.
     */
    @Test
    public void testCreateRenewalBasedOnExistingUtkastWithInvalidPatientpersonNummer() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        changeOriginTo("DJUPINTEGRATION");

        String utkastId = createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(null);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", utkastId);

        spec()
                .contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/fornya")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM.name()))
                .body("message", not(isEmptyString()));

    }

    @Test
    public void testCreateRenewalBasedOnIntygFromDifferentCareUnitWithCoherentJournalingSuccess() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER);
        signUtkast(intygsId);

        // Then logout
        spec()
                .redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");
        setSjf();

        // Set coherentJournaling=true in copyIntygRequest, this is normally done in the js using the copyService.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", intygsId);

        String newIntygsId = spec()
                .and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/fornya")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo("lisjp"))
                .extract()
                .path("intygsUtkastId");

        // Check that the copy contains the correct stuff
        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/lisjp/" + newIntygsId)
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(LEONIE_KOEHL.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER)));
    }

    @Test
    public void testCreateRenewalBasedOnIntygFromDifferentCareUnitWithCoherentJournalingFail() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER);
        signUtkast(intygsId);

        // Then logout
        spec()
                .redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // coherentJournaling defaults to false, so don't set it here.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", intygsId);

        spec()
                .and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/fornya")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testReplaceIntyg() {
        final String personnummer = "19121212-1212";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        changeOriginTo("DJUPINTEGRATION");

        String intygsId = createSignedIntyg("lisjp", personnummer);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "lisjp");
        pathParams.put("intygsId", intygsId);

        final Response response = spec()
                .and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/ersatt")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo("lisjp")).extract().response();

        JsonPath intygJson = new JsonPath(response.body().asString());

        String utkastId = intygJson.getString("intygsUtkastId");

        // Verify that the new draft has correct relations
        spec()
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/lisjp/" + utkastId).then()
                .body("relations.parent.intygsId", equalTo(intygsId))
                .body("relations.parent.relationKod", equalTo(RelationKod.ERSATT.name()));

        // Verify the original certficate has a child relationship
        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/lisjp/" + intygsId).then()
                .body("relations.latestChildRelations.replacedByUtkast.intygsId", equalTo(utkastId))
                .body("relations.latestChildRelations.replacedByUtkast.relationKod", equalTo(RelationKod.ERSATT.name()));


    }

    @Test
    public void testCreateUtkastFromTemplate() throws Exception {
        final String personnummer = "19121212-1212";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String dbIntyg = createDbIntyg(personnummer);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "db");
        pathParams.put("intygsId", dbIntyg);
        pathParams.put("newIntygsTyp", "doi");

        spec()
                .and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/{newIntygsTyp}/create")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(dbIntyg)))
                .body("intygsTyp", equalTo("doi"));
    }

    @Test
    public void testCompletionContainsCommentStringInOvrigt() throws Exception {
        final String personnummer = "19121212-1212";
        final String kommentar = "Testkommentar";

        Personnummer pers = Personnummer.createPersonnummer(personnummer).get();

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "lisjp";
        String intygsId = createSignedIntyg(intygsTyp, personnummer);

        createArendeQuestion(intygsTyp, intygsId, personnummer, ArendeAmne.KOMPLT);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", intygsTyp);
        pathParams.put("intygsId", intygsId);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(pers);
        copyIntygRequest.setKommentar(kommentar);

        final Response response = spec()
                .and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(200)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/komplettera")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo(intygsTyp)).extract().response();

        JsonPath intygJson = new JsonPath(response.body().asString());
        String newUtkastId = intygJson.getString("intygsUtkastId");

        spec()
                .expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + newUtkastId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.ovrigt", containsString(kommentar));
    }

    private String createDbIntyg(String personnummer) throws IOException {
        String intygsTyp = "db";

        String intygsId = createUtkast(intygsTyp, personnummer);

        Response responseIntyg = spec()
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) mapper.readTree(responseIntyg.body().asString());

        String version = rootNode.get("version").asText();

        ObjectNode content = (ObjectNode) rootNode.get("content");

        content.put("identitetStyrkt", "körkort");
        content.put("dodsdatumSakert", "false");
        content.put("dodsdatum", "2027-01-01");
        content.put("antraffatDodDatum", "2027-01-02");
        content.put("dodsplatsKommun", "kommun");
        content.put("dodsplatsBoende", "SJUKHUS");
        content.put("barn", "true");
        content.put("explosivImplantat", "true");
        content.put("explosivAvlagsnat", "true");
        content.put("undersokningYttre", "UNDERSOKNING_GJORT_KORT_FORE_DODEN");
        content.put("undersokningDatum", "2026-12-31");
        content.put("polisanmalan", "true");
        content.put("avstangningSmittskydd", true);
        content.put("tjanstgoringstid", "40");
        content.put("ressattTillArbeteEjAktuellt", true);
        content.putObject("nedsattMed100");
        ObjectNode node = (ObjectNode) content.get("nedsattMed100");
        node.put("from", "2026-01-19");
        node.put("tom", "2026-01-25");

        spec()
                .body(content)
                .expect().statusCode(200)
                .when().put(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json"))
                .body("version", equalTo(Integer.parseInt(version) + 1)).extract().response();

        return intygsId;
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
        spec()
                .expect().statusCode(500)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId);
    }

    private void deleteUtkast(String id) {
        given().contentType(ContentType.JSON).expect().statusCode(200).when().delete("testability/intyg/" + id);
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
