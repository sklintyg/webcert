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

package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssMetadataService;

@RunWith(MockitoJUnitRunner.class)
public class SignatureApiControllerTest {

    @Mock
    private UnderskriftService mockUnderskriftService;

    @Mock
    private MonitoringLogService mockMonitoringService;

    @Mock
    private FakeSignatureServiceImpl mockFakeSignatureServiceImpl;

    @Mock
    private DssMetadataService dssMetadataService;

    @InjectMocks
    private SignatureApiController signatureApiController = new SignatureApiController();

    @Test
    public void signServiceResponse() {
        // TODO
    }

    @Test
    public void signServiceClientMetadata() {

        when(dssMetadataService.getClientMetadataAsString()).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Test>Inera AB</Test>");

        Response response = signatureApiController.signServiceClientMetadata();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("application/samlmetadata+xml", response.getMediaType().toString());
        assertEquals("attachment; filename=\"wc_dss_client_metadata.xml\"", response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));


    }
}