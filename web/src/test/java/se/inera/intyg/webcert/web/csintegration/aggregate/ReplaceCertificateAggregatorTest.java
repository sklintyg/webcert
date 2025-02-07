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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class ReplaceCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";

    ReplaceCertificateFacadeService replaceCertificateFromWC;
    ReplaceCertificateFacadeService replaceCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    ReplaceCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        replaceCertificateFromWC = mock(ReplaceCertificateFacadeService.class);
        replaceCertificateFromCS = mock(ReplaceCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new ReplaceCertificateAggregator(
            replaceCertificateFromWC,
            replaceCertificateFromCS,
            certificateServiceProfile);
    }

    @Test
    void shouldReplaceFromWebcertIfProfileIsInactive() {
        aggregator.replaceCertificate(ID);

        Mockito.verify(replaceCertificateFromWC).replaceCertificate(ID);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldReplaceFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(replaceCertificateFromCS.replaceCertificate(ID))
                .thenReturn(NEW_ID);
            aggregator.replaceCertificate(ID);

            Mockito.verify(replaceCertificateFromWC, times(0)).replaceCertificate(ID);
        }

        @Test
        void shouldReplaceFromWCIfProfileIsInactiveAndCertificateDoesNotExistInCS() {
            aggregator.replaceCertificate(ID);

            Mockito.verify(replaceCertificateFromWC, times(1)).replaceCertificate(ID);
        }
    }
}
