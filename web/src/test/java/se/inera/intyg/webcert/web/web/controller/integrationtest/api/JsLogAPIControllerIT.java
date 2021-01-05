/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.HEIGHT;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.BROWSER_INFO;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.NET_ID_VERSION;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.WIDTH;

import io.restassured.RestAssured;
import java.util.HashMap;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

/**
 * Created by marced on 01/12/15.
 */
public class JsLogAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testPostDebugLog() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .and().body("rest-api-integrationtest-message")
            .expect().statusCode(200)
            .when().post("api/jslog/debug");
    }

    @Test
    public void testPostMonitoringLogInvalidRequest() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        spec()
            .and().body(new MonitoringRequest())
            .expect().statusCode(400)
            .when().post("api/jslog/monitoring");
    }

    @Test
    public void testPostMonitoringLog() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(BROWSER_INFO);
        HashMap<String, String> info = new HashMap<>();
        info.put(HEIGHT, "1080");
        info.put(WIDTH, "1920");
        info.put(NET_ID_VERSION, "11.0");
        request.setInfo(info);

        spec()
            .and().body(request)
            .expect().statusCode(200)
            .when().post("api/jslog/monitoring");
    }
}
