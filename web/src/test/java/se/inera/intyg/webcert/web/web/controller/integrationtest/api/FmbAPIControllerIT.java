/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;

import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

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
        final ResponseBody body = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParam("icd10", "J22")
            .expect().statusCode(200)
            .when().get("api/fmb/{icd10}").getBody();

        String test = "";

    }

    @Test
    public void getFmbForUnKnownIcd10() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParam("icd10", "X1337")
            .expect().statusCode(204)
            .when().get("api/fmb/{icd10}");
    }

    @Test
    public void getValideraSjukskrivningstid() {

        //Currently, the fmb database is empty when staring webcert so we need to manually trigger fmb sync to make
        // sure database is populated before actual testing begins. When/if this is automated, this trigger request
        // can be removed.
        given().expect().statusCode(200).when().get("testability/fmb/updatefmbdata");

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        Map<String, String> params = new HashMap<>();
        params.put("icd10Kod1", "S62");
        params.put("personnummer", "191212121212");
        params.put("periods", "{\"from\":\"2020-01-29\",\"tom\":\"2020-02-04\",\"nedsattning\":100}");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).queryParams(params)
            .expect().statusCode(200)
            .when().get("api/fmb/valideraSjukskrivningstid").then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-fmb-validerasjukskrivningstid-response-schema.json"));
    }

}
