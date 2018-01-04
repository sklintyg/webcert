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
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeDraftEntry;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public class ArendeDraftApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testArendeForIntyg() {
        sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygId = "intygId";
        String text = "text";
        String amne = "amne";
        createArendeDraft(intygId, text, amne);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParameter("intygId", intygId)
                .expect()
                    .statusCode(200)
                .when()
                    .get("api/arende/draft/{intygId}")
                .then()
                    .body(matchesJsonSchemaInClasspath("jsonschema/webcert-arende-draft.json"))
                    .body("text", equalTo(text))
                    .body("intygId", equalTo(intygId))
                    .body("amne", equalTo(amne));

        deleteDraft(intygId);
    }

    private void deleteDraft(String intygId) {

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParameters("intygId", intygId)
                .expect()
                    .statusCode(200)
                .when()
                    .delete("api/arende/draft/{intygId}");
    }

    private void createArendeDraft(String intygId, String text, String amne) {
        ArendeDraftEntry entry = new ArendeDraftEntry();
        entry.setIntygId(intygId);
        entry.setText(text);
        entry.setAmne(amne);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).and().body(entry)
                .expect()
                    .statusCode(200)
                .when()
                    .put("api/arende/draft");
    }
}
