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
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;

@ExtendWith(MockitoExtension.class)
class CreateCertificateAggregatorTest {

    private static final String ORIGINAL_PATIENT_ID = "191212121212";
    private static final String TYPE = "TYPE";
    private static final String ID_FROM_CS = "ID_FROM_CS";
    private static final String ID_FROM_WC = "ID_FROM_WC";

    CreateCertificateFacadeService createCertificateFromWC;
    CreateCertificateFacadeService createCertificateFromCS;
    CreateCertificateFacadeService aggregator;

    @BeforeEach
    void setup() {
        createCertificateFromWC = mock(CreateCertificateFacadeService.class);
        createCertificateFromCS = mock(CreateCertificateFacadeService.class);

        aggregator = new CreateCertificateAggregator(
            createCertificateFromWC,
            createCertificateFromCS
        );
    }

    @Test
    void shouldReturnCertificateIdFromCSIfExists() throws CreateCertificateException {
        when(createCertificateFromCS.create(TYPE, ORIGINAL_PATIENT_ID))
            .thenReturn(ID_FROM_CS);

        final var response = aggregator.create(TYPE, ORIGINAL_PATIENT_ID);
        verify(createCertificateFromCS, times(1)).create(TYPE, ORIGINAL_PATIENT_ID);

        assertEquals(ID_FROM_CS, response);
    }

    @Test
    void shouldReturnCertificateIdFromWCIfCertificateDoesNotExistInCS() throws CreateCertificateException {
        when(createCertificateFromCS.create(TYPE, ORIGINAL_PATIENT_ID))
            .thenReturn(null);
        when(createCertificateFromWC.create(TYPE, ORIGINAL_PATIENT_ID))
            .thenReturn(ID_FROM_WC);

        final var response = aggregator.create(TYPE, ORIGINAL_PATIENT_ID);
        verify(createCertificateFromCS, times(1)).create(TYPE, ORIGINAL_PATIENT_ID);

        assertEquals(ID_FROM_WC, response);
    }
}
