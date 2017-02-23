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
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import com.jayway.restassured.RestAssured;
import org.junit.Test;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Verify that the API framework handles Rest framework problems, such as an nonexisting endpoint method in such a
 * way that our custom exception handler is used and responds with our custom error json response.
 *
 * @see se.inera.intyg.webcert.web.web.handlers.WebcertRestExceptionHandler
 * 
 * Created by marced on 01/12/15.
 */
public class RestExceptionHandlerIT extends BaseRestIntegrationTest {


    @Test
    public void testGetNonExistingEndpointMethod() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().cookie("ROUTEID", BaseRestIntegrationTest.routeId)
                .expect().statusCode(500)
                .when().get("api/anvandare/non-existing-endpoint}")
                .then().body("errorCode", equalTo(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM.name()))
                .body("message", not(isEmptyString()));
    }

}
