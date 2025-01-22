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

package se.inera.intyg.webcert.web.csintegration.citizen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@ExtendWith(MockitoExtension.class)
class CertificateInternalServiceFromCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PERSON_ID = "personId";
    private static final GetCitizenCertificateRequestDTO GET_CITIZEN_CERTIFICATE_REQUEST_DTO = GetCitizenCertificateRequestDTO.builder()
        .build();
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private GetCertificateInternalServiceFromCS certificateInternalServiceFromCS;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).citizenCertificateExists(CERTIFICATE_ID);
        assertNull(certificateInternalServiceFromCS.get(CERTIFICATE_ID, PERSON_ID));
    }

    @Test
    void shallReturnGetCertificateResponse() {
        final var certificate = new Certificate();
        final var availableFunctions = List.of(AvailableFunctionDTO.create(AvailableFunctionTypeDTO.ATTENTION, "name", true));
        final var certificateTexts = List.of(CertificateText.builder().build());

        final var responseFromCS = GetCitizenCertificateResponseDTO.builder()
            .certificate(certificate)
            .availableFunctions(availableFunctions)
            .texts(certificateTexts)
            .build();

        final var expectedResponse = GetCertificateResponse.create(
            certificate,
            availableFunctions,
            certificateTexts
        );

        doReturn(true).when(csIntegrationService).citizenCertificateExists(CERTIFICATE_ID);
        doReturn(GET_CITIZEN_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory).getCitizenCertificateRequest(PERSON_ID);
        doReturn(responseFromCS).when(csIntegrationService).getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID);

        final var actualResponse = certificateInternalServiceFromCS.get(CERTIFICATE_ID, PERSON_ID);
        assertEquals(expectedResponse, actualResponse);
    }
}
