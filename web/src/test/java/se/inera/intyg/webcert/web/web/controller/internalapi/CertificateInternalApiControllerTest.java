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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetAvailableFunctionsForCertificateService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetCertificatePdfService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@ExtendWith(MockitoExtension.class)
class CertificateInternalApiControllerTest {

    private static final Certificate EXPECTED_CERTIFICATE = new Certificate();
    private static final List<AvailableFunctionDTO> EXPECTED_AVAILABLE_FUNCTIONS = List.of(
        AvailableFunctionDTO.create(AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE, null,
            null, null)
    );
    private static final List<CertificateText> EXPECTED_TEXTS = List.of(
        CertificateText.builder().build()
    );
    private static final byte[] EXPECTED_PDF_DATA = new byte[0];
    private static final String EXPECTED_FILENAME = "filename";
    private static final CertificatePdfResponseDTO EXPECTED_PDL_RESPONSE = CertificatePdfResponseDTO.create(
        EXPECTED_FILENAME,
        EXPECTED_PDF_DATA
    );
    private static final String CERTIFICATE_ID = "certificateId";
    private static final boolean SHOULD_NOT_PDL_LOG = false;
    private static final boolean SHOULD_NOT_VALIDATE_ACCESS = false;
    @Mock
    private GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService;

    @Mock
    private GetCertificatePdfService getCertificatePdfService;
    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private CertificateInternalApiController certificateInternalApiController;

    @Nested
    class GetCertificate {

        @BeforeEach
        void setUp() {
            doReturn(EXPECTED_CERTIFICATE)
                .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, SHOULD_NOT_PDL_LOG, SHOULD_NOT_VALIDATE_ACCESS);
            doReturn(EXPECTED_AVAILABLE_FUNCTIONS)
                .when(getAvailableFunctionsForCertificateService).get(EXPECTED_CERTIFICATE);
        }

        @Test
        void shallReturnCertificate() {
            final var actualCertificateResponse = certificateInternalApiController.getCertificate(CERTIFICATE_ID);
            assertEquals(EXPECTED_CERTIFICATE, actualCertificateResponse.getCertificate());
        }

        @Test
        void shallReturnAvailableFunctions() {
            final var actualCertificateResponse = certificateInternalApiController.getCertificate(CERTIFICATE_ID);
            assertEquals(EXPECTED_AVAILABLE_FUNCTIONS, actualCertificateResponse.getAvailableFunctions());
        }

        @Test
        void shallReturnCertificateTexts() {
            final var actualCertificateResponse = certificateInternalApiController.getCertificate(CERTIFICATE_ID);
            assertEquals(EXPECTED_TEXTS, actualCertificateResponse.getTexts());
        }

        @Test
        void shallNotPdlLogWhenRetrievingCertificate() {
            final var booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
            certificateInternalApiController.getCertificate(CERTIFICATE_ID);
            verify(getCertificateFacadeService).getCertificate(
                anyString(),
                booleanArgumentCaptor.capture(),
                anyBoolean()
            );
            assertEquals(SHOULD_NOT_PDL_LOG, booleanArgumentCaptor.getValue());
        }

        @Test
        void shallNotValidateAccessWhenRetrievingCertificate() {
            final var booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
            certificateInternalApiController.getCertificate(CERTIFICATE_ID);
            verify(getCertificateFacadeService).getCertificate(
                anyString(),
                anyBoolean(),
                booleanArgumentCaptor.capture()
            );
            assertEquals(SHOULD_NOT_VALIDATE_ACCESS, booleanArgumentCaptor.getValue());
        }

    }

    @Nested
    class GetPdfData {

        private final CertificatePdfRequestDTO printCertificateRequest = new CertificatePdfRequestDTO();

        @BeforeEach
        void setUp() {
            doReturn(EXPECTED_PDL_RESPONSE)
                .when(getCertificatePdfService)
                .get(printCertificateRequest.getCustomizationId(), CERTIFICATE_ID);
        }

        @Test
        void shallReturnPrintCertificateResponseWithFileName() {
            final var actualPrintCertificateResponse = certificateInternalApiController.getPdfData(printCertificateRequest, CERTIFICATE_ID);
            assertEquals(EXPECTED_FILENAME, actualPrintCertificateResponse.getFilename());
        }

        @Test
        void shallReturnPrintCertificateResponseWithPdfData() {
            final var actualPrintCertificateResponse = certificateInternalApiController.getPdfData(printCertificateRequest, CERTIFICATE_ID);
            assertEquals(EXPECTED_PDF_DATA, actualPrintCertificateResponse.getPdfData());
        }
    }
}
