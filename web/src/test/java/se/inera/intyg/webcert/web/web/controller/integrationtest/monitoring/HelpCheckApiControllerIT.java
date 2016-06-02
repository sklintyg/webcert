/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of rehabstod (https://github.com/sklintyg/rehabstod).
 *
 * rehabstod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rehabstod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.integrationtest.monitoring;

import com.jayway.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by marced on 02/06/16.
 */
public class HelpCheckApiControllerIT extends BaseRestIntegrationTest {
    @Test
    public void testHealthCheckPing() {

        given().contentType(ContentType.JSON).
                expect().statusCode(200)
                .body(Matchers.containsString("OK"))
                .when().get("monitoring/health-check/ping");
    }
}
