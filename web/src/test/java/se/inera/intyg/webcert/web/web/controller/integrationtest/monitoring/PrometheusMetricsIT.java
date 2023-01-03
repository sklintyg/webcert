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
package se.inera.intyg.webcert.web.web.controller.integrationtest.monitoring;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Created by marced on 02/06/16.
 */
public class PrometheusMetricsIT extends BaseRestIntegrationTest {

    @Test
    public void testGetMetrics() {

        given().contentType(ContentType.JSON).
            expect().statusCode(200)
            .body(Matchers.containsString("OK"))
            .when().get("metrics");
    }
}
