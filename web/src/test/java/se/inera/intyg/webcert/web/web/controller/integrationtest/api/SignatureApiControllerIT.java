/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static se.inera.intyg.webcert.web.web.controller.integrationtest.moduleapi.UtkastModuleApiControllerIT.GRPAPI_STUBBE_BASE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.awaitility.Durations;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.funktionstjanster.grp.v1.ProgressStatusType;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.auth.eleg.FakeElegCredentials;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class SignatureApiControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_LAKARE_NAME = "Jan Nilsson";

    private static final String SIGNATURE_API_BASE = "/api/signature";
    private static final String SIGNATURE_FAKE_API_BASE = "/api/fake/signature";
    private static final String MODULEAPI_UTKAST_BASE = "moduleapi/utkast";

    @Test
    public void testInitieraSignering() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        spec()
            .expect().statusCode(200)
            .when().post(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
            + "/signeringshash/" + SignMethod.FAKE.name())
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("hash", Matchers.notNullValue())
            .extract().response();
    }

    @Test
    public void testFejkSignera() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        Response response = spec()
            .expect().statusCode(200)
            .when().post(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                + "/signeringshash/" + SignMethod.FAKE.name())
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("hash", Matchers.notNullValue())
            .body("status", equalTo("BEARBETAR"))
            .extract().response();

        String ticketId = response.body().jsonPath().getString("id");

        spec()
            .expect().statusCode(200)
            .when().post(SIGNATURE_FAKE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
            + "/fejksignera/" + ticketId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("SIGNERAD"))
            .extract().response();
    }

    @Test
    public void testServerSigneraUtkastMedGrp() throws IOException {

        FakeElegCredentials fakeElegCredentials = new FakeElegCredentials();
        fakeElegCredentials.setPersonId("19121212-1212");
        fakeElegCredentials.setPrivatLakare(true);
        fakeElegCredentials.setOrigin(UserOriginType.NORMAL.name());
        fakeElegCredentials.setAuthenticationMethod(AuthenticationMethod.MOBILT_BANK_ID.name());

        RestAssured.sessionId = getAuthSession(fakeElegCredentials);

        Intyg intyg = createIntyg();

        // var knownSignStatuses = {'BEARBETAR':'', 'VANTA_SIGN':'', 'NO_CLIENT':'', 'SIGNERAD':'', 'OKAND': ''};

        // Påbörja signering
        Response responseTicket = spec()
            .expect().statusCode(200)
            .when()
            .post(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                + "/signeringshash/" + SignMethod.GRP.name())
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("BEARBETAR"))
            .extract().response();

        // Hämta ut biljett-id från svaret
        JsonPath model = new JsonPath(responseTicket.body().asString());
        String biljettId = model.getString("id");

        // biljettId är inte samma sak som orderRef.
        // Hämta ut orderRef först
        Response responseOrderRef = spec()
            .expect().statusCode(200)
            .when()
            .get(GRPAPI_STUBBE_BASE + "/orderref/" + biljettId)
            .then()
            .extract().response();

        String orderRef = responseOrderRef.body().asString();

        // Ändra GRP-status till USER_SIGN och kontrollera
        spec()
            .body(createGrpSignatureStatus(orderRef, ProgressStatusType.USER_SIGN))
            .expect().statusCode(200)
            .when()
            .put(GRPAPI_STUBBE_BASE + "/status");

        // Verifiera att vi får status VANTA_SIGN inom 6 sekunder.
        await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> spec()
            .expect().statusCode(200)
            .when()
            .get(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("VANTA_SIGN")));

        // Ändra GRP-status till COMPLETE och kontrollera
        spec()
            .body(createGrpSignatureStatus(orderRef, ProgressStatusType.COMPLETE))
            .expect().statusCode(200)
            .when()
            .put(GRPAPI_STUBBE_BASE + "/status");

        await().pollDelay(Durations.TWO_SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted(() -> spec()
            .expect().statusCode(200)
            .when()
            .get(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"))
            .body("status", equalTo("SIGNERAD")));

    }

    @Test
    public void testBiljettStatus() throws IOException {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Intyg intyg = createIntyg();

        Response responseTicket = spec()
            .expect().statusCode(200)
            .when().post(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + intyg.getId() + "/" + intyg.getVersion()
                + "/signeringshash/" + SignMethod.FAKE.name())
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json")).extract().response();

        JsonPath model = new JsonPath(responseTicket.body().asString());
        String biljettId = model.getString("id");

        spec()
            .expect().statusCode(200)
            .when().get(SIGNATURE_API_BASE + "/" + intyg.getIntygsTyp() + "/" + biljettId + "/signeringsstatus")
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-signatur-response-schema.json"));
    }

    @Test
    public void testSigneraUtkastInvalidState() {
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
        String version = model.getString("version");

        spec()
            .expect().statusCode(500)
            .when()
            .post(SIGNATURE_API_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version + "/signeringshash/" + SignMethod.FAKE.name())
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-error-response-schema.json"))
            .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INVALID_STATE.name()))
            .body("message", not(emptyString()));
    }

    private Intyg createIntyg() throws IOException {
        String intygsTyp = "lisjp";

        String intygsId = createUtkast(intygsTyp, DEFAULT_PATIENT_PERSONNUMMER);

        Response responseIntyg = spec()
            .expect().statusCode(200)
            .when().get(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json")).extract().response();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) mapper.readTree(responseIntyg.body().asString());

        String version = rootNode.get("version").asText();

        ObjectNode content = (ObjectNode) rootNode.get("content");

        content.put("avstangningSmittskydd", true);
        content.putArray("diagnoser");

        ArrayNode diagnoser = (ArrayNode) content.get("diagnoser");
        ObjectNode diagnos = JsonNodeFactory.instance.objectNode();
        diagnos.put("diagnosBeskrivning", "Klämskada");
        diagnos.put("diagnosKodSystem", "ICD_10_SE");
        diagnos.put("diagnosKod", "S47");

        diagnoser.add(diagnos);

        content.putArray("sjukskrivningar");
        ArrayNode sjukskrivningar = (ArrayNode) content.get("sjukskrivningar");
        ObjectNode sjukskrivning = new ObjectNode(JsonNodeFactory.instance);

        sjukskrivning.putObject("period");
        ObjectNode period = (ObjectNode) sjukskrivning.get("period");
        period.put("from", "2016-01-19");
        period.put("tom", "2016-01-25");

        sjukskrivning.put("sjukskrivningsgrad", "TRE_FJARDEDEL");
        sjukskrivningar.add(sjukskrivning);

        responseIntyg = spec()
            .body(content)
            .log().all()
            .expect().statusCode(200)
            .when()
            .put(MODULEAPI_UTKAST_BASE + "/" + intygsTyp + "/" + intygsId + "/" + version)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-save-draft-response-schema.json"))
            .body("version", equalTo(Integer.parseInt(version) + 1)).extract().response();

        JsonPath model = new JsonPath(responseIntyg.body().asString());

        version = model.getString("version");

        return new Intyg(version, intygsId, intygsTyp);
    }

    private String createGrpSignatureStatus(String biljettId, ProgressStatusType status) {
        return "{ \"orderRef\": \"" + biljettId + "\", \"status\": \"" + status.value() + "\" }";
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
