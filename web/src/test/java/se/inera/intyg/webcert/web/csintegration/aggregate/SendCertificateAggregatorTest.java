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
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class SendCertificateAggregatorTest {

    private static final String ID = "ID";

    SendCertificateFacadeService sendCertificateFromWC;
    SendCertificateFacadeService sendCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    SendCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        sendCertificateFromWC = mock(SendCertificateFacadeService.class);
        sendCertificateFromCS = mock(SendCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new SendCertificateAggregator(
            sendCertificateFromWC,
            sendCertificateFromCS,
            certificateServiceProfile);
    }

    @Test
    void shouldSendFromWebcertIfProfileIsInactive() {
        aggregator.sendCertificate(ID);

        Mockito.verify(sendCertificateFromWC).sendCertificate(ID);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldSendFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(sendCertificateFromCS.sendCertificate(ID))
                .thenReturn(ID);
            aggregator.sendCertificate(ID);

            Mockito.verify(sendCertificateFromWC, times(0)).sendCertificate(ID);
        }

        @Test
        void shouldSendFromWCIfProfileIsInactiveAndCertificateDoesNotExistInCS() {
            aggregator.sendCertificate(ID);

            Mockito.verify(sendCertificateFromWC, times(1)).sendCertificate(ID);
        }
    }
}
