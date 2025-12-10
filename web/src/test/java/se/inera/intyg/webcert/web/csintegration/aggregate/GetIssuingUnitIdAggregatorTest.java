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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.certificate.GetIssuingUnitIdFromCertificateService;
import se.inera.intyg.webcert.web.service.certificate.GetIssuingUnitIdFromWebcert;

@ExtendWith(MockitoExtension.class)
class GetIssuingUnitIdAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String UNIT_ID = "unitId";
    @Mock
    GetIssuingUnitIdFromWebcert getUnitIdFromWC;
    @Mock
    GetIssuingUnitIdFromCertificateService getUnitIdFromCS;
    @InjectMocks
    GetIssuingUnitIdAggregator getIssuingUnitIdAggregator;

    @Test
    void shallReturnResponseFromWCIfResponseFromCSIsNull() {
        doReturn(UNIT_ID).when(getUnitIdFromWC).get(CERTIFICATE_ID);
        doReturn(null).when(getUnitIdFromCS).get(CERTIFICATE_ID);
        final var actualUnitId = getIssuingUnitIdAggregator.get(CERTIFICATE_ID);

        verify(getUnitIdFromCS, times(1)).get(CERTIFICATE_ID);
        verify(getUnitIdFromWC, times(1)).get(CERTIFICATE_ID);
        assertEquals(UNIT_ID, actualUnitId);
    }

    @Test
    void shallReturnResponseFromCS() {
        doReturn(UNIT_ID).when(getUnitIdFromCS).get(CERTIFICATE_ID);
        final var actualUnitId = getIssuingUnitIdAggregator.get(CERTIFICATE_ID);

        verify(getUnitIdFromCS, times(1)).get(CERTIFICATE_ID);
        assertEquals(UNIT_ID, actualUnitId);
    }
}
