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

package se.inera.intyg.webcert.web.csintegration.typeinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.certificatetype.GetCertificateTypeInfoFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateTypeInfoFromCertificateServiceTest {

    private static final CertificateServiceTypeInfoRequestDTO REQUEST = CertificateServiceTypeInfoRequestDTO.builder().build();
    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("191212121212").get();
    private static final List<CertificateTypeInfoDTO> TYPES = List.of(new CertificateTypeInfoDTO());
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private GetCertificateTypeInfoFromCertificateService getCertificateTypeInfoFromCertificateService;

    @Test
    void shouldReturnListOfTypesFromCSInternalApi() {
        doReturn(REQUEST).when(csIntegrationRequestFactory).getCertificateTypesRequest(PATIENT_ID);
        doReturn(TYPES).when(csIntegrationService).getTypeInfo(REQUEST);
        assertEquals(TYPES,
            getCertificateTypeInfoFromCertificateService.get(PATIENT_ID)
        );
    }
}
