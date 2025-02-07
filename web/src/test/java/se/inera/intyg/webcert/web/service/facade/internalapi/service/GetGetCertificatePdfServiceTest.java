/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.RequiredFieldsForCertificatePdf;

@ExtendWith(MockitoExtension.class)
class GetGetCertificatePdfServiceTest {

    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String WRONG_CUSTOMIZAITON_ID = "wrongCustomizaitonId";
    private static final String CUSTOMIZE_ID = "!diagnoser";
    private static final String INTERNAL_JSON_MODEL = "internalJsonModel";
    private static final RequiredFieldsForCertificatePdf REQUIRED_FIELDS_FOR_CERTIFICATE_PDF = RequiredFieldsForCertificatePdf.create(
        CERTIFICATE_TYPE_VERSION, CERTIFICATE_TYPE, INTERNAL_JSON_MODEL,
        Collections.emptyList(), UtkastStatus.SIGNED);
    private static final byte[] BYTES = new byte[0];
    private static final String FILE_NAME = "fileName";
    private static final PdfResponse PDF_RESPONSE = new PdfResponse(BYTES, FILE_NAME);
    private static final CertificatePdfResponseDTO EXPECTED_CERTIFICATE_PDF_RESPONSE = CertificatePdfResponseDTO.create(
        PDF_RESPONSE.getFilename(),
        PDF_RESPONSE.getPdfData()
    );

    @Mock
    private ModuleApi moduleApi;
    @Mock
    private GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
    @Mock
    private IntygModuleRegistryImpl moduleRegistry;
    @InjectMocks
    private GetGetCertificatePdfService getCertificatePdfService;

    @Test
    void shouldReturnPdfResponse() throws ModuleNotFoundException, ModuleException {
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
            .thenReturn(moduleApi);
        when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
            .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
        when(moduleApi.pdf(any(), any(), any(), any())).thenReturn(PDF_RESPONSE);
        final var result = getCertificatePdfService.get(null, CERTIFICATE_ID, null);
        assertEquals(EXPECTED_CERTIFICATE_PDF_RESPONSE, result);
    }

    @Nested
    class ModuleRegistryInteraction {

        private final ArgumentCaptor<String> moduleRegistryArgumentCaptor = ArgumentCaptor.forClass(String.class);

        @BeforeEach
        void setUp() throws ModuleException {
            when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
                .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
            when(moduleApi.pdf(any(), anyList(), any(), any())).thenReturn(PDF_RESPONSE);

        }

        @Test
        void shouldUseTypeVersionFromRequiredFields() throws ModuleNotFoundException {
            when(moduleRegistry.getModuleApi(moduleRegistryArgumentCaptor.capture(),
                eq(CERTIFICATE_TYPE_VERSION)))
                .thenReturn(moduleApi);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);

            assertEquals(CERTIFICATE_TYPE, moduleRegistryArgumentCaptor.getValue());
        }

        @Test
        void shouldUseTypeFromRequiredFields() throws ModuleNotFoundException {
            when(moduleRegistry.getModuleApi(eq(CERTIFICATE_TYPE),
                moduleRegistryArgumentCaptor.capture()))
                .thenReturn(moduleApi);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);

            assertEquals(CERTIFICATE_TYPE_VERSION, moduleRegistryArgumentCaptor.getValue());
        }
    }

    @Nested
    class GetRequiredFieldsInteraction {

        private final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        @Test
        void shouldUseProvidedCertificateId() throws ModuleNotFoundException, ModuleException {
            when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
                .thenReturn(moduleApi);
            when(getRequiredFieldsForCertificatePdfService.get(stringArgumentCaptor.capture()))
                .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
            when(moduleApi.pdf(any(), anyList(), any(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);

            assertEquals(CERTIFICATE_ID, stringArgumentCaptor.getValue());
        }
    }

    @Nested
    class ModuleApiPdfInteraction {

        @BeforeEach
        void setUp() throws ModuleNotFoundException {
            when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
                .thenReturn(moduleApi);
            when(getRequiredFieldsForCertificatePdfService.get(CERTIFICATE_ID))
                .thenReturn(REQUIRED_FIELDS_FOR_CERTIFICATE_PDF);
        }


        @Test
        void shouldUseJsonModelFromRequiredFields() throws ModuleException {
            final var stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            when(moduleApi.pdf(stringArgumentCaptor.capture(), any(), any(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);
            assertEquals(INTERNAL_JSON_MODEL, stringArgumentCaptor.getValue());
        }

        @Test
        void shouldUseStatusesFromRequiredFields() throws ModuleException {
            final var listArgumentCaptor = ArgumentCaptor.forClass(List.class);
            when(moduleApi.pdf(any(), listArgumentCaptor.capture(), any(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);
            assertNotNull(listArgumentCaptor.getValue());
        }

        @Test
        void shouldUseStatusFromRequiredFields() throws ModuleException {
            final var statusArgumentCaptor = ArgumentCaptor.forClass(UtkastStatus.class);
            when(moduleApi.pdf(any(), anyList(), any(), statusArgumentCaptor.capture())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);
            assertEquals(UtkastStatus.SIGNED, statusArgumentCaptor.getValue());
        }


        @Test
        void shouldUseOriginMinaIntyg() throws ModuleException {
            final var originArgumentCaptor = ArgumentCaptor.forClass(ApplicationOrigin.class);
            when(moduleApi.pdf(any(), anyList(), originArgumentCaptor.capture(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);
            assertEquals(ApplicationOrigin.MINA_INTYG, originArgumentCaptor.getValue());
        }


        @Test
        void shouldCallPdfEmployerIfCorrectCustomizeIdWasProvidedFromRequest() throws ModuleException {
            when(moduleApi.pdfEmployer(any(), anyList(), any(), anyList(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(CUSTOMIZE_ID, CERTIFICATE_ID, null);
            verify(moduleApi).pdfEmployer(any(), anyList(), any(), anyList(), any());
        }

        @Test
        void shouldCallPdfIfWrongCustomizeIdWasProvidedFromRequest() throws ModuleException {
            when(moduleApi.pdf(any(), anyList(), any(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(WRONG_CUSTOMIZAITON_ID, CERTIFICATE_ID, null);
            verify(moduleApi).pdf(any(), anyList(), any(), any());
        }

        @Test
        void shouldCallPdfIfNoCustomizeIdWasProvidedFromRequest() throws ModuleException {
            when(moduleApi.pdf(any(), anyList(), any(), any())).thenReturn(PDF_RESPONSE);
            getCertificatePdfService.get(null, CERTIFICATE_ID, null);
            verify(moduleApi).pdf(any(), anyList(), any(), any());
        }
    }
}
