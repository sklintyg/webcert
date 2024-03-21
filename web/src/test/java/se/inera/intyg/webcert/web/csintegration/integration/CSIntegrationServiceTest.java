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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceGetCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetListCertificatesResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateWithoutSignatureRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
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
    private static final DeleteCertificateRequestDTO DELETE_CERTIFICATE_REQUEST = DeleteCertificateRequestDTO.builder().build();
    private static final ParameterizedTypeReference<DeleteCertificateResponseDTO> DELETE_RESPONSE
        = new ParameterizedTypeReference<>() {
    };
    private static final GetListCertificatesResponseDTO LIST_RESPONSE = GetListCertificatesResponseDTO.builder()
        .certificates(List.of(CERTIFICATE))
        .build();
    private static final GetPatientCertificatesRequestDTO PATIENT_LIST_REQUEST = GetPatientCertificatesRequestDTO.builder().build();
    private static final GetUnitCertificatesRequestDTO UNIT_LIST_REQUEST = GetUnitCertificatesRequestDTO.builder().build();
    private static final ListIntygEntry CONVERTED_CERTIFICATE = new ListIntygEntry();
    private static final ValidateCertificateRequestDTO VALIDATE_REQUEST = ValidateCertificateRequestDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final ValidationErrorDTO[] VALIDATION_ERRORS = {new ValidationErrorDTO()};
    private static final ValidateCertificateResponseDTO VALIDATE_RESPONSE = ValidateCertificateResponseDTO.builder()
        .validationErrors(VALIDATION_ERRORS)
        .build();

    private static final String XML_DATA = "xmlData";
    private static final GetCertificateXmlResponseDTO GET_CERTIFICIATE_XML_RESPONSE = GetCertificateXmlResponseDTO.builder()
        .xml(XML_DATA)
        .build();

    private static final GetCertificateXmlRequestDTO GET_CERTIFICIATE_XML_REQUEST = GetCertificateXmlRequestDTO.builder().build();
    private static final SignCertificateRequestDTO SIGN_CERTIFICATE_REQUEST_DTO = SignCertificateRequestDTO.builder().build();
    private static final SignCertificateWithoutSignatureRequestDTO SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO =
        SignCertificateWithoutSignatureRequestDTO.builder()
            .build();
    private static final SignCertificateResponseDTO SIGN_CERTIFICATE_RESPONSE_DTO = SignCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final long VERSION = 0L;

    private static final byte[] BYTES = new byte[0];
    private static final PrintCertificateRequestDTO PRINT_REQUEST = PrintCertificateRequestDTO.builder().build();
    private static final PrintCertificateResponseDTO PRINT_RESPONSE = PrintCertificateResponseDTO.builder()
        .fileName("FILENAME")
        .pdfData(BYTES)
        .build();
    private static final SendCertificateRequestDTO SEND_REQUEST = SendCertificateRequestDTO.builder().build();
    private static final SendCertificateResponseDTO SEND_RESPONSE = SendCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @Mock
    private ListIntygEntryConverter listIntygEntryConverter;

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
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.certificateExists(ID)
            );
        }

        @Nested
        class HasResponse {

            private final CertificateExistsResponseDTO expectedResponse = CertificateExistsResponseDTO.builder()
                .exists(true)
                .build();

            @BeforeEach
            void setup() {
                when(restTemplate.getForObject(anyString(), any()))
                    .thenReturn(expectedResponse);
            }


            @Test
            void shouldReturnBooleanFromResponse() {
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
    }

    @Nested
    class Delete {

        void setupResponse(boolean isNull) {
            final var certificateResponse = DeleteCertificateResponseDTO.builder().certificate(CERTIFICATE).build();

            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    eq(DELETE_RESPONSE)
                )
            ).thenReturn(new ResponseEntity<>(isNull ? null : certificateResponse, HttpStatus.OK));
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {
            setupResponse(true);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            assertThrows(IllegalStateException.class, () -> csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST));
        }

        @Test
        void shouldSetUrlCorrect() {
            setupResponse(false);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(restTemplate).exchange(captor.capture(), any(HttpMethod.class), any(HttpEntity.class), eq(DELETE_RESPONSE));

            assertEquals("baseUrl/api/certificate/ID/10", captor.getValue());
        }

        @Test
        void shouldSetHttpMethod() {
            setupResponse(false);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(restTemplate).exchange(anyString(), captor.capture(), any(HttpEntity.class), eq(DELETE_RESPONSE));

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldSetRequestAsBody() {
            setupResponse(false);
            final var captor = ArgumentCaptor.forClass(HttpEntity.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(restTemplate).exchange(anyString(), any(HttpMethod.class), captor.capture(), eq(DELETE_RESPONSE));
            assertEquals(DELETE_CERTIFICATE_REQUEST, captor.getValue().getBody());
        }

        @Test
        void shouldReturnCertificate() {
            setupResponse(false);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            final var response = csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, response);
        }
    }

    @Test
    void shouldThrowExceptionIfCertificatesForPatientResponseIsNull() {
        assertThrows(IllegalStateException.class, () -> csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST));
    }

    @Test
    void shouldThrowExceptionIfCertificatesForUnitResponseIsNull() {
        assertThrows(IllegalStateException.class, () -> csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST));
    }

    @Nested
    class GetPatientCertificates {

        @BeforeEach
        void setup() {
            when(listIntygEntryConverter.convert(CERTIFICATE))
                .thenReturn(CONVERTED_CERTIFICATE);
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(LIST_RESPONSE);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetPatientCertificatesRequestDTO.class);

            csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(PATIENT_LIST_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnConvertedCertificates() {
            final var response = csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/patient/certificates", captor.getValue());
        }
    }

    @Nested
    class GetUnitCertificates {

        @BeforeEach
        void setup() {
            when(listIntygEntryConverter.convert(CERTIFICATE))
                .thenReturn(CONVERTED_CERTIFICATE);
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(LIST_RESPONSE);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetUnitCertificatesRequestDTO.class);

            csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(UNIT_LIST_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnConvertedCertificates() {
            final var response = csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/unit/certificates", captor.getValue());
        }
    }

    @Nested
    class GetUnitCertificatesInfo {

        private static final String STAFF_ID = "staffId";
        private static final String STAFF_FULL_NAME = "staffFullName";
        private final GetUnitCertificatesInfoRequestDTO listInfoRequest = GetUnitCertificatesInfoRequestDTO.builder().build();
        private final GetUnitCertificatesInfoResponseDTO listInfoResponse = GetUnitCertificatesInfoResponseDTO.builder()
            .staffs(
                List.of(
                    Staff.builder()
                        .personId(STAFF_ID)
                        .fullName(STAFF_FULL_NAME)
                        .build()
                )
            )
            .build();

        @BeforeEach
        void setup() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(listInfoResponse);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetUnitCertificatesInfoRequestDTO.class);

            csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(listInfoRequest, captor.getValue());
        }

        @Test
        void shouldReturnConvertedStaffs() {
            final var response = csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            assertEquals(List.of(new StaffListInfo(STAFF_ID, STAFF_FULL_NAME)), response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/unit/certificates/info", captor.getValue());
        }
    }

    @Nested
    class ValidateCertificate {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.validateCertificate(VALIDATE_REQUEST)
            );
        }

        @Nested
        class HasResponse {

            @BeforeEach
            void setup() {
                when(restTemplate.postForObject(anyString(), any(), any()))
                    .thenReturn(VALIDATE_RESPONSE);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(ValidateCertificateRequestDTO.class);

                csIntegrationService.validateCertificate(VALIDATE_REQUEST);
                verify(restTemplate).postForObject(anyString(), captor.capture(), any());

                assertEquals(VALIDATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnValidationErrors() {
                final var response = csIntegrationService.validateCertificate(VALIDATE_REQUEST);

                assertEquals(VALIDATION_ERRORS, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.validateCertificate(VALIDATE_REQUEST);
                verify(restTemplate).postForObject(captor.capture(), any(), any());

                assertEquals("baseUrl/api/certificate/ID/validate", captor.getValue());
            }
        }
    }

    @Nested
    class GetCertificateXml {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), eq(GET_CERTIFICIATE_XML_REQUEST), eq(GetCertificateXmlResponseDTO.class)))
                .thenReturn(GET_CERTIFICIATE_XML_RESPONSE);
            final var captor = ArgumentCaptor.forClass(GetCertificateXmlRequestDTO.class);

            csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CERTIFICIATE_XML_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnGetCertificateXmlResponseDTO() {
            when(restTemplate.postForObject(anyString(), eq(GET_CERTIFICIATE_XML_REQUEST), eq(GetCertificateXmlResponseDTO.class)))
                .thenReturn(GET_CERTIFICIATE_XML_RESPONSE);
            final var response = csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);

            assertEquals(GET_CERTIFICIATE_XML_RESPONSE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), eq(GET_CERTIFICIATE_XML_REQUEST), eq(GetCertificateXmlResponseDTO.class));

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/xml", captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), eq(GET_CERTIFICIATE_XML_REQUEST), eq(GetCertificateXmlResponseDTO.class)))
                .thenReturn(null);
            assertNull(csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID));
        }
    }

    @Nested
    class SignCertificate {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_REQUEST_DTO), eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SignCertificateRequestDTO.class);

            csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SIGN_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificateFromSignCertificateResponseDTO() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_REQUEST_DTO), eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);
            final var response = csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_REQUEST_DTO), eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);

            csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(restTemplate).postForObject(captor.capture(), eq(SIGN_CERTIFICATE_REQUEST_DTO), eq(SignCertificateResponseDTO.class));

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/sign/" + VERSION, captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_REQUEST_DTO), eq(SignCertificateResponseDTO.class)))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION));
        }
    }

    @Nested
    class SignCertificateWithoutSignature {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO),
                eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SignCertificateWithoutSignatureRequestDTO.class);

            csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificateFromSignCertificateResponseDTO() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO),
                eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);
            final var response = csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO,
                CERTIFICATE_ID, VERSION);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO),
                eq(SignCertificateResponseDTO.class)))
                .thenReturn(SIGN_CERTIFICATE_RESPONSE_DTO);

            csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(restTemplate).postForObject(captor.capture(), eq(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO),
                eq(SignCertificateResponseDTO.class));

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/signwithoutsignature/" + VERSION, captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), eq(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO),
                eq(SignCertificateResponseDTO.class)))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID,
                    VERSION));
        }
    }

    @Nested
    class PrintCertificate {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.printCertificate(ID, PRINT_REQUEST)
            );
        }

        @Nested
        class HasResponse {

            @BeforeEach
            void setup() {
                when(restTemplate.postForObject(anyString(), any(), any()))
                    .thenReturn(PRINT_RESPONSE);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(PrintCertificateRequestDTO.class);

                csIntegrationService.printCertificate(ID, PRINT_REQUEST);
                verify(restTemplate).postForObject(anyString(), captor.capture(), any());

                assertEquals(PRINT_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnPdf() {
                final var response = csIntegrationService.printCertificate(ID, PRINT_REQUEST);

                assertEquals(PRINT_RESPONSE.getPdfData(), response.getPdfData());
            }

            @Test
            void shouldReturnFileName() {
                final var response = csIntegrationService.printCertificate(ID, PRINT_REQUEST);

                assertEquals(PRINT_RESPONSE.getFileName(), response.getFilename());
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.printCertificate(ID, PRINT_REQUEST);
                verify(restTemplate).postForObject(captor.capture(), any(), any());

                assertEquals("baseUrl/api/certificate/ID/pdf", captor.getValue());
            }
        }
    }

    @Nested
    class SendCertificate {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.sendCertificate(ID, SEND_REQUEST)
            );
        }

        @Nested
        class HasResponse {

            @BeforeEach
            void setup() {
                when(restTemplate.postForObject(anyString(), any(), any()))
                    .thenReturn(SEND_RESPONSE);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);

                csIntegrationService.sendCertificate(ID, SEND_REQUEST);
                verify(restTemplate).postForObject(anyString(), captor.capture(), any());

                assertEquals(SEND_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.sendCertificate(ID, SEND_REQUEST);

                assertEquals(SEND_RESPONSE.getCertificate(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.sendCertificate(ID, SEND_REQUEST);
                verify(restTemplate).postForObject(captor.capture(), any(), any());

                assertEquals("baseUrl/api/certificate/ID/send", captor.getValue());
            }
        }
    }
}