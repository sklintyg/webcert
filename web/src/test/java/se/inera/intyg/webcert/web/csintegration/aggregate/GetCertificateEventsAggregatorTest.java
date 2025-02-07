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
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;

class GetCertificateEventsAggregatorTest {

    private static final String ID = "ID";
    private static final CertificateEventDTO[] EVENTS = {new CertificateEventDTO()};

    GetCertificateEventsFacadeService getCertificateEventsFromWC;
    GetCertificateEventsFacadeService getCertificateEventsFromCS;
    CertificateServiceProfile certificateServiceProfile;
    GetCertificateEventsFacadeService aggregator;

    @BeforeEach
    void setup() {
        getCertificateEventsFromWC = mock(GetCertificateEventsFacadeService.class);
        getCertificateEventsFromCS = mock(GetCertificateEventsFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);

        aggregator = new GetCertificateEventsAggregator(
            getCertificateEventsFromWC,
            getCertificateEventsFromCS,
            certificateServiceProfile);
    }

    @Test
    void shouldForwardFromWebcertIfProfileIsInactive() {
        aggregator.getCertificateEvents(ID);

        Mockito.verify(getCertificateEventsFromWC).getCertificateEvents(ID);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldForwardFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(getCertificateEventsFromCS.getCertificateEvents(ID))
                .thenReturn(EVENTS);
            aggregator.getCertificateEvents(ID);

            Mockito.verify(getCertificateEventsFromWC, times(0)).getCertificateEvents(ID);
        }

        @Test
        void shouldForwardFromWCIfProfileIsInactiveAndCertificateDoesNotExistInCS() {
            aggregator.getCertificateEvents(ID);

            Mockito.verify(getCertificateEventsFromWC, times(1)).getCertificateEvents(ID);
        }
    }
}
