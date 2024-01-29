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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.web.csintegration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class CSIntegrationServiceTest {

    private static final CertificateServiceTypeInfoRequestDTO REQUEST = new CertificateServiceTypeInfoRequestDTO();
    private static final CertificateTypeInfoDTO convertedTypeInfo = new CertificateTypeInfoDTO();
    private static final CertificateServiceTypeInfoDTO typeInfo = new CertificateServiceTypeInfoDTO();
    private static final List<CertificateServiceTypeInfoDTO> typeInfos = List.of(typeInfo);
    private static final CertificateServiceTypeInfoResponseDTO RESPONSE = new CertificateServiceTypeInfoResponseDTO(typeInfos);


    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @InjectMocks
    private CSIntegrationService csIntegrationService;

    @BeforeEach
    void setUp() {
        when(certificateTypeInfoConverter.convert(any()))
            .thenReturn(convertedTypeInfo);
        when(restTemplate.postForObject(anyString(), any(), any()))
            .thenReturn(RESPONSE);
    }

    @Test
    void shouldPreformPostUsingRequest() {
        final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
        csIntegrationService.getTypeInfo(REQUEST);
        verify(restTemplate).postForObject(anyString(), captor.capture(), any());
        assertEquals(REQUEST, captor.getValue());
    }

    @Test
    void shouldReturnConvertedObject() {
        final var response = csIntegrationService.getTypeInfo(REQUEST);
        assertTrue(response.contains(convertedTypeInfo));
    }
}