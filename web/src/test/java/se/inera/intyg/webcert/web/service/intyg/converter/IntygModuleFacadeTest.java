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
package se.inera.intyg.webcert.web.service.intyg.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

@RunWith(MockitoJUnitRunner.class)
public class IntygModuleFacadeTest {

    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String CERTIFICATE_TYPE_VERSION_1_0 = "1.0";

    private static final String INT_JSON = "<ext-json>";
    private static final String HSVARD_RECIPIENT_ID = "HSVARD";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private IntygModuleFacadeImpl moduleFacade = new IntygModuleFacadeImpl();

    @Before
    public void setupCommonExpectations() throws Exception {
        // setup to return a mocked module API
        when(moduleRegistry.getModuleApi(or(isNull(), anyString()), or(isNull(), anyString()))).thenReturn(moduleApi);
        when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString(), or(isNull(), anyString())))
            .thenReturn(CERTIFICATE_TYPE_VERSION_1_0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertFromInternalToPdfDocument() throws IntygModuleFacadeException, ModuleException {
        byte[] pdfData = "PDFDATA".getBytes();
        PdfResponse pdfResp = new PdfResponse(pdfData, "file.pdf");
        when(moduleApi.pdf(anyString(), anyList(), any(ApplicationOrigin.class), eq(UtkastStatus.SIGNED))).thenReturn(pdfResp);

        IntygPdf intygPdf = moduleFacade.convertFromInternalToPdfDocument(CERTIFICATE_TYPE, INT_JSON,
            Arrays.asList(new Status(CertificateState.RECEIVED, "", LocalDateTime.now())), UtkastStatus.SIGNED, false);
        assertNotNull(intygPdf.getPdfData());
        assertEquals("file.pdf", intygPdf.getFilename());

        verify(moduleApi).pdf(anyString(), anyList(), eq(ApplicationOrigin.WEBCERT), eq(UtkastStatus.SIGNED));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IntygModuleFacadeException.class)
    public void testConvertFromInternalToPdfDocumentModuleException() throws IntygModuleFacadeException, ModuleException {
        when(moduleApi.pdf(anyString(), anyList(), any(ApplicationOrigin.class), eq(UtkastStatus.SIGNED)))
            .thenThrow(new ModuleException(""));

        moduleFacade.convertFromInternalToPdfDocument(CERTIFICATE_TYPE, INT_JSON,
            Arrays.asList(new Status(CertificateState.RECEIVED, "", LocalDateTime.now())), UtkastStatus.SIGNED, false);
    }

    @Test(expected = IntygModuleFacadeException.class)
    public void testConvertFromInternalToPdfDocumentModuleNotFoundException()
        throws IntygModuleFacadeException, ModuleException, ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0)).thenThrow(new ModuleNotFoundException());

        moduleFacade.convertFromInternalToPdfDocument(CERTIFICATE_TYPE, INT_JSON,
            Arrays.asList(new Status(CertificateState.RECEIVED, "", LocalDateTime.now())), UtkastStatus.SIGNED, false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertFromInternalToPdfDocumentEmployer() throws IntygModuleFacadeException, ModuleException {
        byte[] pdfData = "PDFDATA".getBytes();
        PdfResponse pdfResp = new PdfResponse(pdfData, "file.pdf");
        when(moduleApi.pdfEmployer(anyString(), anyList(), any(ApplicationOrigin.class), anyList(), eq(UtkastStatus.SIGNED)))
            .thenReturn(pdfResp);

        IntygPdf intygPdf = moduleFacade.convertFromInternalToPdfDocument(CERTIFICATE_TYPE, INT_JSON,
            Arrays.asList(new Status(CertificateState.RECEIVED, "", LocalDateTime.now())), UtkastStatus.SIGNED, true);
        assertNotNull(intygPdf.getPdfData());
        assertEquals("file.pdf", intygPdf.getFilename());

        verify(moduleApi).pdfEmployer(anyString(), anyList(), eq(ApplicationOrigin.WEBCERT), anyList(), eq(UtkastStatus.SIGNED));
    }

    @Test
    public void testGetCertificate() throws Exception {
        final String certificateId = "certificateId";
        final String logicalAddress = "logicalAddress";
        ReflectionTestUtils.setField(moduleFacade, "logicalAddress", logicalAddress);
        when(moduleApi.getCertificate(certificateId, logicalAddress, HSVARD_RECIPIENT_ID))
            .thenReturn(new CertificateResponse(INT_JSON, null, new CertificateMetaData(), false));
        CertificateResponse res = moduleFacade.getCertificate(certificateId, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);

        assertNotNull(res);

        verify(moduleApi).getCertificate(certificateId, logicalAddress, HSVARD_RECIPIENT_ID);
    }

    @Test(expected = IntygModuleFacadeException.class)
    public void testGetCertificateModuleException() throws Exception {
        when(moduleApi.getCertificate(anyString(), isNull(), eq(HSVARD_RECIPIENT_ID))).thenThrow(new ModuleException());
        moduleFacade.getCertificate("certificateId", CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
    }

    @Test(expected = IntygModuleFacadeException.class)
    public void testGetCertificateModuleNotFoundException() throws Exception {
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0)).thenThrow(new ModuleNotFoundException());
        moduleFacade.getCertificate("certificateId", CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
    }

    @Test
    public void testRegisterCertificate() throws Exception {
        final String logicalAddress = "logicalAddress";
        ReflectionTestUtils.setField(moduleFacade, "logicalAddress", logicalAddress);
        moduleFacade.registerCertificate(CERTIFICATE_TYPE, INT_JSON);

        verify(moduleApi).registerCertificate(INT_JSON, logicalAddress);
    }

    @Test(expected = ModuleException.class)
    public void testRegisterCertificateModuleException() throws Exception {
        doThrow(new ModuleException()).when(moduleApi).registerCertificate(any(), any());
        moduleFacade.registerCertificate(CERTIFICATE_TYPE, INT_JSON);
    }

    @Test(expected = IntygModuleFacadeException.class)
    public void testRegisterCertificateModuleNotFoundException() throws Exception {
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0)).thenThrow(new ModuleNotFoundException());
        moduleFacade.registerCertificate(CERTIFICATE_TYPE, INT_JSON);
    }

    @Test
    public void testGetRevokeCertificateRequest() throws Exception {
        final String message = "revokeMessage";
        Utlatande utlatande = mock(Utlatande.class);
        moduleFacade.getRevokeCertificateRequest(CERTIFICATE_TYPE, utlatande, null, message);
        verify(moduleApi, times(1)).createRevokeRequest(eq(utlatande), eq(null), eq(message));
    }

    @Test(expected = ModuleException.class)
    public void testGetRevokeCertificateRequestModuleException() throws Exception {
        Utlatande utlatande = mock(Utlatande.class);
        when(moduleApi.createRevokeRequest(eq(utlatande), isNull(), anyString())).thenThrow(new ModuleException());
        moduleFacade.getRevokeCertificateRequest(CERTIFICATE_TYPE, utlatande, null, "message");
    }

    @Test(expected = IntygModuleFacadeException.class)
    public void testGetRevokeCertificateRequestModuleNotFoundException() throws Exception {
        when(moduleRegistry.getModuleApi(eq(CERTIFICATE_TYPE), or(isNull(), anyString()))).thenThrow(new ModuleNotFoundException());
        moduleFacade.getRevokeCertificateRequest(CERTIFICATE_TYPE, mock(Utlatande.class), null, "message");
    }

    @Test
    public void testGetUtlatandeFromInternalModel() throws Exception {
        when(moduleApi.getUtlatandeFromJson(INT_JSON)).thenReturn(mock(Utlatande.class));
        Utlatande res = moduleFacade.getUtlatandeFromInternalModel(CERTIFICATE_TYPE, INT_JSON);

        assertNotNull(res);

        verify(moduleApi).getUtlatandeFromJson(INT_JSON);
    }

    @Test(expected = WebCertServiceException.class)
    public void testUtlatandBuiltFromInvalidJson() throws ModuleException, IOException {
        when(moduleApi.getUtlatandeFromJson(anyString())).thenThrow(new IOException());
        moduleFacade.getUtlatandeFromInternalModel(CERTIFICATE_TYPE, "X");
    }
}
