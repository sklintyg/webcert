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
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFRAME_OPTIONS_HEADER;

import io.restassured.RestAssured;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Created by marced on 16/12/15.
 */
public class ViewIntegrationControllerIT extends BaseRestIntegrationTest {

    private static final String ORIGIN = "READONLY";

    /**
     * Verify that a doctor can use a draft redirect link
     * and be redirected to the correct url.
     */
    @Test
    public void testRedirectSuccess() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("lisjp", DEFAULT_PATIENT_PERSONNUMMER);

        changeOriginTo(ORIGIN);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsId", utkastId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("enhet", "IFV1239877878-1042");

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId).redirects().follow(false)
            .and().pathParams(pathParams)
            .and().queryParams(queryParams)
            .expect().statusCode(HttpServletResponse.SC_TEMPORARY_REDIRECT)
            .when().get("/visa/intyg/{intygsId}/readonly")
            .then()
            .header(HttpHeaders.LOCATION, stringContainsInOrder(Arrays.asList("/intyg-read-only/lisjp/1." , utkastId)))
            .header(XFRAME_OPTIONS_HEADER, emptyOrNullString());
    }

}
