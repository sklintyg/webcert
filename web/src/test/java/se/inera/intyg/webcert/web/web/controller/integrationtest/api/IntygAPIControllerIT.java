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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.NotifiedState;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Basic test suite that verifies that the endpoint (/api/intyg) for generic intygs operations (list
 * drafts/copy/notification status)
 * are available and reponds according to specification.
 * <p/>
 * Created by marced on 01/12/15.
 */
public class IntygAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetListDraftsAndIntygForPersonWithEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when().get("api/intyg/person/{personNummer}").
                then().
                body(equalTo("[]"));
    }

    @Test
    public void testGetListDraftsAndIntygForPersonWithNonEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        ListIntygEntry[] intygArray =
                given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when()
                        .get("api/intyg/person/{personNummer}").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-list-response-schema.json")).extract().response()
                        .as(ListIntygEntry[].class);

        assertEquals(1, intygArray.length);

        assertEquals(utkastId, intygArray[0].getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, intygArray[0].getPatientId().getPersonnummer());
        assertEquals("fk7263", intygArray[0].getIntygType());
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(200).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").
                then().
                body("intygsUtkastId", not(isEmptyString())).
                body("intygsUtkastId", not(equalTo(utkastId))).
                body("intygsTyp", equalTo("fk7263"));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastFailsForMissingPriveliges() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Lakare role has the KOPIERA_INTYG priviledge, but the privilege is restricted to origintype=NORMAL /
        // DJUPINTEGRERAD.
        // We change the current users origin to be uthop which should trigger an auth exception response.
        changeOriginTo(WebCertUserOriginType.UTHOPP.name());

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).
                expect().statusCode(500).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").
                then().
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name())).
                body("message", not(isEmptyString()));

    }

    /**
     * Check that trying to copy utkast with bad input gives error response.
     */
    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastWithInvalidPatientpersonNummer() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(null);
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(500).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").then().
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM.name())).
                body("message", not(isEmptyString()));

    }

    @Test
    public void testCreateNewUtkastCopyBasedOnIntygFromDifferentCareUnitWithCoherentJournalingSuccess() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Then logout
        given()
            .expect().statusCode(HttpServletResponse.SC_OK)
            .when().get("logout");

        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // Set coherentJournaling=true in copyIntygRequest, this is normally done in the js using the copyService.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setCoherentJournaling(true);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);

        String newIntygsId = given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
            .expect().statusCode(HttpServletResponse.SC_OK)
            .when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                    .body("intygsUtkastId", not(isEmptyString()))
                    .body("intygsUtkastId", not(equalTo(intygsId)))
                    .body("intygsTyp", equalTo("fk7263"))
                        .extract()
                            .path("intygsUtkastId");

        // Check that the copy contains the correct stuff
        given().expect().statusCode(200)
        .when().get("moduleapi/intyg/fk7263/" + newIntygsId + "?sjf=true")
            .then()
                .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-intyg-response-schema.json"))
                .body("contents.grundData.skapadAv.personId", equalTo(LEONIE_KOEHL.getHsaId()))
                .body("contents.grundData.patient.personId", equalTo(DEFAULT_PATIENT_PERSONNUMMER));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnIntygFromDifferentCareUnitWithCoherentJournalingFail() throws IOException {
        // First use DEFAULT_LAKARE to create a signed certificate on care unit A.
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        String intygsId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);
        
        // Then logout
        given()
        .expect().statusCode(HttpServletResponse.SC_OK)
        .when().get("logout");
        
        // Next, create new user credentials with another care unit B, and attempt to access the certificate created in previous step.
        RestAssured.sessionId = getAuthSession(LEONIE_KOEHL);
        changeOriginTo("DJUPINTEGRATION");

        // coherentJournaling defaults to false, so don't set it here.
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", intygsId);
        
        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest)
                .expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera")
                .then()
                    .body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name()))
                    .body("message", not(isEmptyString()));
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
                given().contentType(ContentType.JSON).and().body(notifiedState).and().pathParams(pathParams).expect().statusCode(200).when()
                        .put("api/intyg/{intygsTyp}/{intygsId}/{version}/vidarebefordra").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-put-notified-utkast-response-schema.json")).extract().response()
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
