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

package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Very basic test of the FMB API that requests an icd10 code known to have fmb info and one that doesn't.
 */
public class FmbAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void getFmbForKnownIcd10() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        //J22 is added programatically by the fmbStub ad also used by other tests.
        given().pathParam("icd10", "J22").expect().statusCode(200).when().get("api/fmb/{icd10}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fmb-query-response-schema.json")).
                body("icd10Code", equalTo("J22")).
                body("forms", hasSize(4));
    }

    @Test
    public void getFmbForUnKnownIcd10() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().pathParam("icd10", "X1337").expect().statusCode(200).when().get("api/fmb/{icd10}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-fmb-query-response-schema.json")).
                body("icd10Code", equalTo("X1337")).
                body("forms", hasSize(0));
    }

}
