/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import se.inera.intyg.intygstyper.fk7263.model.internal.PrognosBedomning;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import java.io.IOException;
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

    private static final String BASEAPI = "moduleapi/utkast";

    @Test
    public void testGetDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "sjukpenning";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().expect().statusCode(200).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"));
    }

    @Test
    public void testSaveDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "fk7263";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().expect().statusCode(200).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");
        Map<String, String> content = model.getJsonObject("content");

        given().contentType(ContentType.JSON).body(content).expect().statusCode(200).when().
                put(BASEAPI + "/" + intygsTyp + "/" + intygsId + "/" + version).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json")).
                body("version", equalTo(Integer.parseInt(version) + 1));
    }

    @Test
    public void testDiscardDraft() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "sjukpenning";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().expect().statusCode(200).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        given().contentType(ContentType.JSON).expect().statusCode(200).when().
                delete(BASEAPI + "/" + intygsTyp + "/" + intygsId + "/" + version);

        given().contentType(ContentType.JSON).expect().statusCode(500).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json")).
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND.name())).
                body("message", not(isEmptyString()));
    }

    @Test
    public void testLogPrintOfDraftToPDL() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "sjukpenning";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        given().contentType(ContentType.JSON).expect().statusCode(200).when().
                post(BASEAPI + "/" + intygsTyp + "/" + intygsId + "/loggautskrift");
    }

    @Test
    public void testSigneraUtkastInvalidState() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsTyp = "sjukpenning";
        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().expect().statusCode(200).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());
        String version = model.getString("version");

        given().contentType(ContentType.JSON).expect().statusCode(500).when().
                post(BASEAPI + "/" + intygsTyp + "/" + intygsId + "/" + version + "/signeringshash").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json")).
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INVALID_STATE.name())).
                body("message", not(isEmptyString()));
    }

    @Test
    public void testSigneraUtkast() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        given().contentType(ContentType.JSON).expect().statusCode(200).when().
                post(BASEAPI + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion() + "/signeringshash").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();
    }

    @Test
    public void testServerSigneraUtkast() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        given().contentType(ContentType.JSON).expect().statusCode(200).when().
                post(BASEAPI + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion() + "/signeraserver").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();
    }

    @Test
    public void testBiljettStatus() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        Response reponseTicket = given().contentType(ContentType.JSON).expect().statusCode(200).when().
                post(BASEAPI + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion() + "/signeringshash").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(reponseTicket.body().asString());
        String biljettId = model.getString("id");

        given().contentType(ContentType.JSON).expect().statusCode(200).when().
                get(BASEAPI + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus").then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"));
    }

    private Intyg createIntyg() throws IOException {
        String intygsTyp = "fk7263";

        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = given().expect().statusCode(200).when().get(BASEAPI + "/" + intygsTyp + "/" + intygsId).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

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

        responseIntyg = given().contentType(ContentType.JSON).body(content).expect().statusCode(200).when().
                put(BASEAPI + "/" + intygsTyp + "/" + intygsId + "/" + version).then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json")).
                body("version", equalTo(Integer.parseInt(version) + 1)).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());

        version = model.getString("version");

        return new Intyg(version, intygsId, intygsTyp);
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
