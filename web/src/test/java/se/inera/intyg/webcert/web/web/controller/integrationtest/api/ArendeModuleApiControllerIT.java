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
package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.sessionId;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateMessageParameter;

public class ArendeModuleApiControllerIT extends BaseRestIntegrationTest {

    private static final String INTYGSTYP = "luse";

    @Test
    public void testArendeForIntyg() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        createArendeQuestion(INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().pathParameter("intygsId", intygId).expect().statusCode(200).when()
                .get("moduleapi/arende/{intygsId}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-list-schema.json"))
                .body("$", hasSize(1));
    }

    @Test
    public void testAnswer() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        // Setup a question that we can use to answer
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        String internId = createArendeQuestion(INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().contentType(ContentType.JSON).pathParams("intygsTyp", INTYGSTYP, "meddelandeId", internId).body("svarsText")
                .expect().statusCode(200).when().put("moduleapi/arende/{intygsTyp}/{meddelandeId}/besvara").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-schema.json"));
    }

    @Test
    public void testSetForwarded() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        String internId = createArendeQuestion(INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().contentType(ContentType.JSON).pathParams("intygsTyp", DEFAULT_INTYGSTYP, "meddelandeId", internId).body(Boolean.TRUE)
                .expect().statusCode(200).when().put("moduleapi/arende/{intygsTyp}/{meddelandeId}/vidarebefordrad").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-schema.json"));
    }

    @Test
    public void createQuestion() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);

        // We need to mark the certificate as sent otherwise we cannot create a question the legitimate way
        sendIntyg(intygId);

        CreateMessageParameter param = new CreateMessageParameter();
        param.setAmne(ArendeAmne.OVRIGT);
        param.setRubrik("rubrik");
        param.setMeddelande("Test");

        given().contentType(ContentType.JSON).pathParams("intygsTyp", INTYGSTYP, "intygsId", intygId).body(param)
                .expect().statusCode(200).when().post("moduleapi/arende/{intygsTyp}/{intygsId}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-schema.json"));
    }

    @Test
    public void closeAsHandled() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        String internId = createArendeQuestion(INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);
        given().pathParams("intygsTyp", INTYGSTYP, "meddelandeId", internId).expect().statusCode(200).when()
                .put("moduleapi/arende/{intygsTyp}/{meddelandeId}/stang").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-schema.json"));
    }

    @Test
    public void openAsUnhandled() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        String internId = createArendeQuestion(INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        // Close the question
        given().pathParams("intygsTyp", INTYGSTYP, "meddelandeId", internId).expect().statusCode(200).when()
                .put("moduleapi/arende/{intygsTyp}/{meddelandeId}/stang");

        // Open the question again
        given().pathParams("intygsTyp", INTYGSTYP, "meddelandeId", internId).expect().statusCode(200).when()
                .put("moduleapi/arende/{intygsTyp}/{meddelandeId}/oppna").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-schema.json"));
    }

}
