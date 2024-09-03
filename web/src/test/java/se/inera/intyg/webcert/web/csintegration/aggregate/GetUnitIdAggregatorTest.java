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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.certificate.GetUnitIdFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.certificate.GetUnitIdFromWebcert;

@ExtendWith(MockitoExtension.class)
class GetUnitIdAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String UNIT_ID = "unitId";
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @Mock
    GetUnitIdFromWebcert getUnitIdFromWC;
    @Mock
    GetUnitIdFromCertificateService getUnitIdFromCS;
    @InjectMocks
    GetUnitIdAggregator getUnitIdAggregator;

    @Test
    void shallReturnResponseFromWCIfCertificateServiceProfileIsInactive() {
        doReturn(false).when(certificateServiceProfile).active();
        getUnitIdAggregator.get(CERTIFICATE_ID);

        verify(getUnitIdFromWC, times(1)).get(CERTIFICATE_ID);
        verifyNoInteractions(getUnitIdFromCS);
    }

    @Test
    void shallReturnResponseFromWCIfResponseFromCSIsNull() {
        doReturn(UNIT_ID).when(getUnitIdFromWC).get(CERTIFICATE_ID);
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(null).when(getUnitIdFromCS).get(CERTIFICATE_ID);
        final var actualUnitId = getUnitIdAggregator.get(CERTIFICATE_ID);

        verify(getUnitIdFromCS, times(1)).get(CERTIFICATE_ID);
        verify(getUnitIdFromWC, times(1)).get(CERTIFICATE_ID);
        assertEquals(UNIT_ID, actualUnitId);
    }

    @Test
    void shallReturnResponseFromCS() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(UNIT_ID).when(getUnitIdFromCS).get(CERTIFICATE_ID);
        final var actualUnitId = getUnitIdAggregator.get(CERTIFICATE_ID);

        verify(getUnitIdFromCS, times(1)).get(CERTIFICATE_ID);
        assertEquals(UNIT_ID, actualUnitId);
    }
}
