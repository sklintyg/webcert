/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.internalapi;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Very basic test of the FMB API that requests an icd10 code known to have fmb info and one that doesn't.
 */
public class IntegratedUnitsApiControllerIT extends InternalApiBaseRestIntegrationTest {

    private String url = "/internalapi/integratedUnits/";

    @Test
    public void getAllIntegratedUnits() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + "all")
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-integrated-units-response-schema.json"));
    }

    @Test
    public void getIntegratedUnit() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String unitId = "SE4815162344-1A02";

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + unitId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-integrated-unit-response-schema.json"));
    }

    @Test
    public void getIntegratedUnitNotFound() {
        given().expect().statusCode(NOT_FOUND)
            .when()
            .get( url + "NOT_FOUND");
    }

}
