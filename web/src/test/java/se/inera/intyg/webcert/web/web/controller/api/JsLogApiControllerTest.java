package se.inera.intyg.webcert.web.web.controller.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.DIAGNOSKODVERK_CHANGED;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MonitoringRequestEvent.REVOKED_PRINTED;
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

        request.setEvent(REVOKED_PRINTED);
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
    public void testMonitoringRevokedPrinted() {
        final String intygId = "intygId";

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(REVOKED_PRINTED);
        Map<String, String> extraInfo = new HashMap<>();
        extraInfo.put(INTYG_ID, intygId);
        request.setInfo(extraInfo);

        Response response = controller.monitoring(request);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logRevokedPrinted(intygId);
        verifyNoMoreInteractions(monLog);
    }

    @Test
    public void testMonitoringDiagnoskodverkChanged() {
        final String intygId = "intygId";

        MonitoringRequest request = new MonitoringRequest();
        request.setEvent(DIAGNOSKODVERK_CHANGED);
        Map<String, String> extraInfo = new HashMap<>();
        extraInfo.put(INTYG_ID, intygId);
        request.setInfo(extraInfo);

        Response response = controller.monitoring(request);

        assertNotNull(response);
        assertEquals(OK.getStatusCode(), response.getStatus());

        verify(monLog).logDiagnoskodverkChanged(intygId);
        verifyNoMoreInteractions(monLog);
    }

}