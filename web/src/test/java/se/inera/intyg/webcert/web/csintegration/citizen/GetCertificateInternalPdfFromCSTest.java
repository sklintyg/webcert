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

package se.inera.intyg.webcert.web.csintegration.citizen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateInternalPdfFromCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    private static final GetCitizenCertificatePdfRequestDTO GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO =
        GetCitizenCertificatePdfRequestDTO.builder()
            .build();
    private static final String CUSTOMIZATION_ID = "customizationId";
    private static final String FILE_NAME = "fileName";
    private static final byte[] PDF_DATA = "pdfData".getBytes(StandardCharsets.UTF_8);
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private GetGetCertificateInternalPdfFromCS getCertificateInternalPdfFromCS;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).citizenCertificateExists(CERTIFICATE_ID);
        assertNull(getCertificateInternalPdfFromCS.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID));
    }

    @Test
    void shallReturnGetCertificatePdfResponse() {
        final var responseFromCS = GetCitizenCertificatePdfResponseDTO.builder()
            .filename(FILE_NAME)
            .pdfData(PDF_DATA)
            .build();

        final var expectedResponse = CertificatePdfResponseDTO.create(
            FILE_NAME,
            PDF_DATA
        );

        doReturn(true).when(csIntegrationService).citizenCertificateExists(CERTIFICATE_ID);
        doReturn(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO).when(csIntegrationRequestFactory).getCitizenCertificatePdfRequest(PERSON_ID);
        doReturn(responseFromCS).when(csIntegrationService)
            .getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, CERTIFICATE_ID);

        final var actualResponse = getCertificateInternalPdfFromCS.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        assertEquals(expectedResponse, actualResponse);
    }
}
