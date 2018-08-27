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

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Very basic test of the List* endpoints of the /api/receiver endpoint.
 */
public class ReceiverAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void listPossibleReceivers() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        given().expect().statusCode(200).when().get("api/receiver/possiblereceivers/af00213")
                .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-list-possible-receivers-response-schema.json"));
    }

    @Test
    public void listPossibleReceiversWithApprovedForUnknownIntygsId() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        given().expect().statusCode(200).when().get("api/receiver/approvedreceivers/af00213/123").then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-list-approved-receivers-response-schema.json"));

    }
}
