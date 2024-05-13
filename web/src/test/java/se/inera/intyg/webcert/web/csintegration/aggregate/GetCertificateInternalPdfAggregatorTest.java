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
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.web.controller.internalapi.CertificatePdfService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateInternalPdfAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    private static final String CUSTOMIZATION_ID = "customizationId";
    private static final String FILE_NAME = "fileName";
    private static final byte[] BYTES = "byte".getBytes(StandardCharsets.UTF_8);
    @Mock
    private CertificatePdfService certificatePdfServiceFromWC;
    @Mock
    private CertificatePdfService certificatePdfServiceFromCS;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    private GetCertificateInternalPdfAggregator getCertificateInternalPdfAggregator;

    @BeforeEach
    void setUp() {
        getCertificateInternalPdfAggregator = new GetCertificateInternalPdfAggregator(
            certificateServiceProfile,
            certificatePdfServiceFromWC,
            certificatePdfServiceFromCS
        );
    }

    @Test
    void shouldReturnResponseFromCSIfProfileActiveAndResponseNotNull() {
        final var expectedResult = CertificatePdfResponseDTO.create(FILE_NAME, BYTES);
        when(certificateServiceProfile.active()).thenReturn(true);
        when(certificatePdfServiceFromCS.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = getCertificateInternalPdfAggregator.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        verify(certificatePdfServiceFromCS, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnResponseFromWCIfProfileIsNotActive() {
        final var expectedResult = CertificatePdfResponseDTO.create(FILE_NAME, BYTES);
        when(certificateServiceProfile.active()).thenReturn(false);
        when(certificatePdfServiceFromWC.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = getCertificateInternalPdfAggregator.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        verify(certificatePdfServiceFromWC, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        verify(certificatePdfServiceFromCS, times(0)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIIfCSProfileIsActiveButReturnsNull() {
        final var expectedResult = CertificatePdfResponseDTO.create(FILE_NAME, BYTES);
        when(certificateServiceProfile.active()).thenReturn(true);
        when(certificatePdfServiceFromWC.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = getCertificateInternalPdfAggregator.get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        verify(certificatePdfServiceFromWC, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);
        verify(certificatePdfServiceFromCS, times(1)).get(CUSTOMIZATION_ID, CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }
}