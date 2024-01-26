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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.certificate.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class CSIntegrationServiceTest {

    private static final CertificateServiceTypeInfoRequestDTO REQUEST = new CertificateServiceTypeInfoRequestDTO();
    private static final CertificateTypeInfoDTO convertedTypeInfo = new CertificateTypeInfoDTO();
    private static final CertificateServiceTypeInfoDTO typeInfo = new CertificateServiceTypeInfoDTO();
    private static final CertificateServiceTypeInfoDTO[] typeInfos = {typeInfo};
    private static final Certificate certificate = new Certificate();
    private static final CreateCertificateRequestDTO CREATE_CERTIFICATE_REQUEST = new CreateCertificateRequestDTO();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @InjectMocks
    private CSIntegrationService csIntegrationService;

    @Nested
    class TypeInfo {

        @BeforeEach
        void setUp() {
            when(certificateTypeInfoConverter.convert(any()))
                .thenReturn(convertedTypeInfo);
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(typeInfos);
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

    @Nested
    class CreateCertificate {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(certificate);
            final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

            csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(CREATE_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(certificate);
            final var response = csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);

            assertEquals(certificate, response);
        }

        @Test
        void shouldReturnBooleanResponseFromExistsApiEndpoint() {
            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(true);
            final var response = csIntegrationService.createCertificateExists("type", "version");

            assertTrue(response);
        }

        @Test
        void shouldSetUrlCorrectForExists() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.createCertificateExists("type", "version");
            verify(restTemplate).getForObject(captor.capture(), any());

            assertEquals("baseUrl/api/certificate/type/version/exists", captor.getValue());
        }
    }
}