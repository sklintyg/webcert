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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

@ExtendWith(MockitoExtension.class)
class PrintCertificateFromCertificateServiceTest {

    private static final String ID = "ID";

    private static final PrintCertificateRequestDTO REQUEST = PrintCertificateRequestDTO.builder().build();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    IntygPdf responseFromCS;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @InjectMocks
    PrintCertificateFromCertificateService printCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesntExistInCS() {
        final var response = printCertificateFromCertificateService.print(ID);

        assertNull(response);
    }

    @Test
    void shouldReturnResponseFromCSIfCertificateExistInCS() {
        when(csIntegrationRequestFactory.getPrintCertificateRequest(""))
            .thenReturn(REQUEST);
        when(csIntegrationService.certificateExists(ID))
            .thenReturn(true);
        when(csIntegrationService.printCertificate(ID, REQUEST))
            .thenReturn(responseFromCS);

        final var response = printCertificateFromCertificateService.print(ID);

        assertEquals(responseFromCS, response);
    }
}