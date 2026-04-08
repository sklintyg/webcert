/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.BROWSER_INFO;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.DIAGNOSKODVERK_CHANGED;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import se.inera.intyg.webcert.infra.monitoring.logging.UserAgentInfo;
import se.inera.intyg.webcert.infra.monitoring.logging.UserAgentParser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class JsLogApiControllerTest {

  private static final String userAgentString = "a user agent";
  @Mock private MonitoringLogService monLog;
  @Mock private UserAgentParser userAgentParser;
  @InjectMocks private JsLogApiController controller;

  @BeforeEach
  void setupMocks() {
    when(userAgentParser.parse(anyString()))
        .thenReturn(new UserAgentInfo("IE", "1.0", "OS", "1.1"));
  }

  @Test
  void testMonitoringBadRequest() {
    assertEquals(400, controller.monitoring(null, null).getStatusCode().value());

    assertEquals(400, controller.monitoring(new MonitoringRequest(), null).getStatusCode().value());

    MonitoringRequest request = new MonitoringRequest();

    request.setEvent(DIAGNOSKODVERK_CHANGED);
    assertEquals(400, controller.monitoring(request, null).getStatusCode().value());

    request.setEvent(BROWSER_INFO);
    assertEquals(400, controller.monitoring(request, null).getStatusCode().value());
  }

  @Test
  void testMonitoringBrowserInfo() {
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

    ResponseEntity<?> response = controller.monitoring(request, userAgentString);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());

    verify(monLog).logBrowserInfo("IE", "1.0", "OS", "1.1", width, height, netIdVersion);
    verifyNoMoreInteractions(monLog);
  }

  @Test
  void testMonitoringDiagnoskodverkChanged() {
    final String intygId = "intygId";
    final String intygType = "intygType";

    MonitoringRequest request = new MonitoringRequest();
    request.setEvent(DIAGNOSKODVERK_CHANGED);
    Map<String, String> extraInfo = new HashMap<>();
    extraInfo.put(INTYG_ID, intygId);
    extraInfo.put(INTYG_TYPE, intygType);
    request.setInfo(extraInfo);

    ResponseEntity<?> response = controller.monitoring(request, userAgentString);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());

    verify(monLog).logDiagnoskodverkChanged(intygId, intygType);
    verifyNoMoreInteractions(monLog);
  }
}
