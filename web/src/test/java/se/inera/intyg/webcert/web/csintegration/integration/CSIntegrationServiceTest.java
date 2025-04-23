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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.*;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

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
    private static final ReadyForSignResponseDTO READY_FOR_SIGN_RESPONSE_DTO = ReadyForSignResponseDTO.builder().certificate(CERTIFICATE)
        .build();
    private static final CreateCertificateRequestDTO CREATE_CERTIFICATE_REQUEST = CreateCertificateRequestDTO.builder().build();
    private static final CertificateServiceCreateCertificateResponseDTO CREATE_RESPONSE =
        CertificateServiceCreateCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();
    private static final ReplaceCertificateRequestDTO REPLACE_CERTIFICATE_REQUEST = ReplaceCertificateRequestDTO.builder().build();
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST = GetCertificateRequestDTO.builder().build();
    private static final GetCitizenCertificateRequestDTO GET_CITIZEN_CERTIFICATE_REQUEST_DTO = GetCitizenCertificateRequestDTO.builder()
        .build();
    private static final GetCitizenCertificatePdfRequestDTO GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO =
        GetCitizenCertificatePdfRequestDTO.builder()
            .build();
    public static final ReplaceCertificateResponseDTO REPLACE_CERTIFICATE_RESPONSE = ReplaceCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final CertificateServiceGetCertificateResponseDTO GET_RESPONSE = CertificateServiceGetCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();

    private static final List<CertificateText> CERTIFICATE_TEXTS = List.of(CertificateText.builder().build());
    private static final List<AvailableFunctionDTO> AVAILABLE_FUNCTIONS = List.of(new AvailableFunctionDTO());
    private static final GetCitizenCertificateResponseDTO GET_CITIZEN_CERTIFICATE_RESPONSE_DTO = GetCitizenCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .texts(CERTIFICATE_TEXTS)
        .availableFunctions(AVAILABLE_FUNCTIONS)
        .build();
    private static final String FILE_NAME = "fileName";
    private static final byte[] PDF_DATA = "pdfData".getBytes(StandardCharsets.UTF_8);
    private static final GetCitizenCertificatePdfResponseDTO GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO =
        GetCitizenCertificatePdfResponseDTO.builder()
            .filename(FILE_NAME)
            .pdfData(PDF_DATA)
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
    private static final RevokeCertificateRequestDTO REVOKE_REQUEST = RevokeCertificateRequestDTO.builder().build();
    private static final RevokeCertificateResponseDTO REVOKE_RESPONSE = RevokeCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final RenewCertificateRequestDTO RENEW_CERTIFICATE_REQUEST = RenewCertificateRequestDTO.builder().build();
    private static final RenewCertificateResponseDTO RENEW_CERTIFICATE_RESPONSE = RenewCertificateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final IncomingMessageRequestDTO INCOMING_MESSAGE_REQUEST_DTO = IncomingMessageRequestDTO.builder()
        .build();

    private static final CertificateComplementRequestDTO COMPLEMENT_CERTIFICATE_REQUEST = CertificateComplementRequestDTO.builder().build();
    private static final CertificateComplementResponseDTO COMPLEMENT_CERTIFICATE_RESPONSE = CertificateComplementResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final AnswerComplementRequestDTO ANSWER_COMPLEMENT_REQUEST = AnswerComplementRequestDTO.builder().build();
    private static final AnswerComplementResponseDTO ANSWER_COMPLEMENT_RESPONSE = AnswerComplementResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final Question QUESTION = Question.builder().build();
    private static final SendAnswerResponseDTO SEND_ANSWER_RESPONSE_DTO = SendAnswerResponseDTO.builder().question(QUESTION).build();
    private static final DeleteAnswerResponseDTO DELETE_ANSWER_RESPONSE_DTO = DeleteAnswerResponseDTO.builder().question(QUESTION).build();
    private static final SendMessageResponseDTO SEND_MESSAGE_RESPONSE_DTO = SendMessageResponseDTO.builder().question(QUESTION).build();
    private static final List<Question> QUESTIONS = List.of(QUESTION);
    private static final GetCertificateMessageRequestDTO GET_CERTIFICATE_MESSAGE_REQUEST_DTO = GetCertificateMessageRequestDTO.builder()
        .build();
    private static final GetCertificateMessageResponseDTO GET_CERTIFICATE_MESSAGE_RESPONSE_DTO = GetCertificateMessageResponseDTO.builder()
        .questions(QUESTIONS)
        .build();
    private static final GetCertificateMessageInternalResponseDTO GET_CERTIFICATE_MESSAGE_INTERNAL_RESPONSE_DTO =
        GetCertificateMessageInternalResponseDTO.builder()
            .questions(QUESTIONS)
            .build();

    private static final HandleMessageRequestDTO HANDLE_MESSAGE_REQUEST_DTO = HandleMessageRequestDTO.builder()
        .build();
    private static final HandleMessageResponseDTO HANDLE_MESSAGE_RESPONSE_DTO = HandleMessageResponseDTO.builder()
        .question(QUESTION)
        .build();
    private static final GetCertificateFromMessageRequestDTO GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO =
        GetCertificateFromMessageRequestDTO.builder()
            .build();
    private static final GetCertificateFromMessageResponseDTO GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO =
        GetCertificateFromMessageResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();

    private static final InternalCertificateXmlResponseDTO INTERAL_CERTIFICATE_XML_RESPONSE_DTO =
        InternalCertificateXmlResponseDTO.builder()
            .xml(XML_DATA)
            .build();
    private static final DeleteMessageRequestDTO DELETE_MESSAGE_REQUEST_DTO = DeleteMessageRequestDTO.builder().build();
    private static final CreateMessageRequestDTO CREATE_MESSAGE_REQUEST_DTO = CreateMessageRequestDTO.builder().build();
    private static final CreateMessageResponseDTO CREATE_MESSAGE_RESPONSE_DTO = CreateMessageResponseDTO.builder()
        .question(QUESTION)
        .build();
    private static final SaveMessageResponseDTO SAVE_MESSAGE_RESPONSE_DTO = SaveMessageResponseDTO.builder()
        .question(QUESTION)
        .build();
    private static final SaveMessageRequestDTO SAVE_MESSAGE_REQUEST_DTO = SaveMessageRequestDTO.builder()
        .build();
    private static final SendMessageRequestDTO SEND_MESSAGE_REQUEST_DTO = SendMessageRequestDTO.builder().build();

    private static final GetUnitQuestionsRequestDTO GET_QUESTIONS_REQUEST = GetUnitQuestionsRequestDTO.builder().build();
    private static final ArendeListItem ARENDE_LIST_ITEM = new ArendeListItem();
    private static final CertificateDTO CERTIFICATE_DTO = new CertificateDTO();
    private static final Question QUESTION_DTO = Question.builder()
        .certificateId(ID)
        .build();
    private static final GetUnitQuestionsResponseDTO GET_QUESTIONS_RESPONSE = GetUnitQuestionsResponseDTO.builder()
        .certificates(List.of(CERTIFICATE_DTO))
        .questions(List.of(QUESTION_DTO))
        .build();
    private static final SaveAnswerResponseDTO SAVE_ANSWER_RESPONSE_DTO = SaveAnswerResponseDTO.builder()
        .question(QUESTION)
        .build();
    private static final SaveAnswerRequestDTO SAVE_ANSWER_REQUEST_DTO = SaveAnswerRequestDTO.builder().build();
    private static final DeleteAnswerRequestDTO DELETE_ANSWER_REQUEST_DTO = DeleteAnswerRequestDTO.builder().build();
    private static final SendAnswerRequestDTO SEND_ANSWER_REQUEST_DTO = SendAnswerRequestDTO.builder().build();
    private static final String MESSAGE_ID = "messageId";
    private static final ForwardCertificateRequestDTO FORWARD_CERTIFICATE_REQUEST_DTO = ForwardCertificateRequestDTO.builder().build();
    private static final ForwardCertificateResponseDTO FORWARD_CERTIFICATE_RESPONSE_DTO =
        ForwardCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();
    private static final List<CertificateEventDTO> EVENTS = List.of(new CertificateEventDTO());
    private static final GetCertificateEventsRequestDTO GET_CERTIFICATE_EVENTS_REQUEST_DTO = GetCertificateEventsRequestDTO.builder()
        .build();
    private static final GetCertificateEventsResponseDTO GET_CERTIFICATE_EVENTS_RESPONSE_DTO =
        GetCertificateEventsResponseDTO.builder()
            .events(EVENTS)
            .build();
    private static final String LIST = "list";
    private static final CertificatesWithQAResponseDTO GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO =
        CertificatesWithQAResponseDTO.builder()
            .list(LIST)
            .build();
    private static final CertificatesWithQARequestDTO GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO =
        CertificatesWithQARequestDTO.builder()
            .build();
    private static final LockDraftsResponseDTO LOCK_OLD_DRAFTS_RESPONSE_DTO = LockDraftsResponseDTO.builder()
        .certificates(List.of(CERTIFICATE)).build();
    private static final LockDraftsRequestDTO LOCK_OLD_DRAFTS_REQUEST_DTO = LockDraftsRequestDTO.builder().build();
    private static final Map<String, StatisticsForUnitDTO> USER_STATISTICS = Map.of("unit", StatisticsForUnitDTO.builder().build());
    private static final UnitStatisticsResponseDTO STATISTICS_RESPONSE_DTO = UnitStatisticsResponseDTO.builder()
        .unitStatistics(USER_STATISTICS)
        .build();
    private static final UnitStatisticsRequestDTO STATISTICS_REQUEST_DTO = UnitStatisticsRequestDTO.builder().build();
    private static final ReadyForSignRequestDTO READY_FOR_SIGN_REQUEST_DTO = ReadyForSignRequestDTO.builder().build();
    private static final SaveCertificateRequestDTO SAVE_CERTIFICATE_REQUEST_DTO = SaveCertificateRequestDTO.builder().certificate(CERTIFICATE).build();
    private static final SaveCertificateResponseDTO SAVE_CERTIFICATE_RESPONSE_DTO = SaveCertificateResponseDTO.builder().certificate(CERTIFICATE).build();

    @Mock
    private RestClient restClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @Mock
    private ListIntygEntryConverter listIntygEntryConverter;

    @Mock
    private ListQuestionConverter listQuestionConverter;

    @InjectMocks
    private CSIntegrationService csIntegrationService;

    RestClient.RequestBodyUriSpec requestBodyUriSpec;
    RestClient.ResponseSpec responseSpec;
    RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    RestClient.RequestHeadersSpec requestHeadersSpec;

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(ID)
                .build()
        );
    }

    @Test
    void shouldReturnEmptyListIfTypeInfoResponseIsNull() {

        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        final String uri = "baseUrl/api/certificatetypeinfo";
        ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(CertificateServiceTypeInfoRequestDTO.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        final var response = CertificateServiceTypeInfoResponseDTO.builder()
                .list(List.of())
                .build();

        doReturn(response).when(responseSpec).body(CertificateServiceTypeInfoResponseDTO.class);

        final var actualResponse = csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
        assertEquals(Collections.emptyList(), actualResponse);
    }

    @Nested
    class TypeInfo {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificatetypeinfo";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CertificateServiceTypeInfoRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(CertificateServiceTypeInfoRequestDTO.class);

            final var response = CertificateServiceTypeInfoResponseDTO.builder()
                    .list(List.of())
                    .build();

            doReturn(response).when(responseSpec).body(CertificateServiceTypeInfoResponseDTO.class);

            csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());
            assertEquals(TYPE_INFO_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnResponse() {

            when(certificateTypeInfoConverter.convert(TYPE_INFO)).thenReturn(CONVERTED_TYPE_INFO);

            doReturn(TYPE_INFO_RESPONSE).when(responseSpec).body(CertificateServiceTypeInfoResponseDTO.class);

            final var actualResponse = csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);

            assertTrue(actualResponse.contains(CONVERTED_TYPE_INFO));
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var response = CertificateServiceTypeInfoResponseDTO.builder()
                    .list(List.of())
                    .build();

            doReturn(response).when(responseSpec).body(CertificateServiceTypeInfoResponseDTO.class);

            csIntegrationService.getTypeInfo(TYPE_INFO_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificatetypeinfo", captor.getValue());
        }
    }

    @Nested
    class CreateCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CreateCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
            }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(CertificateServiceCreateCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST)
            );
        }

        @Test
        void shouldPreformPostUsingRequest() {

            final var captor = ArgumentCaptor.forClass(CreateCertificateRequestDTO.class);

            final var response = CertificateServiceCreateCertificateResponseDTO.builder()
                    .build();

            doReturn(response).when(responseSpec).body(CertificateServiceCreateCertificateResponseDTO.class);

            csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(CREATE_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {

            doReturn(CREATE_RESPONSE).when(responseSpec).body(CertificateServiceCreateCertificateResponseDTO.class);
            final var response = csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            doReturn(CREATE_RESPONSE).when(responseSpec).body(CertificateServiceCreateCertificateResponseDTO.class);

            csIntegrationService.createCertificate(CREATE_CERTIFICATE_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate", captor.getValue());
        }
    }

    @Nested
    class ReplaceCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/replace";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(ReplaceCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(ReplaceCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.replaceCertificate(CERTIFICATE_ID, REPLACE_CERTIFICATE_REQUEST)
            );
        }

        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                doReturn(REPLACE_CERTIFICATE_RESPONSE).when(responseSpec).body(ReplaceCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(ReplaceCertificateRequestDTO.class);

                csIntegrationService.replaceCertificate(CERTIFICATE_ID, REPLACE_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(REPLACE_CERTIFICATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.replaceCertificate(CERTIFICATE_ID, REPLACE_CERTIFICATE_REQUEST);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.replaceCertificate(CERTIFICATE_ID, REPLACE_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/certificateId/replace", captor.getValue());
            }
        }
    }

    @Nested
    class RenewCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/renew";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(RenewCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(RenewCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.renewCertificate(CERTIFICATE_ID, RENEW_CERTIFICATE_REQUEST)
            );
        }

        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                doReturn(RENEW_CERTIFICATE_RESPONSE).when(responseSpec).body(RenewCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(RenewCertificateRequestDTO.class);

                csIntegrationService.renewCertificate(CERTIFICATE_ID, RENEW_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(RENEW_CERTIFICATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.renewCertificate(CERTIFICATE_ID, RENEW_CERTIFICATE_REQUEST);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.renewCertificate(CERTIFICATE_ID, RENEW_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/certificateId/renew", captor.getValue());
            }
        }
    }

    @Nested
    class ComplementCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/complement";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CertificateComplementRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(CertificateComplementResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.complementCertificate(CERTIFICATE_ID, COMPLEMENT_CERTIFICATE_REQUEST)
            );
        }

        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                doReturn(COMPLEMENT_CERTIFICATE_RESPONSE).when(responseSpec).body(CertificateComplementResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(CertificateComplementRequestDTO.class);

                csIntegrationService.complementCertificate(CERTIFICATE_ID, COMPLEMENT_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(COMPLEMENT_CERTIFICATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.complementCertificate(CERTIFICATE_ID, COMPLEMENT_CERTIFICATE_REQUEST);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.complementCertificate(CERTIFICATE_ID, COMPLEMENT_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/certificateId/complement", captor.getValue());
            }
        }
    }

    @Nested
    class CertificateTypeExists {

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/api/certificatetypeinfo/type/exists";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

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

            doReturn(expectedResponse).when(responseSpec).body(CertificateTypeExistsResponseDTO.class);

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

            doReturn(expectedResponse).when(responseSpec).body(CertificateTypeExistsResponseDTO.class);

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

            doReturn(expectedResponse).when(responseSpec).body(CertificateTypeExistsResponseDTO.class);

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

            doReturn(expectedResponse).when(responseSpec).body(CertificateTypeExistsResponseDTO.class);

            final var response = csIntegrationService.certificateTypeExists("type");

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.certificateTypeExists("type");
            verify(requestHeadersUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificatetypeinfo/type/exists", captor.getValue());
        }
    }

    @Nested
    class CertificateExternalTypeExists {


        private static final String CODE_SYSTEM = "codeSystem";
        private static final String CODE = "code";

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/api/certificatetypeinfo/codeSystem/code/exists";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldReturnModelIdFromResponse() {
            final var expectedResponse = CertificateExternalTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .type("type")
                        .version("version")
                        .build()
                )
                .build();

            doReturn(expectedResponse).when(responseSpec).body(CertificateExternalTypeExistsResponseDTO.class);

            final var response = csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE);

            assertEquals(expectedResponse.getCertificateModelId(), response.orElse(null));
        }

        @Test
        void shouldReturnNullIfTypeIsMissing() {
            final var expectedResponse = CertificateExternalTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .version("version")
                        .build()
                )
                .build();

            doReturn(expectedResponse).when(responseSpec).body(CertificateExternalTypeExistsResponseDTO.class);

            final var response = csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE);

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldReturnNullIfVersionIsMissing() {
            final var expectedResponse = CertificateExternalTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder()
                        .type("type")
                        .build()
                )
                .build();

            doReturn(expectedResponse).when(responseSpec).body(CertificateExternalTypeExistsResponseDTO.class);

            final var response = csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE);

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldReturnNullIfObjectIsEmpty() {
            final var expectedResponse = CertificateExternalTypeExistsResponseDTO.builder()
                .certificateModelId(
                    CertificateModelIdDTO.builder().build()
                )
                .build();

            doReturn(expectedResponse).when(responseSpec).body(CertificateExternalTypeExistsResponseDTO.class);

            final var response = csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE);

            assertTrue(response.isEmpty());
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.certificateExternalTypeExists(CODE_SYSTEM, CODE);
            verify(requestHeadersUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificatetypeinfo/" + CODE_SYSTEM + "/" + CODE + "/exists", captor.getValue());
        }
    }

    @Nested
    class GetCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/id";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetCertificateRequestDTO.class);

            doReturn(GET_RESPONSE).when(responseSpec).body(CertificateServiceGetCertificateResponseDTO.class);

            csIntegrationService.getCertificate(ID, GET_CERTIFICATE_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(GET_RESPONSE).when(responseSpec).body(CertificateServiceGetCertificateResponseDTO.class);

            final var response = csIntegrationService.getCertificate(ID, GET_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            doReturn(GET_RESPONSE).when(responseSpec).body(CertificateServiceGetCertificateResponseDTO.class);

            csIntegrationService.getCertificate("id", GET_CERTIFICATE_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/id", captor.getValue());
        }
    }

    @Nested
    class CertificateExists {

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/api/certificate/ID/exists";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

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
                doReturn(expectedResponse).when(responseSpec).body(CertificateExistsResponseDTO.class);
            }


            @Test
            void shouldReturnBooleanFromResponse() {
                final var response = csIntegrationService.certificateExists("ID");

                assertEquals(expectedResponse.getExists(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.certificateExists("ID");
                verify(requestHeadersUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID/exists", captor.getValue());
            }
        }
    }


    @Nested
    class MessageExists {

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/api/message/ID/exists";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.messageExists(ID)
            );
        }

        @Nested
        class HasResponse {

            private final MessageExistsResponseDTO expectedResponse = MessageExistsResponseDTO.builder()
                .exists(true)
                .build();

            @BeforeEach
            void setup() {
                doReturn(expectedResponse).when(responseSpec).body(MessageExistsResponseDTO.class);
            }


            @Test
            void shouldReturnBooleanFromResponse() {
                final var response = csIntegrationService.messageExists("ID");

                assertEquals(expectedResponse.getExists(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.messageExists("ID");
                verify(requestHeadersUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/message/ID/exists", captor.getValue());
            }
        }
    }

    @Nested
    class CitizenCertificateExists {

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/api/citizen/certificate/ID/exists";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                    IllegalStateException.class, () -> csIntegrationService.citizenCertificateExists(ID)
            );
        }

        @Nested
        class HasResponse {
            private final CitizenCertificateExistsResponseDTO expectedResponse = CitizenCertificateExistsResponseDTO.builder()
                    .exists(true)
                    .build();

            @BeforeEach
            void setup() {
                doReturn(expectedResponse).when(responseSpec).body(CitizenCertificateExistsResponseDTO.class);
            }

            @Test
            void shouldReturnBooleanFromResponse() {
                final var response = csIntegrationService.citizenCertificateExists("ID");

                assertEquals(expectedResponse.getExists(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.citizenCertificateExists("ID");
                verify(requestHeadersUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/citizen/certificate/ID/exists", captor.getValue());
            }
        }
    }

    @Nested
    class SaveCertificate {
        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.put()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SaveCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {
            doReturn(SAVE_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO)
            );
        }


        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                doReturn(SAVE_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(SaveCertificateRequestDTO.class);

                csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(SAVE_CERTIFICATE_REQUEST_DTO, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID", captor.getValue());
            }
        }
    }

    @Nested
    class Delete {

        void setupResponse(boolean isNull) {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/10";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(DeleteCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
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

            final var response = DeleteCertificateResponseDTO.builder()
                    .certificate(CERTIFICATE)
                    .build();

            doReturn(response).when(responseSpec).body(DeleteCertificateResponseDTO.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/ID/10", captor.getValue());
        }

        @Test
        void shouldSetHttpMethod() {
            setupResponse(false);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            final var response = DeleteCertificateResponseDTO.builder()
                    .certificate(CERTIFICATE)
                    .build();

            doReturn(response).when(responseSpec).body(DeleteCertificateResponseDTO.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(restClient).method(captor.capture());

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldSetRequestAsBody() {
            setupResponse(false);
            final var captor = ArgumentCaptor.forClass(DeleteCertificateRequestDTO.class);

            final var response = DeleteCertificateResponseDTO.builder()
                    .certificate(CERTIFICATE)
                    .build();

            doReturn(response).when(responseSpec).body(DeleteCertificateResponseDTO.class);

            csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            verify(requestBodyUriSpec).body(captor.capture());
            assertEquals(DELETE_CERTIFICATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            setupResponse(false);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            final var response = DeleteCertificateResponseDTO.builder()
                    .certificate(CERTIFICATE)
                    .build();

            doReturn(response).when(responseSpec).body(DeleteCertificateResponseDTO.class);

            final var actualResponse = csIntegrationService.deleteCertificate("ID", 10, DELETE_CERTIFICATE_REQUEST);

            assertEquals(CERTIFICATE, actualResponse);
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
            when(listIntygEntryConverter.convert(CERTIFICATE)).thenReturn(CONVERTED_CERTIFICATE);

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/patient/certificates";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetPatientCertificatesRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetPatientCertificatesRequestDTO.class);

            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(PATIENT_LIST_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnConvertedCertificates() {
            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            final var actualResponse = csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), actualResponse);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/patient/certificates", captor.getValue());
        }
    }

    @Nested
    class GetUnitCertificates {

        @BeforeEach
        void setup() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/unit/certificates";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetUnitCertificatesRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

            when(listIntygEntryConverter.convert(CERTIFICATE)).thenReturn(CONVERTED_CERTIFICATE);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            final var expectedResponse = List.of(new ListIntygEntry());

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            final var actualResponse = csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);

            assertEquals(expectedResponse, actualResponse);
        }

        @Test
        void shouldReturnConvertedCertificates() {

            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            final var actualResponse = csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), actualResponse);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var response = GetListCertificatesResponseDTO.builder()
                    .certificates(List.of(CERTIFICATE))
                    .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);

            verify(requestBodyUriSpec).uri(captor.capture());

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
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/unit/certificates/info";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetUnitCertificatesInfoRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            final var response = GetUnitCertificatesInfoResponseDTO.builder()
                    .staffs(List.of())
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitCertificatesInfoResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(GetUnitCertificatesInfoRequestDTO.class);

            csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(listInfoRequest, captor.getValue());
        }

        @Test
        void shouldReturnConvertedStaffs() {
            final var response = GetUnitCertificatesInfoResponseDTO.builder()
                    .staffs(List.of(Staff.builder().personId(STAFF_ID).fullName(STAFF_FULL_NAME).build()))
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitCertificatesInfoResponseDTO.class);
            final var actualResponse = csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            assertEquals(List.of(new StaffListInfo(STAFF_ID, STAFF_FULL_NAME)), actualResponse);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var response = GetUnitCertificatesInfoResponseDTO.builder()
                    .staffs(List.of(Staff.builder().personId(STAFF_ID).fullName(STAFF_FULL_NAME).build()))
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitCertificatesInfoResponseDTO.class);

            csIntegrationService.listCertificatesInfoForUnit(listInfoRequest);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/unit/certificates/info", captor.getValue());
        }
    }

    @Nested
    class ValidateCertificate {

        @BeforeEach
        void setup() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/validate";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(ValidateCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            doReturn(null).when(responseSpec).body(ValidateCertificateResponseDTO.class);

            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.validateCertificate(VALIDATE_REQUEST)
            );
        }

        @Nested
        class HasResponse {

            @BeforeEach
            void setup() {
                doReturn(VALIDATE_RESPONSE).when(responseSpec).body(ValidateCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(ValidateCertificateRequestDTO.class);

                csIntegrationService.validateCertificate(VALIDATE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(VALIDATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnValidationErrors() {
                final var response = csIntegrationService.validateCertificate(VALIDATE_REQUEST);

                assertEquals(VALIDATION_ERRORS, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.validateCertificate(VALIDATE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID/validate", captor.getValue());
            }
        }
    }

    @Nested
    class GetCertificateXml {

        @BeforeEach
        void setup() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/xml";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCertificateXmlRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CERTIFICIATE_XML_RESPONSE).when(responseSpec).body(GetCertificateXmlResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(GetCertificateXmlRequestDTO.class);

            csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CERTIFICIATE_XML_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnGetCertificateXmlResponseDTO() {

            doReturn(GET_CERTIFICIATE_XML_RESPONSE).when(responseSpec).body(GetCertificateXmlResponseDTO.class);

            final var response = csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);

            assertEquals(GET_CERTIFICIATE_XML_RESPONSE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/xml", captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetCertificateXmlResponseDTO.class);
            assertNull(csIntegrationService.getCertificateXml(GET_CERTIFICIATE_XML_REQUEST, CERTIFICATE_ID));
        }
    }

    @Nested
    class SignCertificate {

        @BeforeEach
        void setup() {
            //ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/sign/0";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SignCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(SignCertificateRequestDTO.class);

            csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SIGN_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificateFromSignCertificateResponseDTO() {

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);
            final var response = csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);

            csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/sign/" + VERSION, captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(SignCertificateResponseDTO.class);
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

    @Nested
    class RevokeCertificate {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
        }

        @Test
        void shouldThrowExceptionIfNullResponse() {
            assertThrows(
                IllegalStateException.class, () -> csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST)
            );
        }

        @Nested
        class HasResponse {

            @BeforeEach
            void setup() {
                when(restTemplate.postForObject(anyString(), any(), any()))
                    .thenReturn(REVOKE_RESPONSE);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(RevokeCertificateRequestDTO.class);

                csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);
                verify(restTemplate).postForObject(anyString(), captor.capture(), any());

                assertEquals(REVOKE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);

                assertEquals(REVOKE_RESPONSE.getCertificate(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);
                verify(restTemplate).postForObject(captor.capture(), any(), any());

                assertEquals("baseUrl/api/certificate/ID/revoke", captor.getValue());
            }
        }
    }

    @Nested
    class GetCitizenCertificate {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(GetCitizenCertificateRequestDTO.class);

            csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnGetCitizenCertificateResponseDTO() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO);
            final var response = csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, ID);

            assertEquals(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO, response);
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/citizen/certificate/" + CERTIFICATE_ID, captor.getValue());
        }
    }

    @Nested
    class GetCitizenCertificatePdf {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(GetCitizenCertificatePdfRequestDTO.class);

            csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnGetCitizenCertificatePdfResponseDTO() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO);
            final var response = csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, ID);

            assertEquals(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO, response);
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/citizen/certificate/" + CERTIFICATE_ID + "/print", captor.getValue());
        }
    }

    @Nested
    class AnswerComplementCertificate {

        @Test
        void shouldThrowExceptionIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST)
            );
        }

        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                when(restTemplate.postForObject(anyString(), any(), any()))
                    .thenReturn(ANSWER_COMPLEMENT_RESPONSE);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(AnswerComplementRequestDTO.class);

                csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST);
                verify(restTemplate).postForObject(anyString(), captor.capture(), any());

                assertEquals(ANSWER_COMPLEMENT_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST);
                verify(restTemplate).postForObject(captor.capture(), any(), any());

                assertEquals("baseUrl/api/certificate/certificateId/answerComplement", captor.getValue());
            }
        }
    }

    @Nested
    class PostMessage {

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(IncomingMessageRequestDTO.class);

            csIntegrationService.postMessage(INCOMING_MESSAGE_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(INCOMING_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.postMessage(INCOMING_MESSAGE_REQUEST_DTO);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message", captor.getValue());
        }
    }

    @Nested
    class GetCertificateQuestions {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(GetCertificateMessageRequestDTO.class);

            csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestions() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);

            assertEquals(QUESTIONS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + CERTIFICATE_ID, captor.getValue());
        }
    }

    @Nested
    class HandleMessageTests {

        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(HANDLE_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(HandleMessageRequestDTO.class);

            csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(HANDLE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestions() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(HANDLE_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(HANDLE_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/handle", captor.getValue());
        }
    }

    @Nested
    class GetCertificateFromMessageRequestTests {

        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(GetCertificateFromMessageRequestDTO.class);

            csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/certificate", captor.getValue());
        }
    }

    @Nested
    class GetCertificateQuestionsInternal {

        @Test
        void shouldReturnQuestions() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_MESSAGE_INTERNAL_RESPONSE_DTO);
            final var response = csIntegrationService.getQuestions(CERTIFICATE_ID);

            assertEquals(QUESTIONS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getQuestions(CERTIFICATE_ID));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_MESSAGE_INTERNAL_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getQuestions(CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/internalapi/message/" + CERTIFICATE_ID, captor.getValue());
        }
    }

    @Nested
    class GetCertificateInternal {

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_RESPONSE);
            final var response = csIntegrationService.getInternalCertificate(ID);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getInternalCertificate(ID)
            );
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_RESPONSE);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getInternalCertificate("id");
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/internalapi/certificate/id", captor.getValue());
        }
    }

    @Nested
    class GetCertificateXmlInternal {

        @Test
        void shouldReturnXml() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(INTERAL_CERTIFICATE_XML_RESPONSE_DTO);
            final var response = csIntegrationService.getInternalCertificateXml(ID);

            assertEquals(XML_DATA, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getInternalCertificateXml(ID)
            );
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(INTERAL_CERTIFICATE_XML_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getInternalCertificateXml("id");
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/internalapi/certificate/id/xml", captor.getValue());
        }
    }

    @Nested
    class DeleteMessage {

        @Test
        void shouldSetHttpMethod() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
                )
            ).thenReturn(new ResponseEntity<>(HttpStatus.OK));
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            csIntegrationService.deleteMessage(MESSAGE_ID, DELETE_MESSAGE_REQUEST_DTO);

            verify(restTemplate).exchange(anyString(), captor.capture(), any(HttpEntity.class), any(ParameterizedTypeReference.class));

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.deleteMessage(
                "messageId",
                DELETE_MESSAGE_REQUEST_DTO
            );

            verify(restTemplate).exchange(
                captor.capture(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

            assertEquals("baseUrl/api/message/messageId/delete", captor.getValue());
        }
    }

    @Nested
    class CreateMessageTest {


        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(CreateMessageRequestDTO.class);

            csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(CREATE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(CREATE_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + CERTIFICATE_ID + "/create", captor.getValue());
        }
    }

    @Nested
    class DeleteAnswerTest {


        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformDeleteUsingRequest() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
                )
            ).thenReturn(new ResponseEntity<>(DELETE_ANSWER_RESPONSE_DTO, HttpStatus.OK));
            final var captor = ArgumentCaptor.forClass(HttpEntity.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            verify(restTemplate).exchange(anyString(), any(HttpMethod.class), captor.capture(), any(ParameterizedTypeReference.class));

            assertEquals(DELETE_ANSWER_REQUEST_DTO, captor.getValue().getBody());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
                )
            ).thenReturn(new ResponseEntity<>(DELETE_ANSWER_RESPONSE_DTO, HttpStatus.OK));
            final var response = csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldSetHttpMethod() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
                )
            ).thenReturn(new ResponseEntity<>(DELETE_ANSWER_RESPONSE_DTO, HttpStatus.OK));
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            verify(restTemplate).exchange(anyString(), captor.capture(), any(HttpEntity.class), any(ParameterizedTypeReference.class));

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
                )
            ).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(DELETE_ANSWER_RESPONSE_DTO, HttpStatus.OK));

            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);
            verify(restTemplate).exchange(captor.capture(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class));

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/deleteanswer", captor.getValue());
        }
    }

    @Nested
    class SaveMessageTest {


        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SaveMessageRequestDTO.class);

            csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SAVE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/save", captor.getValue());
        }
    }

    @Nested
    class SaveAnswerTest {


        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_ANSWER_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SaveAnswerRequestDTO.class);

            csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SAVE_ANSWER_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_ANSWER_RESPONSE_DTO);
            final var response = csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SAVE_ANSWER_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/saveanswer", captor.getValue());
        }
    }

    @Nested
    class SendMessageTest {


        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_MESSAGE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SendMessageRequestDTO.class);

            csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SEND_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_MESSAGE_RESPONSE_DTO);
            final var response = csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_MESSAGE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/send", captor.getValue());
        }
    }

    @Nested
    class GetQuestionsForUnit {

        @BeforeEach
        void setup() {
            CERTIFICATE_DTO.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .build());

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/unit/messages";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetUnitQuestionsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldThrowIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetUnitQuestionsResponseDTO.class);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.listQuestionsForUnit(GET_QUESTIONS_REQUEST)
            );
        }

        @Test
        void shouldPreformPostUsingRequest() {

            final var response = GetUnitQuestionsResponseDTO.builder()
                    .questions(List.of())
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitQuestionsResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(GetUnitQuestionsRequestDTO.class);

            csIntegrationService.listQuestionsForUnit(GET_QUESTIONS_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_QUESTIONS_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnConvertedListItems() {
            when(listQuestionConverter.convert(Optional.of(CERTIFICATE_DTO), QUESTION_DTO)).thenReturn(ARENDE_LIST_ITEM);

            final var response = GetUnitQuestionsResponseDTO.builder()
                    .questions(List.of(QUESTION_DTO))
                    .certificates(List.of(CERTIFICATE_DTO))
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitQuestionsResponseDTO.class);

            final var actualResponse = csIntegrationService.listQuestionsForUnit(GET_QUESTIONS_REQUEST);

            assertEquals(List.of(ARENDE_LIST_ITEM), actualResponse);
        }

        @Test
        void shouldSetUrlCorrect() {
            when(listQuestionConverter.convert(Optional.of(CERTIFICATE_DTO), QUESTION_DTO)).thenReturn(ARENDE_LIST_ITEM);
            //when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(GET_QUESTIONS_RESPONSE);

            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var response = GetUnitQuestionsResponseDTO.builder()
                    .questions(List.of(QUESTION_DTO))
                    .certificates(List.of(CERTIFICATE_DTO))
                    .build();

            doReturn(response).when(responseSpec).body(GetUnitQuestionsResponseDTO.class);

            csIntegrationService.listQuestionsForUnit(GET_QUESTIONS_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/unit/messages", captor.getValue());

        }
    }

    @Nested
    class SendAnswerTest {


        private static final String MESSAGE_ID = "messageId";

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_ANSWER_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(SendAnswerRequestDTO.class);

            csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(SEND_ANSWER_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_ANSWER_RESPONSE_DTO);
            final var response = csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(SEND_ANSWER_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/sendanswer", captor.getValue());
        }
    }

    @Nested
    class ForwardCertificateTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(FORWARD_CERTIFICATE_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(ForwardCertificateRequestDTO.class);

            csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(FORWARD_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(FORWARD_CERTIFICATE_RESPONSE_DTO);
            final var response = csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(FORWARD_CERTIFICATE_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate/" + ID + "/forward", captor.getValue());
        }
    }

    @Nested
    class GetCertificateEventsTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(GetCertificateEventsRequestDTO.class);

            csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CERTIFICATE_EVENTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO);
            final var response = csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);

            assertEquals(EVENTS.size(), response.length);
            assertEquals(EVENTS.get(0), response[0]);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate/" + ID + "/events", captor.getValue());
        }
    }

    @Nested
    class GetPatientCertificatesWithQATest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(CertificatesWithQARequestDTO.class);

            csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnString() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO);
            final var response = csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);

            assertEquals(LIST, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/internalapi/certificate/qa", captor.getValue());
        }
    }

    @Nested
    class LockDraftsTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(LockDraftsRequestDTO.class);

            csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(LOCK_OLD_DRAFTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificates() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO);
            final var response = csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);

            assertEquals(List.of(CERTIFICATE), response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/internalapi/certificate/lock", captor.getValue());
        }
    }

    @Nested
    class GetStatisticsTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(STATISTICS_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(UnitStatisticsRequestDTO.class);

            csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(STATISTICS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnUserStatistics() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(STATISTICS_RESPONSE_DTO);
            final var response = csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);

            assertEquals(USER_STATISTICS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(STATISTICS_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/unit/certificates/statistics", captor.getValue());
        }
    }

    @Nested
    class MarkCertificateReadyForSignTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(READY_FOR_SIGN_RESPONSE_DTO);
            final var captor = ArgumentCaptor.forClass(ReadyForSignRequestDTO.class);

            csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(READY_FOR_SIGN_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(READY_FOR_SIGN_RESPONSE_DTO);
            final var response = csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(READY_FOR_SIGN_RESPONSE_DTO);
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);

            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/certificate/" + ID + "/readyForSign", captor.getValue());
        }
    }
}
