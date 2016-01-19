package se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Integration test for {@link se.inera.intyg.webcert.web.web.controller.moduleapi.IntygModuleApiController}.
 *
 * Due to the nature of these integration tests - i.e. running without Intygstjänsten, some corners are cut using
 * testability APIs in order to set up test data properly.
 */
public class IntygModuleApiControllerIT extends BaseRestIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(IntygModuleApiControllerIT.class);

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
        signeraUtkastWithTestbarhetsApi(intygsId);

        SendSignedIntygParameter sendParam = new SendSignedIntygParameter();
        sendParam.setRecipient("FK");
        sendParam.setPatientConsent(true);
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
        signeraUtkastWithTestbarhetsApi(intygsId);

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
        signeraUtkastWithTestbarhetsApi(intygsId);

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

    private void signeraUtkastWithTestbarhetsApi(String intygsId) {
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
