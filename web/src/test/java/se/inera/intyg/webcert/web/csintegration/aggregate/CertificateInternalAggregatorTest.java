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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificateInteralApi;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@ExtendWith(MockitoExtension.class)
class CertificateInternalAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    @Mock
    private GetCertificateInteralApi cercertificateInternalServiceFromWC;
    @Mock
    private GetCertificateInteralApi certificateInternalServiceFromCS;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    private GetCertificateInternalAggregator certificateInternalAggregator;

    @BeforeEach
    void setUp() {
        certificateInternalAggregator = new GetCertificateInternalAggregator(
            certificateServiceProfile,
            cercertificateInternalServiceFromWC,
            certificateInternalServiceFromCS
        );
    }

    @Test
    void shouldReturnResponseFromCSIfProfileActiveAndResponseNotNull() {
        final var expectedResult = GetCertificateResponse.create(new Certificate());
        when(certificateServiceProfile.active()).thenReturn(true);
        when(certificateInternalServiceFromCS.get(CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = certificateInternalAggregator.get(CERTIFICATE_ID, PERSON_ID);
        verify(certificateInternalServiceFromCS, times(1)).get(CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnResponseFromWCIfProfileIsNotActive() {
        final var expectedResult = GetCertificateResponse.create(new Certificate());
        when(certificateServiceProfile.active()).thenReturn(false);
        when(cercertificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = certificateInternalAggregator.get(CERTIFICATE_ID, PERSON_ID);

        verify(cercertificateInternalServiceFromWC, times(1)).get(CERTIFICATE_ID, PERSON_ID);
        verify(certificateInternalServiceFromCS, times(0)).get(CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIIfCSProfileIsActiveButReturnsNull() {
        final var expectedResult = GetCertificateResponse.create(new Certificate());
        when(certificateServiceProfile.active()).thenReturn(true);
        when(cercertificateInternalServiceFromWC.get(CERTIFICATE_ID, PERSON_ID)).thenReturn(expectedResult);

        final var response = certificateInternalAggregator.get(CERTIFICATE_ID, PERSON_ID);

        verify(cercertificateInternalServiceFromWC, times(1)).get(CERTIFICATE_ID, PERSON_ID);
        verify(certificateInternalServiceFromCS, times(1)).get(CERTIFICATE_ID, PERSON_ID);

        assertEquals(expectedResult, response);
    }
}
