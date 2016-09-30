package se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.*;

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

        given().expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId).then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsBasIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-bas";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId).then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));

        deleteUtkast(intygsId);
    }

    @Test
    public void testGetTsDiabetesIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "ts-diabetes";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().expect().statusCode(200)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId).then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
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
        sendParam.setRecipient("FK");
        given().contentType(ContentType.JSON).body(sendParam).expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/skicka").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

        deleteUtkast(intygsId);
    }

    @Test
    public void testRevokeSignedIntyg() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);
        signeraUtkastWithTestabilityApi(intygsId);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setRevokeMessage("Makulera!");
        given().contentType(ContentType.JSON).body(revokeParam).expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkalla").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-send-signed-intyg-response-schema.json"));

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
        revokeParam.setRevokeMessage("Makulera!");
        given().contentType(ContentType.JSON).body(revokeParam)
                .expect().statusCode(500)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkalla")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));

        deleteUtkast(intygsId);
    }

    /**
     * Verify that coherent journaling works, i.e that requests with the parameter sjf=true can access certificates
     * created on different care units than the currently active / selected one.
     */
    @Test
    public void testGetIntygFromDifferentCareUnitWithCoherentJournalingFlagSuccess() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");

        given().redirects().follow(false).and().pathParam("intygsId", intygsId)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().get("moduleapi/intyg/fk7263/{intygsId}?sjf=true")
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));
    }

    /**
     * Requests without the parameter sjf=true should get an INTERNAL_SERVER_ERROR with error message
     * AUTHORIZATION_PROBLEM when accessing certificates on different care units than the currently active / selected
     * one.
     */
    @Test
    public void testGetIntygFromDifferentCareUnitWithoutCoherentJournalingFlagFail() {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);
        // Then logout
        given().redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);

        changeOriginTo("DJUPINTEGRATION");

        given().redirects().follow(false).and().pathParam("intygsId", intygsId)
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
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(personnummer));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(200).when()
                .post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
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
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(500).when()
                .post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera").then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name())).body("message", not(isEmptyString()));

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
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(500).when()
                .post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera").then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM.name())).body("message", not(isEmptyString()));

    }

    @Test
    public void testCreateNewUtkastCopyBasedOnIntygFromDifferentCareUnitWithCoherentJournalingSuccess() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Then logout
        given().redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // Set coherentJournaling=true in copyIntygRequest, this is normally done in the js using the copyService.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setCoherentJournaling(true);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        String newIntygsId = given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_OK)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo("fk7263"))
                .extract()
                .path("intygsUtkastId");

        // Check that the copy contains the correct stuff
        given().expect().statusCode(200)
                .when().get("moduleapi/intyg/fk7263/" + newIntygsId + "?sjf=true")
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
        given().redirects().follow(false)
                .expect().statusCode(HttpServletResponse.SC_FOUND)
                .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in
        // previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // coherentJournaling defaults to false, so don't set it here.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().post("moduleapi/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testRevokeReplaceSignedIntyg() {
        final String personnummer = "19121212-1212";
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, personnummer);
        signeraUtkastWithTestabilityApi(intygsId);

        RevokeSignedIntygParameter revokeParam = new RevokeSignedIntygParameter();
        revokeParam.setRevokeMessage("Makulera!");

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));

        RevokeReplaceSignedIntygRequest request = new RevokeReplaceSignedIntygRequest();
        request.setRevokeSignedIntygParameter(revokeParam);
        request.setCopyIntygRequest(copyIntygRequest);

        given().contentType(ContentType.JSON).body(request).expect().statusCode(200)
                .when().post("moduleapi/intyg/" + intygsTyp + "/" + intygsId + "/aterkallaersatt").then()
                .body("intygsUtkastId", not(isEmptyString()))
                .body("intygsUtkastId", not(equalTo(intygsId)))
                .body("intygsTyp", equalTo(intygsTyp));

        deleteUtkast(intygsId);
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
        given().expect().statusCode(500)
                .when().get("moduleapi/intyg/" + intygsTyp + "/" + intygsId);
    }

    private void deleteUtkast(String id) {
        given().contentType(ContentType.JSON).expect().statusCode(200).when().delete("testability/intyg/" + id);
    }

}
