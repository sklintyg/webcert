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

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.web.service.intyginfo.IntygInfoServiceInterface;

@ExtendWith(MockitoExtension.class)
class IntygInfoAggregatorTest {

    private static final String ID = "ID";
    private static final WcIntygInfo CERTIFICATE_FROM_CS = new WcIntygInfo();
    private static final WcIntygInfo CERTIFICATE_FROM_WC = new WcIntygInfo();

    IntygInfoServiceInterface getCertificateFromWC;
    IntygInfoServiceInterface getCertificateFromCS;
    IntygInfoServiceInterface aggregator;

    @BeforeEach
    void setup() {
        getCertificateFromWC = mock(IntygInfoServiceInterface.class);
        getCertificateFromCS = mock(IntygInfoServiceInterface.class);

        aggregator = new IntygInfoAggregator(
            getCertificateFromWC,
            getCertificateFromCS
        );
    }

    @Test
    void shouldReturnCertificateFromCSIIfCertificateExistsInCS() {
        when(getCertificateFromCS.getIntygInfo(ID))
            .thenReturn(Optional.of(CERTIFICATE_FROM_CS));

        final var response = aggregator.getIntygInfo(ID);
        verify(getCertificateFromCS, times(1)).getIntygInfo(ID);

        assertEquals(CERTIFICATE_FROM_CS, response.get());
    }

    @Test
    void shouldReturnCertificateFromWCIfCertificateDoesNotExistInCS() {
        when(getCertificateFromWC.getIntygInfo(ID))
            .thenReturn(Optional.of(CERTIFICATE_FROM_WC));

        final var response = aggregator.getIntygInfo(ID);
        verify(getCertificateFromWC, times(1)).getIntygInfo(ID);

        assertEquals(CERTIFICATE_FROM_WC, response.get());
    }
}