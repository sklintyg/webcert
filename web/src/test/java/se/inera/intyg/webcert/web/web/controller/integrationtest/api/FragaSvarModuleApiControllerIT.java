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
package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.sessionId;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.web.web.controller.api.dto.QARequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DispatchState;

public class FragaSvarModuleApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testFragaSvarForIntyg() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParameters("intygsTyp", DEFAULT_INTYGSTYP, "intygsId", intygId)
                .expect().statusCode(200)
                .when().get("moduleapi/fragasvar/{intygsTyp}/{intygsId}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-with-extra-info-for-intyg-list-schema.json"))
                .body("$", hasSize(1));

        deleteQuestion(internId);
    }

    @Test
    public void testAnswer() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        // Setup a question that we can use to answer
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON)
                .pathParams("intygsTyp", DEFAULT_INTYGSTYP, "fragasvarId", internId).body("svarsText")
                .expect().statusCode(200).when().put("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/besvara").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-single-fragasvar-for-intyg-schema.json"));

        deleteQuestion(internId);
    }

    @Test
    public void testSetAsVidarebefordrad() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygsId, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).pathParams("intygsId", intygsId)
                .expect().statusCode(200).when().post("moduleapi/fragasvar/{intygsId}/vidarebefordrad").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-for-intyg-list-schema.json"));
        deleteQuestion(internId);
    }

    @Test
    public void createQuestion() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);

        // We need to mark the question as sent otherwise we cannot create a question the legitimate way
        sendIntyg(intygId);

        CreateQuestionParameter param = new CreateQuestionParameter();
        param.setAmne(Amne.OVRIGT);
        param.setFrageText("Test");

        int id = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).pathParams("intygsTyp", DEFAULT_INTYGSTYP, "intygsId", intygId).body(param)
                .expect().statusCode(200).when().post("moduleapi/fragasvar/{intygsTyp}/{intygsId}").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-single-fragasvar-for-intyg-schema.json"))
                .extract().path("internReferens");
        deleteQuestion(id);
    }

    @Test
    public void closeAsHandled() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .pathParams("intygsTyp", DEFAULT_INTYGSTYP, "fragasvarId", internId)
                .expect().statusCode(200)
                .when().get("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/stang")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-single-fragasvar-for-intyg-schema.json"));
        deleteQuestion(internId);
    }

    @Test
    public void closeQAsAsHandled() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        // Create the body of the request
        ArrayList<QARequest> requests = new ArrayList<>();
        QARequest request = new QARequest();
        request.setFragaSvarId((long) internId);
        request.setIntygsTyp(DEFAULT_INTYGSTYP);
        requests.add(request);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).body(requests)
                .expect().statusCode(200)
                .when().put("moduleapi/fragasvar/stang")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-fragasvar-for-intyg-list-schema.json"))
                .body("$", hasSize(requests.size()));
        deleteQuestion(internId);
    }

    // @Test
    public void openAsUnhandled() {
        sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygId = createSignedIntyg(DEFAULT_INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);
        int internId = createQuestion(DEFAULT_INTYGSTYP, intygId, DEFAULT_PATIENT_PERSONNUMMER);

        // Close the question
        given().pathParams("intygsTyp", DEFAULT_INTYGSTYP, "fragasvarId", internId).expect().statusCode(200).when()
                .get("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/stang");

        // Open the question again
        given().pathParams("intygsTyp", DEFAULT_INTYGSTYP, "fragasvarId", internId).expect().statusCode(200).when()
                .get("moduleapi/fragasvar/{intygsTyp}/{fragasvarId}/oppna").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-single-fragasvar-for-intyg-schema.json"));

        deleteQuestion(internId);
    }

}
