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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.certificate.ReadyForSignForCertificateService;
import se.inera.intyg.webcert.web.service.facade.impl.ReadyForSignFacadeServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReadyForSignAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    @Mock
    private ReadyForSignFacadeServiceImpl readyForSignForWC;
    @Mock
    private ReadyForSignForCertificateService readyForSignForCS;
    private ReadyForSignAggregator readyForSignAggregator;

    @BeforeEach
    void setUp() {
        readyForSignAggregator = new ReadyForSignAggregator(readyForSignForWC, readyForSignForCS);
    }

    @Test
    void shallReturnResponseFromWCIfCertificateProfileIsInactive() {
        readyForSignAggregator.readyForSign(CERTIFICATE_ID);
        verify(readyForSignForWC, times(1)).readyForSign(CERTIFICATE_ID);
        verify(readyForSignForCS, times(0)).readyForSign(CERTIFICATE_ID);
    }

    @Test
    void shallReturnResponseFromWCIfResponseFromCSReturnsNull() {
        final var expectedCertificate = new Certificate();
        doReturn(null).when(readyForSignForCS).readyForSign(CERTIFICATE_ID);
        doReturn(expectedCertificate).when(readyForSignForWC).readyForSign(CERTIFICATE_ID);
        final var actualCertificate = readyForSignAggregator.readyForSign(CERTIFICATE_ID);

        verify(readyForSignForWC, times(1)).readyForSign(CERTIFICATE_ID);
        verify(readyForSignForCS, times(1)).readyForSign(CERTIFICATE_ID);
        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    void shallReturnResponseFromCS() {
        final var expectedCertificate = new Certificate();
        doReturn(expectedCertificate).when(readyForSignForCS).readyForSign(CERTIFICATE_ID);
        final var actualCertificate = readyForSignAggregator.readyForSign(CERTIFICATE_ID);

        verify(readyForSignForWC, times(0)).readyForSign(CERTIFICATE_ID);
        verify(readyForSignForCS, times(1)).readyForSign(CERTIFICATE_ID);
        assertEquals(expectedCertificate, actualCertificate);
    }
}
