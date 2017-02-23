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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.webcert.web.web.controller.api.dto.*;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Basic test suite that verifies that the endpoint (/api/intyg) for generic intygs operations (list
 * drafts/copy/notification status)
 * are available and reponds according to specification.
 *
 * Created by marced on 01/12/15.
 */
public class IntygAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetListDraftsAndIntygForPersonWithEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        ListIntygEntry[] intygArray = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER)
                .expect().statusCode(200)
                .when().get("api/intyg/person/{personNummer}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-list-response-schema.json")).extract().response()
                .as(ListIntygEntry[].class);

        // assert there are no drafts from WebCert
        assertFalse(Arrays.asList(intygArray).stream().anyMatch(i -> IntygSource.WC.equals(i.getSource())));
    }

    @Test
    public void testGetListDraftsAndIntygForPersonWithNonEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        ListIntygEntry[] intygArray =
                given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER)
                        .expect().statusCode(200)
                        .when().get("api/intyg/person/{personNummer}")
                        .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-list-response-schema.json")).extract().response()
                        .as(ListIntygEntry[].class);

        assertTrue(intygArray.length > 0);

        assertEquals(utkastId, intygArray[0].getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, intygArray[0].getPatientId().getPersonnummer());
        assertEquals("fk7263", intygArray[0].getIntygType());
    }

    @Test
    public void testNotifiedOnUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);
        pathParams.put("version", "0");

        NotifiedState notifiedState = new NotifiedState();
        notifiedState.setNotified(true);

        ListIntygEntry updatedIntyg =
                given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).contentType(ContentType.JSON).and().body(notifiedState).and().pathParams(pathParams)
                        .expect().statusCode(200)
                        .when().put("api/intyg/{intygsTyp}/{intygsId}/{version}/vidarebefordra")
                        .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-put-notified-utkast-response-schema.json")).extract().response()
                        .as(ListIntygEntry.class);

        assertNotNull(updatedIntyg);

        assertEquals(utkastId, updatedIntyg.getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, updatedIntyg.getPatientId().getPersonnummer());
        assertEquals("fk7263", updatedIntyg.getIntygType());

        // it's been updated, so version should have been incremented
        assertEquals(1, updatedIntyg.getVersion());

        // and of course it should have been vidarebefordrad as we instructed
        assertTrue(updatedIntyg.isVidarebefordrad());
    }

}
