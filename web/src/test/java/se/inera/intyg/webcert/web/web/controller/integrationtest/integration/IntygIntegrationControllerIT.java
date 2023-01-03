/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFRAME_OPTIONS_HEADER;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Created by marced on 16/12/15.
 */
public class IntygIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_INTYGSID = "abcd123-abcd123-abcd123";


    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link and gets redirected to the correct url.
     */
    @Test
    public void testRedirectSuccessUtkastUsingGET() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsId", utkastId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER);
        queryParams.put("responsibleHospName", "HrDoktor");
        queryParams.put("enhet", "IFV1239877878-1042");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParams(pathParams)
            .and().queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when().get("/visa/intyg/{intygsId}")
            .then()
            .header(HttpHeaders.LOCATION, endsWith("/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"))
            .header(XFRAME_OPTIONS_HEADER, equalToIgnoringCase("DENY"));
    }

    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link and gets redirected to the correct url.
     */
    @Test
    public void testRedirectSuccessUtkastUsingGETWithIntygTyp() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsId", utkastId);
        pathParams.put("intygsTyp", "TSTRK1007");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER);
        queryParams.put("responsibleHospName", "HrDoktor");
        queryParams.put("enhet", "IFV1239877878-1042");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParams(pathParams)
            .and().queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when().get("/visa/intyg/{intygsTyp}/{intygsId}")
            .then()
            .header(HttpHeaders.LOCATION, endsWith("/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"))
            .header(XFRAME_OPTIONS_HEADER, equalToIgnoringCase("DENY"));
    }

    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link and gets redirected to the correct url.
     */
    @Test
    public void testRedirectSuccessUtkastUsingPOST() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> pathParams = ImmutableMap.of(
            "intygsId", utkastId);

        Map<String, String> formParams = ImmutableMap.of(
            "alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER,
            "responsibleHospName", "HrDoktor",
            "enhet", "IFV1239877878-1042");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParams(pathParams)
            .and().formParams(formParams)
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when().post("/visa/intyg/{intygsId}")
            .then().header(HttpHeaders.LOCATION, endsWith("/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"));
    }

    /**
     * Verify that a djupintegrerad lakare can use a intyg redirect link and gets redirected to the correct url (that is
     * different from an utkast link).
     */
    @Test
    public void testRedirectSuccessSigneratIntygUsingGET() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParam("intygsId", intygsId)
            .and().queryParams("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER, "enhet", "IFV1239877878-1042")
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER).when().get("/visa/intyg/{intygsId}").then()
            .header(HttpHeaders.LOCATION, endsWith("/intyg/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/" + intygsId + "/"));
    }

    @Test
    public void testRedirectSuccessSigneratIntygUsingPOST() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        Map<String, String> pathParams = ImmutableMap.of("intygsId", intygsId);

        changeOriginTo("DJUPINTEGRATION");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParams(pathParams)
            .and().formParams("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER, "enhet", "IFV1239877878-1042")
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER).when().post("/visa/intyg/{intygsId}").then()
            .header(HttpHeaders.LOCATION, endsWith("/intyg/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/" + intygsId + "/"));
    }

    /**
     * Verify that a lakare can't use a utkast redirect link.
     */
    @Test
    public void testRedirectFailsWithInvalidRole() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParam("intygsId", DEFAULT_INTYGSID)
            .expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT)
            .when().get("visa/intyg/{intygsId}?alternatePatientSSn=x&responsibleHospName=x&enhet=IFV1239877878-1042")
            .then().header(HttpHeaders.LOCATION, endsWith("/error.jsp?reason=auth-exception"));
    }

    /**
     * Verify that a lakare can't use a utkast redirect link.
     */
    @Test
    public void testRedirectFailsWhenCertificateHasBeenDeleted() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = "1357924680";

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER)
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "patientfornamn")
            .put("efternamn", "patientefternamn")
            .put("mellannamn", "patientmellannamn")
            .put("postadress", "patientpostadress")
            .put("postnummer", "patientpostnummer")
            .put("postort", "patientpostort")
            .put("enhet", "IFV1239877878-1042")
            .build();

        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT)
            .when().get("/visa/intyg/{intygsId}")
            .then().header(HttpHeaders.LOCATION, endsWith("/error.jsp?reason=integration.nocontent"));

    }

    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link for intygstypluse and gets redirected to
     * the correct url.
     */
    @Test
    public void testRedirectSuccessUtkastLuse() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("luse", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER)
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "patientfornamn")
            .put("efternamn", "patientefternamn")
            .put("mellannamn", "patientmellannamn")
            .put("postadress", "patientpostadress")
            .put("postnummer", "patientpostnummer")
            .put("postort", "patientpostort")
            .put("enhet", "IFV1239877878-1042")
            .build();

        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect()
            .statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when()
            .get("/visa/intyg/{intygsId}")
            .then()
            .header(HttpHeaders.LOCATION, endsWith("/luse/" + LUSE_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"));

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .prettyPeek()
            .then()
            .body("parameters.alternateSsn", equalTo(DEFAULT_PATIENT_PERSONNUMMER))
            .body("parameters.responsibleHospName", equalTo("HrDoktor"))
            .body("parameters.fornamn", equalTo("patientfornamn"))
            .body("parameters.mellannamn", equalTo("patientmellannamn"))
            .body("parameters.efternamn", equalTo("patientefternamn"))
            .body("parameters.postadress", equalTo("patientpostadress"))
            .body("parameters.postnummer", equalTo("patientpostnummer"))
            .body("parameters.postort", equalTo("patientpostort"));
    }

    /**
     * Verify that the utkast patient info is updated with supplied parameters as part of the djupintegreration link
     * redirect process.
     */
    @Test
    public void testPatientDetailsUpdatedFromJournalSystemUtkastLuseAndPUServiceIsUnavailable() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("luse", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo(UserOriginType.DJUPINTEGRATION.name());

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", "19121212-1212")
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "nyaförnamnet")
            .put("efternamn", "nyaefternamnet")
            .put("mellannamn", "nyamellannamnet")
            .put("postadress", "nyvägen 12")
            .put("postnummer", "000001")
            .put("postort", "sjukort")
            .put("enhet", "IFV1239877878-1042")
            .build();

        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect()
            .statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when()
            .get("/visa/intyg/{intygsId}")
            .then()
            .header(HttpHeaders.LOCATION, endsWith("/luse/" + LUSE_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"));

        spec()
            .expect().statusCode(200)
            .when().get("moduleapi/utkast/luse/" + utkastId)
            .then().body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("content.grundData.patient.personId", equalTo(formatPersonnummer(queryParams.get("alternatePatientSSn"))));

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .prettyPeek()
            .then()
            .body("parameters.alternateSsn", equalTo("19121212-1212"))
            .body("parameters.responsibleHospName", equalTo("HrDoktor"))
            .body("parameters.fornamn", equalTo("nyaförnamnet"))
            .body("parameters.mellannamn", equalTo("nyamellannamnet"))
            .body("parameters.efternamn", equalTo("nyaefternamnet"))
            .body("parameters.postadress", equalTo("nyvägen 12"))
            .body("parameters.postnummer", equalTo("000001"))
            .body("parameters.postort", equalTo("sjukort"));
    }

    /**
     * Verify that the utkast patientId info is updated with supplied parameters as part of the ts-bas djupintegreration
     * link redirect process.
     */
    @Test
    public void testOnlyPatientIdDetailsUpdatedFromJournalSystemUtkastTsBas() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo(UserOriginType.DJUPINTEGRATION.name());

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", "19121212-1212")
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "nyaförnamnet")
            .put("efternamn", "nyaefternamnet")
            .put("mellannamn", "nyamellannamnet")
            .put("postadress", "nyvägen 12")
            .put("postnummer", "000001")
            .put("postort", "sjukort")
            .put("enhet", "IFV1239877878-1042")
            .build();

        // Go to deep integration link with other patient info than on current utkast...
        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect()
            .statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when()
            .get("/visa/intyg/{intygsId}")
            .then()
            .header(HttpHeaders.LOCATION, endsWith("/ts-bas/" + TS_BAS_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"));

        // ..after following the link - the draft should have updated patient id and fullstandigtNamn
        spec()
            .expect().statusCode(200)
            .when().get("moduleapi/utkast/ts-bas/" + utkastId)
            .then()
            .body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-response-schema.json"))
            .body("content.grundData.patient.personId", equalTo(formatPersonnummer(queryParams.get("alternatePatientSSn"))));

        // INTYG-4086: Vi vet ännu inte huruvida man skall kunna uppdatera patientens namn via parametrar...
        // .body("content.grundData.patient.fullstandigtNamn", isEmptyOrNullString());

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .prettyPeek()
            .then()
            .body("parameters.alternateSsn", equalTo("19121212-1212"))
            .body("parameters.responsibleHospName", equalTo("HrDoktor"))
            .body("parameters.fornamn", equalTo("nyaförnamnet"))
            .body("parameters.mellannamn", equalTo("nyamellannamnet"))
            .body("parameters.efternamn", equalTo("nyaefternamnet"))
            .body("parameters.postadress", equalTo("nyvägen 12"))
            .body("parameters.postnummer", equalTo("000001"))
            .body("parameters.postort", equalTo("sjukort"));
    }

    /**
     * Verify that a djupintegrerad lakare can use a intyg redirect link for intygstyp luse and gets redirected to
     * the correct url (that is different from an utkast link).
     */
    @Test
    public void testRedirectSuccessSigneratIntygLuse() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("luse", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER)
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "patientfornamn")
            .put("efternamn", "patientefternamn")
            .put("mellannamn", "patientmellannamn")
            .put("postadress", "patientpostadress")
            .put("postnummer", "patientpostnummer")
            .put("postort", "patientpostort")
            .put("enhet", "IFV1239877878-1042")
            .build();

        spec()
            .redirects().follow(false)
            .and().pathParam("intygsId", intygsId)
            .and().queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER).when().get("/visa/intyg/{intygsId}")
            .then().header(HttpHeaders.LOCATION, endsWith("/intyg/luse/" + LUSE_BASE_INTYG_TYPE_VERSION + "/" + intygsId + "/"));

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .prettyPeek()
            .then()
            .body("parameters.alternateSsn", equalTo(DEFAULT_PATIENT_PERSONNUMMER))
            .body("parameters.responsibleHospName", equalTo("HrDoktor"))
            .body("parameters.fornamn", equalTo("patientfornamn"))
            .body("parameters.mellannamn", equalTo("patientmellannamn"))
            .body("parameters.efternamn", equalTo("patientefternamn"))
            .body("parameters.postadress", equalTo("patientpostadress"))
            .body("parameters.postnummer", equalTo("patientpostnummer"))
            .body("parameters.postort", equalTo("patientpostort"));
    }

    @Test
    public void testRedirectForLuseWithMissingPatientInformation() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("luse", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        spec()
            .redirects()
            .follow(false)
            .and().pathParam("intygsId", utkastId)
            .expect().statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when().get("visa/intyg/{intygsId}?alternatePatientSSn=x&responsibleHospName=x&enhet=IFV1239877878-1042")
            .then().header(HttpHeaders.LOCATION, endsWith("/luse/" + LUSE_BASE_INTYG_TYPE_VERSION + "/edit/" + utkastId + "/"));

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .prettyPeek()
            .then()
            .body("parameters.alternateSsn", equalTo("x"))
            .body("parameters.responsibleHospName", equalTo("x"))
            .body("$", not(hasKey("parameters.fornamn")));
    }

    /**
     * Verify that request without enhet is redirected to unit selection page.
     */
    @Test
    public void testUserIsRedirectedToUnitSelectionPageWhenNoEnhetIsSpecified() {
        RestAssured.sessionId = getAuthSession(ASA_ANDERSSON);

        String utkastId = createUtkast("luse", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo("DJUPINTEGRATION");

        Map<String, String> pathParams = ImmutableMap.of("intygsId", utkastId);

        Map<String, String> queryParams = ImmutableMap.<String, String>builder()
            .put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER)
            .put("responsibleHospName", "HrDoktor")
            .put("fornamn", "nyaförnamnet")
            .put("efternamn", "nyaefternamnet")
            .put("mellannamn", "nyamellannamnet")
            .put("postadress", "nyvägen 12")
            .put("postnummer", "000001")
            .put("postort", "sjukort")
            .build();

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
            .redirects().follow(false)
            .and().pathParams(pathParams)
            .and().queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT)
            .when().get("visa/intyg/{intygsId}")
            .then().header(HttpHeaders.LOCATION, endsWith("#/integration-enhetsval"));
    }

    @Test
    public void testInactiveUnitIsSet() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo(UserOriginType.DJUPINTEGRATION.name());

        Map<String, String> queryParams = ImmutableMap.of("inaktivEnhet", "true");

        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect()
            .statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when()
            .get("/visa/intyg/{intygsId}");

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .then()
            .body("parameters.inactiveUnit", equalTo(true));
    }

    @Test
    public void testFornyaOkIsSet() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("ts-bas", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo(UserOriginType.DJUPINTEGRATION.name());

        Map<String, Object> queryParams = ImmutableMap.of(IntygIntegrationController.PARAM_FORNYA_OK, true);

        spec()
            .redirects()
            .follow(false)
            .pathParam("intygsId", utkastId)
            .queryParams(queryParams)
            .expect()
            .statusCode(HttpServletResponse.SC_SEE_OTHER)
            .when()
            .get("/visa/intyg/{intygsId}");

        spec(100)
            .expect().statusCode(200)
            .when().get("api/anvandare")
            .then()
            .body("parameters.fornyaOk", equalTo(true));
    }

}
