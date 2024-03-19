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
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.certificate.PrintCertificateFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

@ExtendWith(MockitoExtension.class)
class PrintCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final boolean IS_EMPLOYER = true;

    @Mock
    IntygService intygService;

    @Mock
    PrintCertificateFromCertificateService printCertificateFromCertificateService;

    @Mock
    CertificateServiceProfile certificateServiceProfile;

    @Mock
    IntygPdf responseFromWC;

    @Mock
    IntygPdf responseFromCS;

    @InjectMocks
    PrintCertificateAggregator printCertificateAggregator;

    @Test
    void shouldReturnResponseFromIntygServiceIfProfileIsNotActive() {
        when(intygService.fetchIntygAsPdf(ID, TYPE, IS_EMPLOYER))
            .thenReturn(responseFromWC);

        final var response = printCertificateAggregator.get(ID, TYPE, IS_EMPLOYER);

        assertEquals(responseFromWC, response);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldReturnResponseFromIntygServiceIfCertificateDoesNotExistInCS() {
            when(intygService.fetchIntygAsPdf(ID, TYPE, IS_EMPLOYER))
                .thenReturn(responseFromWC);

            final var response = printCertificateAggregator.get(ID, TYPE, IS_EMPLOYER);

            assertEquals(responseFromWC, response);
        }

        @Test
        void shouldReturnResponseFromCSIfCertificateDoesNotExistInCS() {
            when(printCertificateFromCertificateService.print(ID, TYPE, IS_EMPLOYER))
                .thenReturn(responseFromCS);

            final var response = printCertificateAggregator.get(ID, TYPE, IS_EMPLOYER);

            assertEquals(responseFromCS, response);
        }
    }

}