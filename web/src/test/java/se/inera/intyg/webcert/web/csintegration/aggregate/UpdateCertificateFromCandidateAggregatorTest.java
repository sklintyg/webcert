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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.UpdateCertificateFromCandidateFacadeService;

@ExtendWith(MockitoExtension.class)
class UpdateCertificateFromCandidateAggregatorTest {

    private static final String ID = "ID";

    UpdateCertificateFromCandidateFacadeService updateCertificateFromCandidateFromWC;
    UpdateCertificateFromCandidateFacadeService updateCertificateFromCandidateFromCS;
    UpdateCertificateFromCandidateAggregator aggregator;

    @BeforeEach
    void setup() {
        updateCertificateFromCandidateFromWC = mock(UpdateCertificateFromCandidateFacadeService.class);
        updateCertificateFromCandidateFromCS = mock(UpdateCertificateFromCandidateFacadeService.class);

        aggregator = new UpdateCertificateFromCandidateAggregator(
            updateCertificateFromCandidateFromWC,
            updateCertificateFromCandidateFromCS
        );
    }

    @Test
    void shouldSendFromCSIfCertificateExistsInCS() {
        when(updateCertificateFromCandidateFromCS.update(ID)).thenReturn(ID);
        aggregator.update(ID);

        Mockito.verify(updateCertificateFromCandidateFromWC, times(0)).update(ID);
    }

    @Test
    void shouldSendFromWCIfCertificateDoesNotExistInCS() {
        aggregator.update(ID);

        Mockito.verify(updateCertificateFromCandidateFromWC, times(1)).update(ID);
    }
}
