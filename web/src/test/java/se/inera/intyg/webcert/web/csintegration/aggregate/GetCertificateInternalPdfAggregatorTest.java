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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificatePdfService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateInternalPdfAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    private static final String CUSTOMIZATION_ID = "customizationId";
    private static final String FILE_NAME = "fileName";
    private static final byte[] BYTES = "byte".getBytes(StandardCharsets.UTF_8);
    @Mock
    private GetCertificatePdfService getCertificatePdfServiceFromWC;
    @Mock
    private GetCertificatePdfService getCertificatePdfServiceFromCS;
    private GetGetCertificateInternalPdfAggregator getCertificateInternalPdfAggregator;

    @BeforeEach
    void setUp() {
        getCertificateInternalPdfAggregator = new GetGetCertificateInternalPdfAggregator(
            getCertificatePdfServiceFromWC,
            getCertificatePdfServiceFromCS
        );
    }

    @Test
    void shouldReturnResponseFromCSIfResponseNotNull() {
        final var expectedResult = CertificatePdfResponseDTO.create(FILE_NAME, BYTES);
        when(getCertificatePdfServiceFromCS.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = getCertificateInternalPdfAggregator.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        verify(getCertificatePdfServiceFromCS, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIIfReturnsNull() {
        final var expectedResult = CertificatePdfResponseDTO.create(FILE_NAME, BYTES);
        when(getCertificatePdfServiceFromWC.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = getCertificateInternalPdfAggregator.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        verify(getCertificatePdfServiceFromWC, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        verify(getCertificatePdfServiceFromCS, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }
}
