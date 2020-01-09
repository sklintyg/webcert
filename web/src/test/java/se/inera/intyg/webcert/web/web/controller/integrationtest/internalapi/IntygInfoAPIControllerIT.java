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
import org.junit.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Very basic test of the FMB API that requests an icd10 code known to have fmb info and one that doesn't.
 */
public class IntygInfoAPIControllerIT extends InternalApiBaseRestIntegrationTest {

    public static final int OK = HttpStatus.OK.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();

    private static final String INTYGSTYP = "lisjp";
    private String url = "/internalapi/intygInfo/";

    @Test
    public void getInfoOnlyArende() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = "test123";
        createArendeQuestion(INTYGSTYP, intygsId, DEFAULT_PATIENT_PERSONNUMMER, ArendeAmne.KOMPLT);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-info-response-schema.json"));
    }

    @Test
    public void getInfoOnlyFragaSvar() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = "test123456";
        createQuestion(INTYGSTYP, intygsId, DEFAULT_PATIENT_PERSONNUMMER, Amne.OVRIGT);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-info-response-schema.json"));
    }

    @Test
    public void getInfoUtkast() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast(INTYGSTYP, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .expect().statusCode(OK)
            .when()
            .get( url + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-info-response-schema.json"));
    }

    @Test
    public void getInfoNotFound() {
        given().expect().statusCode(NOT_FOUND)
            .when()
            .get( url + "NOT_FOUND");
    }

}
