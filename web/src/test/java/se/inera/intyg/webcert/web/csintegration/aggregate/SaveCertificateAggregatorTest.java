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
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class SaveCertificateAggregatorTest {

    @Mock
    SaveCertificateFacadeService saveCertificateFacadeServiceInWC;
    @Mock
    SaveCertificateFacadeService saveCertificateFacadeServiceInCS;
    SaveCertificateAggregator aggregator;

    private static final Certificate CERTIFICATE = new Certificate();
    private static final boolean PDL_LOG = false;
    private static final long VERSION_FROM_WC = 1;
    private static final long VERSION_FROM_CS = 99;

    @BeforeEach
    void setup() {
        aggregator = new SaveCertificateAggregator(
            saveCertificateFacadeServiceInWC,
            saveCertificateFacadeServiceInCS
        );
    }

    @Test
    void shallSaveCertificateInWebcertIfCertificateServiceIsNotActive() {
        doReturn(VERSION_FROM_WC).when(saveCertificateFacadeServiceInWC).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_WC,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }

    @Test
    void shallSaveCertificateInCSIfCertificateServiceIsActive() {
        doReturn(VERSION_FROM_CS).when(saveCertificateFacadeServiceInCS).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_CS,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }

    @Test
    void shallSaveCertificateInWCIfCertificateServiceIsActiveButCSReturnsNegativeVersion() {
        doReturn(-1L).when(saveCertificateFacadeServiceInCS).saveCertificate(CERTIFICATE, PDL_LOG);
        doReturn(VERSION_FROM_WC).when(saveCertificateFacadeServiceInWC).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_WC,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }
}
