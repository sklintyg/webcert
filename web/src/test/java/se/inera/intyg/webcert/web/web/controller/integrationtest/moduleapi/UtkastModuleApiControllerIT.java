/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import se.funktionstjanster.grp.v1.ProgressStatusType;
import se.inera.intyg.common.fk7263.model.internal.PrognosBedomning;
import se.inera.intyg.infra.integration.grp.stub.GrpServicePortTypeStub;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import java.io.IOException;
import java.util.Map;

/**
 * Basic test suite that verifies that the endpoint (/moduleapi/utkast) is available and repond according to
 * specification.
 *
 * Created by marhes on 18/01/16.
 */
public class UtkastModuleApiControllerIT extends BaseRestIntegrationTest {

    public static final String MODULEAPI_UTKAST_BASE = "moduleapi/utkast";
    public static final String GRPAPI_STUBBE_BASE = "services/grp-api";

    @Test
    public void testGetDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
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

        String intygsTyp = "fk7263";
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
        String intygsTyp = "fk7263";
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

        String intygsTyp = "fk7263";
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

        String intygsTyp = "fk7263";
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

        String intygsTyp = "fk7263";
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

    @Test
    public void testSigneraUtkastInvalidState() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON)
                .expect().statusCode(500)
                .when().post(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version + "/signeringshash")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json"))
                .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INVALID_STATE.name()))
                .body("message", not(isEmptyString()));
    }

    @Test
    public void testSigneraUtkast() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON)
                .expect().statusCode(200)
                .when().post(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                        + "/signeringshash")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();
    }

    @Test
    public void testServerSigneraUtkast() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON)
                .expect().statusCode(200)
                .when().post(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                        + "/signeraserver")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();
    }

    @Test
    public void testServerSigneraUtkastMedGrp() throws IOException {

        FakeElegCredentials fakeElegCredentials = new FakeElegCredentials();
        fakeElegCredentials.setPersonId(GrpServicePortTypeStub.PERSON_ID);
        fakeElegCredentials.setPrivatLakare(true);

        RestAssured.sessionId = getAuthSession(fakeElegCredentials);

        Intyg intyg = createIntyg();

        // var knownSignStatuses = {'BEARBETAR':'', 'VANTA_SIGN':'', 'NO_CLIENT':'', 'SIGNERAD':'', 'OKAND': ''};

        // Påbörja signering
        Response responseTicket = given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.JSON)
            .expect().statusCode(200)
            .when()
            .post(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                + "/grp/signeraserver")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("BEARBETAR"))
            .extract().response();

        // Hämta ut biljett-id från svaret
        JsonPath model = new JsonPath(responseTicket.body().asString());
        String biljettId = model.getString("id");

        // biljettId är inte samma sak som orderRef.
        // Hämta ut orderRef först
        Response responseOrderRef = given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.TEXT)
            .expect().statusCode(200)
            .when()
            .get(GRPAPI_STUBBE_BASE + "/orderref/" + biljettId)
            .then()
            .extract().response();

        String orderRef = responseOrderRef.body().asString();

        // Ändra GRP-status till USER_SIGN och kontrollera
        given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.JSON)
            .body(createGrpSignatureStatus(orderRef, ProgressStatusType.USER_SIGN))
            .expect().statusCode(200)
            .when()
            .put(GRPAPI_STUBBE_BASE + "/status");

        // Simulera väntetid vid pollning mot riktig GPR-tjänst
        sleep();

        given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.JSON)
            .expect().statusCode(200)
            .when()
            .get(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("VANTA_SIGN"));

        // Ändra GRP-status till COMPLETE och kontrollera
        given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.JSON)
            .body(createGrpSignatureStatus(orderRef, ProgressStatusType.COMPLETE))
            .expect().statusCode(200)
            .when()
            .put(GRPAPI_STUBBE_BASE + "/status");

        // Simulera väntetid vid pollning mot riktig GPR-tjänst
        sleep();

        given()
            .cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .contentType(ContentType.JSON)
            .expect().statusCode(200)
            .when()
            .get(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("SIGNERAD"));

    }

    @Test
    public void testBiljettStatus() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        Response responseTicket = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON)
                .expect().statusCode(200)
                .when().post(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                        + "/signeringshash")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseTicket.body().asString());
        String biljettId = model.getString("id");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"));
    }

    private Intyg createIntyg() throws IOException {
        String intygsTyp = "fk7263";

        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) mapper.readTree(responseIntyg.body().asString());

        String version = rootNode.get("version").asText();

        ObjectNode content = (ObjectNode) rootNode.get("content");
        content.put("avstangningSmittskydd", true);
        content.put("tjanstgoringstid", "40");
        content.put("ressattTillArbeteEjAktuellt", true);
        content.put("prognosBedomning", PrognosBedomning.arbetsformagaPrognosJa.toString());
        content.putObject("nedsattMed100");
        ObjectNode node = (ObjectNode) content.get("nedsattMed100");
        node.put("from", "2016-01-19");
        node.put("tom", "2016-01-25");

        responseIntyg = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).body(content)
                .expect().statusCode(200)
                .when().put(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json"))
                .body("version", equalTo(Integer.parseInt(version) + 1)).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());

        version = model.getString("version");

        return new Intyg(version, intygsId, intygsTyp);
    }

    private String createGrpSignatureStatus(String biljettId, ProgressStatusType status) {
        return "{ \"orderRef\": \"" + biljettId + "\", \"status\": \"" + status.value() + "\" }";
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    private class Intyg {
        private String id;
        private String intygsTyp;
        private String version;

        public Intyg(String version, String id, String intygsTyp) {
            this.version = version;
            this.id = id;
            this.intygsTyp = intygsTyp;
        }

        public String getId() {
            return id;
        }

        public String getIntygsTyp() {
            return intygsTyp;
        }

        public String getVersion() {
            return version;
        }
    }
}
