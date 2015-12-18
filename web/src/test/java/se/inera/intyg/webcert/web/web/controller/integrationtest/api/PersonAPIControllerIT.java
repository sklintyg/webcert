/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
import org.junit.Before;
import org.junit.Test;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Basic verification of endpoint that looks up patients by personnummer.
 * Created by marced on 01/12/15.
 */
public class PersonAPIControllerIT extends BaseRestIntegrationTest {

//    private static final String PATIENT_PERSON_NUMMER = "191212121212";

    @Before
    public void setup() {
        super.setup();
        //User is not so interesting in this case - so we run all request in same session
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
    }

    @Test
    public void testGetNonExistingPerson() {

        given().pathParam("personNummer", "1201127584").expect().statusCode(200).when().get("api/person/{personNummer}").
                then().
                body("status", equalTo(PersonSvar.Status.NOT_FOUND.name()));
    }

    @Test
    public void testGetExistingPerson() {

        given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when().get("api/person/{personNummer}").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-person-response-schema.json")).
                body("person.personnummer", equalTo(DEFAULT_PATIENT_PERSONNUMMER)).
                body("status", equalTo(PersonSvar.Status.FOUND.name()));
    }
}
