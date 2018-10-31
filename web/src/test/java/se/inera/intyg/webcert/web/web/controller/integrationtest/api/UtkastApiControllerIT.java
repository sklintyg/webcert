/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import java.util.ArrayList;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by marced on 17/11/15.
 */
public class UtkastApiControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_LAKARE_NAME = "Jan Nilsson";

    @Test
    public void testGetTsBasUtkast() {
        JsonPath model = testCreateUtkast("ts-bas");
        // INTYG-4086 - do NOT store name or address for FK intyg in DB
        assertEquals(DEFAULT_UTKAST_PATIENT_FORNAMN, model.getString("grundData.patient.fornamn"));
        assertEquals(DEFAULT_UTKAST_PATIENT_EFTERNAMN, model.getString("grundData.patient.efternamn"));
    }

    @Test
    public void testGetTsDiabetesUtkast() {
        JsonPath model = testCreateUtkast("ts-diabetes");
        // INTYG-4086 - do NOT store name or address for FK intyg in DB
        // ts-diabetes v3 does not have patient in grundData
        //assertEquals(DEFAULT_UTKAST_PATIENT_FORNAMN, model.getString("grundData.patient.fornamn"));
        //assertEquals(DEFAULT_UTKAST_PATIENT_EFTERNAMN, model.getString("grundData.patient.efternamn"));
        assertEquals("3.0", model.getString("textVersion"));
    }

    @Test
    public void testCannotCreateUtkastOfDeprecatedType() {
        String utkastType = "fk7263";

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        CreateUtkastRequest utkastRequest = createUtkastRequest(utkastType, DEFAULT_PATIENT_PERSONNUMMER);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).body(utkastRequest)
                .expect().statusCode(400)
                .when().post("api/utkast/" + utkastType);
    }

    /**
     * Generic method that created an utkast of given type and validates basic generic model properties
     *
     * @param utkastType
     *            The type of utkast to create
     */
    private JsonPath testCreateUtkast(String utkastType) {

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        CreateUtkastRequest utkastRequest = createUtkastRequest(utkastType, DEFAULT_PATIENT_PERSONNUMMER);

        Response response = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .contentType(ContentType.JSON).body(utkastRequest)
                .expect().statusCode(200)
                .when().post("api/utkast/" + utkastType)
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json"))
                .body("intygsTyp", equalTo(utkastRequest.getIntygType()))
                .body("skapadAv.hsaId", equalTo(DEFAULT_LAKARE.getHsaId()))
                .body("enhetsId", equalTo(DEFAULT_LAKARE.getEnhetId()))
                .body("version", equalTo(0))
                .body("skapadAv.namn", equalTo(DEFAULT_LAKARE_NAME))
                .body("patientFornamn", equalTo(utkastRequest.getPatientFornamn()))
                .body("patientEfternamn", equalTo(utkastRequest.getPatientEfternamn()))
                .extract().response();

        // The type-specific model is a serialized json "within" the model property, need to extract that first and then
        // we can assert some basic things.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertTrue(model.getString("id").length() > 0);

        assertEquals(utkastRequest.getPatientPersonnummer().getPersonnummer(), model.getString("grundData.patient.personId"));

        return model;
    }

    /**
     * Verify that a lakare with a saved utkast is returned when querying for that.
     */
    @Test
    public void testGetLakareWithDraftsByEnheter() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        Lakare[] lakareWithUtkast = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(200)
                .when().get("api/utkast/lakare")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-lakare-med-utkast-response-schema.json")).extract().response()
                .as(Lakare[].class);

        Assert.assertEquals(1, lakareWithUtkast.length);

        Assert.assertEquals(DEFAULT_LAKARE.getHsaId(), lakareWithUtkast[0].getHsaId());
    }

    /**
     * Verify that filtering by enhetId and hsaId returns expected results.
     */
    @Test
    public void testFilterDraftsForUnit() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        QueryIntygResponse queryResponse = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId())
                .expect().statusCode(200).when().get("api/utkast")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json"))
                .body("totalCount", equalTo(1)).extract().response().as(QueryIntygResponse.class);

        // The only result should match the utkast we created in the setup
        Assert.assertEquals(utkastId, queryResponse.getResults().get(0).getIntygId());
        Assert.assertEquals("ts-bas", queryResponse.getResults().get(0).getIntygType());
        Assert.assertEquals(formatPersonnummer(DEFAULT_PATIENT_PERSONNUMMER), queryResponse.getResults().get(0).getPatientId().getPersonnummer());
    }

    @Test
    public void testFilterDraftsForUnitPagination() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        ArrayList<String> utkastIds = new ArrayList(), utkastPersonIds = new ArrayList();
        for(int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                utkastIds.add(createUtkast("luse", DEFAULT_PATIENT_PERSONNUMMER));
                utkastPersonIds.add(DEFAULT_PATIENT_PERSONNUMMER);
            }
            utkastIds.add(createUtkast("luse", "195401232540")); // Sekretessmarkering på patient
            utkastPersonIds.add("19540123-2540");
        }

        // The newest utkast will be returned first, reverse the expected list
        Collections.reverse(utkastIds);
        Collections.reverse(utkastPersonIds);

        QueryIntygResponse queryResponse = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId())
                .param("pageSize", 4)
                .expect().statusCode(200).when().get("api/utkast")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json"))
                .body("totalCount", equalTo(18)).extract().response().as(QueryIntygResponse.class);

        Assert.assertEquals(4, queryResponse.getResults().size());

        // Removed verification of intyg in the response.
        // The timing resolution is too low on senastSparadDatum when running on the build environment.
        // When running locally with dev profile and h2 we get millisecond resolution and the test works.
        // We would need several seconds of sleep between createUtkast for this to work on the build environment.
/*        for(int i = 0; i < 4; i++) {
            ListIntygEntry entry = queryResponse.getResults().get(i);
            Assert.assertEquals(utkastIds.get(i), entry.getIntygId());
            Assert.assertEquals("fk7263", entry.getIntygType());
            Assert.assertEquals(utkastPersonIds.get(i), entry.getPatientId().getPersonnummer());
        }*/

        QueryIntygResponse queryResponse2 = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId())
                .param("pageSize", 4).param("startFrom", 16)
                .expect().statusCode(200).when().get("api/utkast")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json"))
                .body("totalCount", equalTo(18)).extract().response().as(QueryIntygResponse.class);

        Assert.assertEquals(2, queryResponse2.getResults().size());
/*        for(int i = 0; i < 2; i++) {
            ListIntygEntry entry = queryResponse2.getResults().get(i);
            Assert.assertEquals(utkastIds.get(i + 16), entry.getIntygId());
            Assert.assertEquals("fk7263", entry.getIntygType());
            Assert.assertEquals(utkastPersonIds.get(i + 16), entry.getPatientId().getPersonnummer());
        }*/
    }

    @Test
    public void testFilterDraftsForUnitVardAdminPagination() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        ArrayList<String> utkastIds = new ArrayList(), utkastPersonIds = new ArrayList();
        for(int i = 0; i < 2; i++) {
            utkastIds.add(createUtkast("lisjp", "195401232540")); // Sekretessmarkering på patient
            utkastPersonIds.add("19540123-2540");
            for (int j = 0; j < 8; j++) {
                utkastIds.add(createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER));
                utkastPersonIds.add(DEFAULT_PATIENT_PERSONNUMMER);
            }
        }

        // The newest utkast will be returned first, reverse the expected list
        Collections.reverse(utkastIds);
        Collections.reverse(utkastPersonIds);

        changeRoleTo("VARDADMINISTRATOR");

        // Should only get totalCount=16 since we added 2 patients with sekretessmarkering
        QueryIntygResponse queryResponse = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId())
                .param("pageSize", 4)
                .expect().statusCode(200).when().get("api/utkast")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json"))
                .body("totalCount", equalTo(16)).extract().response().as(QueryIntygResponse.class);

        Assert.assertEquals(4, queryResponse.getResults().size());

        // Disabled. See comment on testFilterDraftsForUnitPagination
/*        for(int i = 0; i < 4; i++) {
            ListIntygEntry entry = queryResponse.getResults().get(i);
            Assert.assertEquals(utkastIds.get(i), entry.getIntygId());
            Assert.assertEquals("fk7263", entry.getIntygType());
            Assert.assertEquals(utkastPersonIds.get(i), entry.getPatientId().getPersonnummer());
        }*/

        QueryIntygResponse queryResponse2 = given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .param("savedBy", DEFAULT_LAKARE.getHsaId()).param("enhetsId", DEFAULT_LAKARE.getEnhetId())
                .param("pageSize", 4).param("startFrom", 14)
                .expect().statusCode(200).when().get("api/utkast")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-query-utkast-response-schema.json"))
                .body("totalCount", equalTo(16)).extract().response().as(QueryIntygResponse.class);

        Assert.assertEquals(2, queryResponse2.getResults().size());

        // With pagesize=4 and startFrom=14 we will get the last 2 entries.
        // Without sekretess markering the 2 entries would have matched 16 and 17.
        // Since 17 is sekretess and filtered out we will get 15 and 16
/*        ListIntygEntry entry = queryResponse2.getResults().get(0);
        Assert.assertEquals(utkastIds.get(15), entry.getIntygId());
        Assert.assertEquals("fk7263", entry.getIntygType());
        Assert.assertEquals(utkastPersonIds.get(15), entry.getPatientId().getPersonnummer());

        entry = queryResponse2.getResults().get(1);
        Assert.assertEquals(utkastIds.get(16), entry.getIntygId());
        Assert.assertEquals("fk7263", entry.getIntygType());
        Assert.assertEquals(utkastPersonIds.get(16), entry.getPatientId().getPersonnummer());*/
    }


    @Test
    public void testGetQuestion() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParams("intygsTyp", "luse", "version", "0.9")
                .expect().statusCode(200)
                .when().get("api/utkast/questions/{intygsTyp}/{version}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-texter.json"));
    }

    @Test
    public void testGetPrevious() {
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);
        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).pathParams("personnummer", "191212121212")
                .expect().statusCode(200)
                .when().get("api/utkast/previousIntyg/{personnummer}")
                .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-previousIntyg.json"));
    }
}
