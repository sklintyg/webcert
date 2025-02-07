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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class GetCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final Certificate CERTIFICATE_FROM_CS = new Certificate();
    private static final Certificate CERTIFICATE_FROM_WC = new Certificate();

    GetCertificateFacadeService getCertificateFromWC;
    GetCertificateFacadeService getCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    GetCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        getCertificateFromWC = mock(GetCertificateFacadeService.class);
        getCertificateFromCS = mock(GetCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new GetCertificateAggregator(
            getCertificateFromWC,
            getCertificateFromCS,
            certificateServiceProfile
        );
    }

    @Test
    void shouldReturnCertificateFromCSIIfCSProfileIsActiveAndCertificateExistsInCS() {
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(getCertificateFromCS.getCertificate(ID, false, true))
            .thenReturn(CERTIFICATE_FROM_CS);

        final var response = aggregator.getCertificate(ID, false, true);
        verify(getCertificateFromCS, times(1)).getCertificate(ID, false, true);

        assertEquals(CERTIFICATE_FROM_CS, response);
    }

    @Test
    void shouldReturnCertificateFromWCIfCertificateDoesNotExistInCS() {
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(getCertificateFromWC.getCertificate(ID, true, false))
            .thenReturn(CERTIFICATE_FROM_WC);

        final var response = aggregator.getCertificate(ID, true, false);
        verify(getCertificateFromWC, times(1)).getCertificate(ID, true, false);

        assertEquals(CERTIFICATE_FROM_WC, response);
    }

    @Test
    void shouldReturnCertificateFromWCIfCSProfileIsNotActive() {
        when(getCertificateFromWC.getCertificate(ID, true, true))
            .thenReturn(CERTIFICATE_FROM_WC);

        final var response = aggregator.getCertificate(ID, true, true);
        verify(getCertificateFromWC, times(1)).getCertificate(ID, true, true);

        assertEquals(CERTIFICATE_FROM_WC, response);
    }
}
