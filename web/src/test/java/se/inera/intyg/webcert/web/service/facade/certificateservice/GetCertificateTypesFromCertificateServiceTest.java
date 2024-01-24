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

package se.inera.intyg.webcert.web.service.facade.certificateservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.integration.certificateservice.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.certificateservice.dto.CertificateServiceTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateTypesFromCertificateServiceTest {

    private static final String PATIENT_ID = "PATIENT_ID";
    private static final CertificateServiceTypeInfoDTO type1 = new CertificateServiceTypeInfoDTO();

    @Mock
    CSIntegrationService csIntegrationService;

    @InjectMocks
    private GetCertificateTypesFromCertificateService getCertificateTypesFromCertificateService;

    @BeforeEach
    void setup() {
        when(csIntegrationService.getTypeInfo(any()))
            .thenReturn(type1);
    }

    @Test
    void shouldReturnResponseFromInternalApi() {
        final var response = getCertificateTypesFromCertificateService.get(PATIENT_ID);

        assertEquals(1, response.size());
        assertTrue(response.contains(type1));
    }

}