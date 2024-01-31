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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class CertificateTypeInfoAggregatorTest {

    private static final CertificateTypeInfoDTO infoFromCS = new CertificateTypeInfoDTO();
    private static final CertificateTypeInfoDTO infoFromWC = new CertificateTypeInfoDTO();
    private static final String ORIGINAL_PATIENT_ID = "191212121212";
    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer(ORIGINAL_PATIENT_ID).get();

    GetCertificateTypesFacadeService getCertificateTypeInfoFromWebcert;
    GetCertificateTypesFacadeService getCertificateTypeInfoFromCertificateService;
    Environment environment;
    CertificateTypeInfoAggregator certificateTypeInfoAggregator;

    @BeforeEach
    void setup() {
        getCertificateTypeInfoFromCertificateService = mock(GetCertificateTypesFacadeService.class);
        getCertificateTypeInfoFromWebcert = mock(GetCertificateTypesFacadeService.class);
        environment = mock(Environment.class);

        when(getCertificateTypeInfoFromWebcert.get(PATIENT_ID))
            .thenReturn(List.of(infoFromWC));

        certificateTypeInfoAggregator = new CertificateTypeInfoAggregator(
            getCertificateTypeInfoFromWebcert,
            getCertificateTypeInfoFromCertificateService,
            environment
        );
    }

    @Test
    void shouldMergeCertificateTypesLists() {
        when(getCertificateTypeInfoFromCertificateService.get(PATIENT_ID))
            .thenReturn(List.of(infoFromCS));
        when(environment.matchesProfiles("certificate-service-active"))
            .thenReturn(true);

        final var response = certificateTypeInfoAggregator.get(PATIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.contains(infoFromCS));
        assertTrue(response.contains(infoFromWC));
    }

    @Test
    void shouldNotMergeCertificateTypesIfProfileNotActive() {
        final var response = certificateTypeInfoAggregator.get(PATIENT_ID);

        assertEquals(1, response.size());
        assertFalse(response.contains(infoFromCS));
        assertTrue(response.contains(infoFromWC));
    }
}