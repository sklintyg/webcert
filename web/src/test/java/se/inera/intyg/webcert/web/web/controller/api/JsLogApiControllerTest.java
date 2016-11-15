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
package se.inera.intyg.webcert.web.web.controller.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.DIAGNOSKODVERK_CHANGED;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.SCREEN_RESOLUTION;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

@RunWith(MockitoJUnitRunner.class)
public class JsLogApiControllerTest {

    @Mock
    private MonitoringLogService monLog;

    @InjectMocks
    private JsLogApiController controller;

    @Test
    public void testMonitoringBadReques() {
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(null).getStatus());

        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(new MonitoringRequest()).getStatus());

        MonitoringRequest request = new MonitoringRequest();

        request.setEvent(DIAGNOSKODVERK_CHANGED);
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(request).getStatus());

        request.setEvent(SCREEN_RESOLUTION);
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(request).getStatus());
    }

    @Test
    public void testMonitoringScreenResolution() {
        final String height = "height";
        final String width = "width";

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(SCREEN_RESOLUTION);
        Map<String, String> extraInfo = new HashMap<>();
        extraInfo.put(MonitoringRequest.HEIGHT, height);
        extraInfo.put(MonitoringRequest.WIDTH, width);
        request.setInfo(extraInfo);

        Response response = controller.monitoring(request);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logScreenResolution(width, height);
        verifyNoMoreInteractions(monLog);
    }

    @Test
    public void testMonitoringDiagnoskodverkChanged() {
        final String intygId = "intygId";
        final String intygType = "intygType";

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(DIAGNOSKODVERK_CHANGED);
        Map<String, String> extraInfo = new HashMap<>();
        extraInfo.put(INTYG_ID, intygId);
        extraInfo.put(INTYG_TYPE, intygType);
        request.setInfo(extraInfo);

        Response response = controller.monitoring(request);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logDiagnoskodverkChanged(intygId, intygType);
        verifyNoMoreInteractions(monLog);
    }

}