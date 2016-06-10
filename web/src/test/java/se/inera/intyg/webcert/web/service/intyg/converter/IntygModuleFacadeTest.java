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

package se.inera.intyg.webcert.web.service.intyg.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

@RunWith(MockitoJUnitRunner.class)
public class IntygModuleFacadeTest {

    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String INT_JSON = "<ext-json>";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private IntygModuleFacadeImpl moduleFacade = new IntygModuleFacadeImpl();

    @Before
    public void setupCommonExpectations() throws Exception {
        // setup to return a mocked module API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);
    }

    @Test
    public void testConvertFromInternalToPdfDocument() throws IntygModuleFacadeException, ModuleException {

        byte[] pdfData = "PDFDATA".getBytes();
        PdfResponse pdfResp = new PdfResponse(pdfData, "file.pdf");
        when(moduleApi.pdf(anyString(), any(List.class), any(ApplicationOrigin.class))).thenReturn(pdfResp);

        IntygPdf intygPdf = moduleFacade.convertFromInternalToPdfDocument(CERTIFICATE_TYPE, INT_JSON, new ArrayList<Status>(), false);
        assertNotNull(intygPdf.getPdfData());
        assertEquals("file.pdf", intygPdf.getFilename());

        verify(moduleApi).pdf(anyString(), any(List.class), eq(ApplicationOrigin.WEBCERT));
    }

    @Test
    public void testGetRevokeCertificateRequest() throws Exception {
        final String message = "revokeMessage";
        moduleFacade.getRevokeCertificateRequest(CERTIFICATE_TYPE, null, null, message);
        verify(moduleApi, times(1)).createRevokeRequest(eq(null), eq(null), eq(message));
    }

    @Test(expected = WebCertServiceException.class)
    public void testUtlatandBuiltFromInvalidJson() throws IOException {
        when(moduleApi.getUtlatandeFromJson(anyString())).thenThrow(new IOException());
        moduleFacade.getUtlatandeFromInternalModel(CERTIFICATE_TYPE, "X");
    }
}
