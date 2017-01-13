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
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DiagnosParameter;

public class DiagnosModuleApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testSearchDiagnosisByCode() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        DiagnosParameter body = new DiagnosParameter();
        body.setCodeFragment("A01");
        body.setCodeSystem(Diagnoskodverk.ICD_10_SE.name());
        body.setNbrOfResults(4);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).and().body(body)
                .expect().statusCode(200)
                .when().post("moduleapi/diagnos/kod/sok")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-diagnos-sok-schema.json"))
                .body("diagnoser", hasSize(4))
                .body("moreResults", equalTo(true))
                .body("resultat", equalTo("OK"));
    }

    @Test
    public void testSearchDiagnosisByDescription() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        DiagnosParameter body = new DiagnosParameter();
        body.setDescriptionSearchString("C");
        body.setCodeSystem(Diagnoskodverk.ICD_10_SE.name());
        body.setNbrOfResults(4);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).and().body(body)
                .expect().statusCode(200)
                .when().post("moduleapi/diagnos/beskrivning/sok")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-diagnos-sok-schema.json"))
                .body("diagnoser", hasSize(4))
                .body("moreResults", equalTo(true))
                .body("resultat", equalTo("OK"));
    }
}
