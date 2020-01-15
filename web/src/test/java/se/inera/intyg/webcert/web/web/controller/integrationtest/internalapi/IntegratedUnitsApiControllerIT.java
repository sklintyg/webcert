/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;

/**
 * Very basic test of the FMB API that requests an icd10 code known to have fmb info and one that doesn't.
 */
public class IntegratedUnitsApiControllerIT extends InternalApiBaseRestIntegrationTest {

    private static String ENHET_1 = "enhet-test123";
    private String url = "/internalapi/integratedUnits/";

    @Before
    public void setup() {
        createIntegreradEnhet(ENHET_1);
    }

    @After
    public void after() {
        deleteIntegreradEnhet(ENHET_1);
    }

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

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + ENHET_1)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-integrated-unit-response-schema.json"));
    }

    @Test
    public void getIntegratedUnitNotFound() {
        given().expect().statusCode(NOT_FOUND)
            .when()
            .get( url + "NOT_FOUND");
    }

    private void deleteIntegreradEnhet(String enhetsId) {
        Response response = spec().delete("testability/integreradevardenheter/" + enhetsId);
    }

    private void createIntegreradEnhet(String enhetsId) {
        IntegreradEnhetEntryWithSchemaVersion enhet = new IntegreradEnhetEntryWithSchemaVersion();
        enhet.setEnhetsId(enhetsId);
        enhet.setEnhetsNamn("enhet1-namn");
        enhet.setVardgivareId("vg1-id");
        enhet.setVardgivareNamn("vg1-namn");

        Response response = spec()
            .contentType(ContentType.JSON).body(enhet)
            .expect().statusCode(200)
            .when().post("testability/integreradevardenheter")
            .then().extract().response();
    }

}
