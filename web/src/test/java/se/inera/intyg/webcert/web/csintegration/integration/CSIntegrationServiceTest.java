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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceGetCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@ExtendWith(MockitoExtension.class)
class CSIntegrationServiceTest {

    private static final CertificateTypeInfoDTO CONVERTED_TYPE_INFO = new CertificateTypeInfoDTO();
    private static final CertificateServiceTypeInfoDTO TYPE_INFO = CertificateServiceTypeInfoDTO.builder().build();
    private static final List<CertificateServiceTypeInfoDTO> TYPE_INFOS = List.of(TYPE_INFO);
    private static final CertificateServiceTypeInfoRequestDTO TYPE_INFO_REQUEST = CertificateServiceTypeInfoRequestDTO.builder().build();
    private static final CertificateServiceTypeInfoResponseDTO TYPE_INFO_RESPONSE = CertificateServiceTypeInfoResponseDTO.builder()
        .list(TYPE_INFOS)
        .build();
    private static final Certificate CERTIFICATE = new Certificate();
    private static final CreateCertificateRequestDTO CREATE_CERTIFICATE_REQUEST = CreateCertificateRequestDTO.builder().build();
    private static final CertificateServiceCreateCertificateResponseDTO CREATE_RESPONSE =
        CertificateServiceCreateCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST = GetCertificateRequestDTO.builder().build();
    private static final CertificateServiceGetCertificateResponseDTO GET_RESPONSE = CertificateServiceGetCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final String ID = "ID";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @InjectMocks
    private CSIntegrationService csIntegrationService;

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(ID)
                .build()
        );
    }

    @Test
    void shouldReturnEmptyListIfTypeInfoResponseIsNull() {
        final var response = csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
        assertEquals(Collections.emptyList(), response);
    }

    @Nested
    class TypeInfo {

        @BeforeEach
        void setUp() {
            when(certificateTypeInfoConverter.convert(any()))
                .thenReturn(CONVERTED_TYPE_INFO);
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(TYPE_INFO_RESPONSE);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);
            csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());
            assertEquals(TYPE_INFO_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnResponse() {
            final var response = csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
            assertTrue(response.contains(CONVERTED_TYPE_INFO));
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificatetypeinfo", captor.getValue());
        }
    }

    @Nested
    class CreateCertificate {

        @Test
        void shouldThrowExceptionIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST)
            );
        }

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_RESPONSE);
            final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

            csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(CREATE_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_RESPONSE);
            final var response = csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_RESPONSE);

            csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate", captor.getValue());
        }
    }

    @Nested
    class CertificateTypeExists {

        @Test
        void shouldReturnModelIdFromResponse() {
            final var expectedResponse = CertificateTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .type("type")
                        .version("version")
                        .build()
                )
                .build();

            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(expectedResponse);

            final var response = csIntegrationService.certificateTypeExists("type");

            assertEquals(expectedResponse.getCertificateModelId(), response.orElse(null));
        }

        @Test
        void shouldReturnNullIfTypeIsMissing() {
            final var expectedResponse = CertificateTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .version("version")
                        .build()
                )
                .build();

            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(expectedResponse);

            final var response = csIntegrationService.certificateTypeExists("type");

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldReturnNullIfVersionIsMissing() {
            final var expectedResponse = CertificateTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .type("type")
                        .build()
                )
                .build();

            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(expectedResponse);

            final var response = csIntegrationService.certificateTypeExists("type");

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldReturnNullIfObjectIsEmpty() {
            final var expectedResponse = CertificateTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder().build()
                )
                .build();

            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(expectedResponse);

            final var response = csIntegrationService.certificateTypeExists("type");

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.certificateTypeExists("fk7211");
            verify(restTemplate).getForObject(captor.capture(), any());

            assertEquals("baseUrl/api/certificatetypeinfo/fk7211/exists", captor.getValue());
        }
    }

    @Nested
    class GetCertificate {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_RESPONSE);
            final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);

            csIntegrationService.getCertificate(ID, GET_CERTIFICATE_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_RESPONSE);
            final var response = csIntegrationService.getCertificate(ID, GET_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificate("id", GET_CERTIFICATE_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate/id", captor.getValue());
        }
    }

    @Nested
    class CertificateExists {

        @Test
        void shouldReturnBooleanFromResponse() {
            final var expectedResponse = CertificateExistsResponseDTO.builder()
                .exists(true)
                .build();
            when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(expectedResponse);

            final var response = csIntegrationService.certificateExists("id");

            assertEquals(expectedResponse.getExists(), response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.certificateExists("id");
            verify(restTemplate).getForObject(captor.capture(), any());

            assertEquals("baseUrl/api/certificate/id/exists", captor.getValue());
        }
    }

    @Nested
    class SaveCertificate {

        private final SaveCertificateRequestDTO saveCertificateRequestDTO = SaveCertificateRequestDTO.builder()
            .certificate(CERTIFICATE)
            .build();
        private final SaveCertificateResponseDTO saveCertificateResponseDTO = SaveCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(saveCertificateResponseDTO);
            final var captor = ArgumentCaptor.forClass(SaveCertificateResponseDTO.class);

            csIntegrationService.saveCertificate(saveCertificateRequestDTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(saveCertificateRequestDTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(saveCertificateResponseDTO);
            final var response = csIntegrationService.saveCertificate(saveCertificateRequestDTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveCertificate(saveCertificateRequestDTO)
            );
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(saveCertificateResponseDTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.saveCertificate(saveCertificateRequestDTO);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate/ID/save", captor.getValue());
        }
    }
}