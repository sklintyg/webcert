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
import org.mockito.Mockito;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;

class RenewCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";

    RenewCertificateFacadeService renewCertificateFromWC;
    RenewCertificateFacadeService renewCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    RenewCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        renewCertificateFromWC = mock(RenewCertificateFacadeService.class);
        renewCertificateFromCS = mock(RenewCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new RenewCertificateAggregator(
            renewCertificateFromWC,
            renewCertificateFromCS,
            certificateServiceProfile);
    }

    @Test
    void shouldRenewFromWebcertIfProfileIsInactive() {
        aggregator.renewCertificate(ID);

        Mockito.verify(renewCertificateFromWC).renewCertificate(ID);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldRenewFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(renewCertificateFromCS.renewCertificate(ID))
                .thenReturn(NEW_ID);
            aggregator.renewCertificate(ID);

            Mockito.verify(renewCertificateFromWC, times(0)).renewCertificate(ID);
        }

        @Test
        void shouldRenewFromWCIfProfileIsInactiveAndCertificateDoesNotExistInCS() {
            aggregator.renewCertificate(ID);

            Mockito.verify(renewCertificateFromWC, times(1)).renewCertificate(ID);
        }
    }
}
