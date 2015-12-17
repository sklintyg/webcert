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

package se.inera.intyg.webcert.web.web.controller.integrationtest.integration;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

/**
 * Created by marced on 16/12/15.
 */
public class IntygIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String DEFAULT_INTYGSID = "abcd123-abcd123-abcd123";

    /**
     * Verify that a djupintegrerad lakare can use a utkast redirect link and gets redirected to the correct url.
     */
    @Test
    public void testRedirectSuccessUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        changeRoleTo("ROLE_LAKARE_DJUPINTEGRERAD");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsId", utkastId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER);
        queryParams.put("responsibleHospName", "HrDoktor");


        given().redirects().follow(false).pathParam("intygsId", utkastId).queryParams(queryParams).
                expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
                when().get("/visa/intyg/{intygsId}").
                then().header(HttpHeaders.LOCATION, endsWith("/fk7263/edit/" + utkastId + "?patientId=" + queryParams.get("alternatePatientSSn") + "&hospName=" + queryParams.get("responsibleHospName")));
    }

    /**
     * Verify that a djupintegrerad lakare can use a intyg redirect link and gets redirected to the correct url (that is different from an utkast link).
     */
    @Test
    public void testRedirectSuccessSigneratIntyg() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String intygsId = createSignedIntyg("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        changeRoleTo("ROLE_LAKARE_DJUPINTEGRERAD");

        given().redirects().follow(false).and().pathParam("intygsId", intygsId).and().queryParam("alternatePatientSSn", DEFAULT_PATIENT_PERSONNUMMER).
                expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT).
                when().get("/visa/intyg/{intygsId}").
                then().header(HttpHeaders.LOCATION, endsWith("/intyg/fk7263/" + intygsId + "?patientId=" + DEFAULT_PATIENT_PERSONNUMMER));
    }

    /**
     * Verify that a lakare can't use a utkast redirect link.
     */
    @Test
    public void testRedirectFailsWithInvalidRole() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().redirects().follow(false).and().pathParam("intygsId", DEFAULT_INTYGSID).
                expect().statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                when().get("visa/intyg/{intygsId}?alternatePatientSSn=x&responsibleHospName=x");
    }
}

