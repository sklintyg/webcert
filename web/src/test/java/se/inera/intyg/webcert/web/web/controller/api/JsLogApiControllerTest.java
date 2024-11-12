/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.BROWSER_INFO;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.DIAGNOSKODVERK_CHANGED;

import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.monitoring.logging.UserAgentInfo;
import se.inera.intyg.infra.monitoring.logging.UserAgentParser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

@RunWith(MockitoJUnitRunner.class)
public class JsLogApiControllerTest {

    private static final String userAgentString = "a user agent";
    @Mock
    private MonitoringLogService monLog;
    @Mock
    private UserAgentParser userAgentParser;
    @InjectMocks
    private JsLogApiController controller;

    @Before
    public void setupMocks() {
        when(userAgentParser.parse(anyString())).thenReturn(new UserAgentInfo("IE", "1.0", "OS", "1.1"));
    }

    @Test
    public void testMonitoringBadRequest() {
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(null, null).getStatus());

        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(new MonitoringRequest(), null).getStatus());

        MonitoringRequest request = new MonitoringRequest();

        request.setEvent(DIAGNOSKODVERK_CHANGED);
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(request, null).getStatus());

        request.setEvent(BROWSER_INFO);
        assertEquals(BAD_REQUEST.getStatusCode(), controller.monitoring(request, null).getStatus());
    }

    @Test
    public void testMonitoringBrowserInfo() {
        final String height = "height";
        final String width = "width";
        final String netIdVersion = "netIdVersion";

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(BROWSER_INFO);
        Map<String, String> extraInfo = new HashMap<>();
        extraInfo.put(MonitoringRequest.HEIGHT, height);
        extraInfo.put(MonitoringRequest.WIDTH, width);
        extraInfo.put(MonitoringRequest.NET_ID_VERSION, netIdVersion);
        request.setInfo(extraInfo);

        Response response = controller.monitoring(request, userAgentString);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logBrowserInfo("IE", "1.0", "OS", "1.1", width, height, netIdVersion);
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

        Response response = controller.monitoring(request, userAgentString);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logDiagnoskodverkChanged(intygId, intygType);
        verifyNoMoreInteractions(monLog);
    }

}