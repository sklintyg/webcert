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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import org.junit.Test;

import com.jayway.restassured.RestAssured;

import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Simple test to verify that the modules-map api andpoint responds according to jsonSchema
 *
 * Created by marced on 01/12/15.
 */
public class ModuleAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetModulesMap() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().expect().statusCode(200).when().get("api/modules/map").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-module-map-response-schema.json")).
                body("", hasSize(greaterThan(0))).
                body("id", hasItems("fk7263", "ts-bas", "ts-diabetes"));
    }

    @Test
    public void testGetActiveModules() {
        given().expect().statusCode(200).when().get("api/modules/active").
            then().
            body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-module-map-response-schema.json")).
            body("", hasSize(greaterThan(0))).
            body("id", hasItems("fk7263", "ts-bas", "ts-diabetes"));
    }
}
