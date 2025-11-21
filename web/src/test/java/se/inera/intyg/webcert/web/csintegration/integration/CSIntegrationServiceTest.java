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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExternalTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceGetCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesWithQARequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesWithQAResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CitizenCertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateFromTemplateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteAnswerRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteAnswerResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DisposeObsoleteDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DisposeObsoleteDraftsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ForwardCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCandidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCandidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateEventsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateEventsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateFromMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateFromMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageInternalResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetListCertificatesResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalIgnoreModelRulesDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.InternalCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ListObsoleteDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ListObsoleteDraftsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockDraftsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReadyForSignRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReadyForSignResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewLegacyCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReplaceCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RevokeCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveAnswerRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveAnswerResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendAnswerRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendAnswerResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SickLeaveCertificateDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateWithoutSignatureRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.StatisticsForUnitDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UnitStatisticsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UnitStatisticsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UpdateWithCandidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UpdateWithCandidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateResponseDTO;
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
    private static final RenewLegacyCertificateRequestDTO RENEWLEGACY_CERTIFICATE_REQUEST = RenewLegacyCertificateRequestDTO.builder()
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
    private static final DisposeObsoleteDraftsResponseDTO DELETE_STALE_DRAFTS_RESPONSE_DTO = DisposeObsoleteDraftsResponseDTO.builder()
        .certificate(CERTIFICATE).build();
    private static final DisposeObsoleteDraftsRequestDTO DELETE_STALE_DRAFTS_REQUEST_DTO = DisposeObsoleteDraftsRequestDTO.builder().build();
    private static final ListObsoleteDraftsRequestDTO LIST_STALE_DRAFTS_REQUEST_DTO = ListObsoleteDraftsRequestDTO.builder().build();
    private static final ListObsoleteDraftsResponseDTO LIST_STALE_DRAFTS_RESPONSE_DTO = ListObsoleteDraftsResponseDTO.builder()
        .certificateIds(List.of(CERTIFICATE_ID)).build();
    private static final Map<String, StatisticsForUnitDTO> USER_STATISTICS = Map.of("unit", StatisticsForUnitDTO.builder().build());
    private static final UnitStatisticsResponseDTO STATISTICS_RESPONSE_DTO = UnitStatisticsResponseDTO.builder()
        .unitStatistics(USER_STATISTICS)
        .build();
    private static final UnitStatisticsRequestDTO STATISTICS_REQUEST_DTO = UnitStatisticsRequestDTO.builder().build();
    private static final ReadyForSignRequestDTO READY_FOR_SIGN_REQUEST_DTO = ReadyForSignRequestDTO.builder().build();
    private static final SaveCertificateRequestDTO SAVE_CERTIFICATE_REQUEST_DTO = SaveCertificateRequestDTO.builder()
        .certificate(CERTIFICATE).build();
    private static final SaveCertificateResponseDTO SAVE_CERTIFICATE_RESPONSE_DTO = SaveCertificateResponseDTO.builder()
        .certificate(CERTIFICATE).build();
    private static final SaveCertificateResponseDTO SAVE_CERTIFICATE_NULL_RESPONSE_DTO = SaveCertificateResponseDTO.builder()
        .certificate(null).build();

    @Mock
    private RestClient restClient;

    @Mock
    private CertificateTypeInfoConverter certificateTypeInfoConverter;

    @Mock
    private ListIntygEntryConverter listIntygEntryConverter;

    @Mock
    private ListQuestionConverter listQuestionConverter;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

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

        @Test
        void shouldUseCertificateTypeIdIfSpecifiedForType() throws ModuleNotFoundException {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            final var wcType = "wcType";
            final var moduleEntryPoint = mock(ModuleEntryPoint.class);
            when(moduleEntryPoint.certificateServiceTypeId()).thenReturn("type");
            when(intygModuleRegistry.moduleExists(wcType)).thenReturn(true);
            when(intygModuleRegistry.getModuleEntryPoint(wcType)).thenReturn(moduleEntryPoint);

            csIntegrationService.certificateTypeExists(wcType);
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

            final String uri = "baseUrl/api/certificate/ID";
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

            csIntegrationService.getCertificate(ID, GET_CERTIFICATE_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + ID, captor.getValue());
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
    class PlaceholderCertificateExistsTests {

        @BeforeEach
        void setUp() {

            responseSpec = mock(RestClient.ResponseSpec.class);
            requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

            final String uri = "baseUrl/internalapi/certificate/placeholder/ID/exists";
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
                IllegalStateException.class, () -> csIntegrationService.placeholderCertificateExists(ID)
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
                final var response = csIntegrationService.placeholderCertificateExists("ID");

                assertEquals(expectedResponse.getExists(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.placeholderCertificateExists("ID");
                verify(requestHeadersUriSpec).uri(captor.capture());

                assertEquals("baseUrl/internalapi/certificate/placeholder/ID/exists", captor.getValue());
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

        @Nested
        class WithResponse {

            @Test
            void shouldPreformPostUsingRequest() {
                doReturn(SAVE_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);

                final var captor = ArgumentCaptor.forClass(SaveCertificateRequestDTO.class);

                csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(SAVE_CERTIFICATE_REQUEST_DTO, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                doReturn(SAVE_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);

                final var response = csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldThrowExceptionIfResponseIsNull() {

                doReturn(SAVE_CERTIFICATE_NULL_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);

                assertThrows(IllegalStateException.class,
                    () -> csIntegrationService.saveCertificate(SAVE_CERTIFICATE_REQUEST_DTO)
                );
            }

            @Test
            void shouldSetUrlCorrect() {
                doReturn(SAVE_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SaveCertificateResponseDTO.class);

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

    @Nested
    class GetPatientCertificates {

        @BeforeEach
        void setup() {
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
            when(listIntygEntryConverter.convert(CERTIFICATE)).thenReturn(CONVERTED_CERTIFICATE);

            final var response = GetListCertificatesResponseDTO.builder()
                .certificates(List.of(CERTIFICATE))
                .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            final var actualResponse = csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), actualResponse);
        }

        @Test
        void shouldThrowExceptionIfCertificatesForPatientResponseIsNull() {
            final var response = GetListCertificatesResponseDTO.builder()
                .certificates(null)
                .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);
            assertThrows(IllegalStateException.class, () -> csIntegrationService.listCertificatesForPatient(PATIENT_LIST_REQUEST));
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
        }

        @Test
        void shouldPreformPostUsingRequest() {

            when(listIntygEntryConverter.convert(CERTIFICATE)).thenReturn(CONVERTED_CERTIFICATE);

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
            when(listIntygEntryConverter.convert(CERTIFICATE)).thenReturn(CONVERTED_CERTIFICATE);

            final var response = GetListCertificatesResponseDTO.builder()
                .certificates(List.of(CERTIFICATE))
                .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);

            final var actualResponse = csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST);

            assertEquals(List.of(CONVERTED_CERTIFICATE), actualResponse);
        }

        @Test
        void shouldThrowExceptionIfCertificatesForUnitResponseIsNull() {
            final var response = GetListCertificatesResponseDTO.builder()
                .certificates(null)
                .build();

            doReturn(response).when(responseSpec).body(GetListCertificatesResponseDTO.class);
            assertThrows(IllegalStateException.class, () -> csIntegrationService.listCertificatesForUnit(UNIT_LIST_REQUEST));
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
            final var captor = ArgumentCaptor.forClass(String.class);

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);

            csIntegrationService.signCertificate(SIGN_CERTIFICATE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(requestBodyUriSpec).uri(captor.capture());

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
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/signwithoutsignature/0";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SignCertificateWithoutSignatureRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(SignCertificateWithoutSignatureRequestDTO.class);

            csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificateFromSignCertificateResponseDTO() {

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);

            final var response = csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO,
                CERTIFICATE_ID, VERSION);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {

            final var captor = ArgumentCaptor.forClass(String.class);

            doReturn(SIGN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(SignCertificateResponseDTO.class);

            csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID, VERSION);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/signwithoutsignature/" + VERSION, captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(SignCertificateResponseDTO.class);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID,
                    VERSION));
        }
    }

    @Nested
    class PrintCertificate {

        @BeforeEach
        void setup() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/pdf";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(PrintCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
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
                doReturn(PRINT_RESPONSE).when(responseSpec).body(PrintCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(PrintCertificateRequestDTO.class);

                csIntegrationService.printCertificate(ID, PRINT_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

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
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.printCertificate(ID, PRINT_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID/pdf", captor.getValue());
            }
        }
    }

    @Nested
    class SendCertificate {

        @BeforeEach
        void setup() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/send";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SendCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
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
                doReturn(SEND_RESPONSE).when(responseSpec).body(SendCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);

                csIntegrationService.sendCertificate(ID, SEND_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(SEND_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.sendCertificate(ID, SEND_REQUEST);

                assertEquals(SEND_RESPONSE.getCertificate(), response);
            }

            @Test
            void shouldSetUrlCorrect() {
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.sendCertificate(ID, SEND_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID/send", captor.getValue());
            }
        }
    }

    @Nested
    class RevokeCertificate {

        @BeforeEach
        void setup() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/revoke";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(RevokeCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
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
                doReturn(REVOKE_RESPONSE).when(responseSpec).body(RevokeCertificateResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(RevokeCertificateRequestDTO.class);

                csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(REVOKE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);

                assertEquals(REVOKE_RESPONSE.getCertificate(), response);
            }

            @Test
            void shouldSetUrlCorrect() {

                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/ID/revoke", captor.getValue());
            }
        }
    }

    @Nested
    class GetCitizenCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/citizen/certificate/ID";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCitizenCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificateResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(GetCitizenCertificateRequestDTO.class);

            csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnGetCitizenCertificateResponseDTO() {

            doReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificateResponseDTO.class);
            final var response = csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, ID);

            assertEquals(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO, response);
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(GET_CITIZEN_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificateResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCitizenCertificate(GET_CITIZEN_CERTIFICATE_REQUEST_DTO, ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/citizen/certificate/" + ID, captor.getValue());
        }
    }

    @Nested
    class GetCitizenCertificatePdf {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/citizen/certificate/ID/print";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCitizenCertificatePdfRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificatePdfResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(GetCitizenCertificatePdfRequestDTO.class);

            csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnGetCitizenCertificatePdfResponseDTO() {

            doReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificatePdfResponseDTO.class);

            final var response = csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, ID);

            assertEquals(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO, response);
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(GET_CITIZEN_CERTIFICATE_PDF_RESPONSE_DTO).when(responseSpec).body(GetCitizenCertificatePdfResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCitizenCertificatePdf(GET_CITIZEN_CERTIFICATE_PDF_REQUEST_DTO, ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/citizen/certificate/" + ID + "/print", captor.getValue());
        }
    }

    @Nested
    class AnswerComplementCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/answerComplement";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(AnswerComplementRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(AnswerComplementResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST)
            );
        }

        @Nested
        class WithResponse {

            @BeforeEach
            void setUp() {
                doReturn(ANSWER_COMPLEMENT_RESPONSE).when(responseSpec).body(AnswerComplementResponseDTO.class);
            }

            @Test
            void shouldPreformPostUsingRequest() {
                final var captor = ArgumentCaptor.forClass(AnswerComplementRequestDTO.class);

                csIntegrationService.answerComplementOnCertificate(CERTIFICATE_ID, ANSWER_COMPLEMENT_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

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
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/certificateId/answerComplement", captor.getValue());
            }
        }
    }

    @Nested
    class PostMessage {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(IncomingMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(IncomingMessageRequestDTO.class);

            csIntegrationService.postMessage(INCOMING_MESSAGE_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(INCOMING_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldSetUrlCorrect() {

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.postMessage(INCOMING_MESSAGE_REQUEST_DTO);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message", captor.getValue());
        }
    }

    @Nested
    class GetCertificateQuestions {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/certificateId";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCertificateMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(GetCertificateMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(GetCertificateMessageRequestDTO.class);

            csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestions() {

            doReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(
                GetCertificateMessageResponseDTO.class);
            final var response = csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);

            assertEquals(QUESTIONS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(GetCertificateMessageResponseDTO.class);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(GET_CERTIFICATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(GetCertificateMessageResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getQuestions(GET_CERTIFICATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + CERTIFICATE_ID, captor.getValue());
        }
    }

    @Nested
    class HandleMessageTests {

        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/handle";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(HandleMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(HANDLE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(HandleMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(HandleMessageRequestDTO.class);

            csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(HANDLE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestions() {

            doReturn(HANDLE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(HandleMessageResponseDTO.class);
            final var response = csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(HandleMessageResponseDTO.class);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(HANDLE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(HandleMessageResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.handleMessage(HANDLE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/handle", captor.getValue());
        }
    }

    @Nested
    class GetCertificateFromMessageRequestTests {

        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/certificate";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCertificateFromMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO).when(responseSpec).body(GetCertificateFromMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(GetCertificateFromMessageRequestDTO.class);

            csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO).when(responseSpec).body(GetCertificateFromMessageResponseDTO.class);
            final var response = csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetCertificateFromMessageResponseDTO.class);
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            doReturn(GET_CERTIFICTE_FROM_MESSAGE_RESPONSE_DTO).when(responseSpec).body(GetCertificateFromMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificate(GET_CERTIFICTE_FROM_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/certificate", captor.getValue());
        }
    }

    @Nested
    class GetCertificateQuestionsInternal {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/message/certificateId";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldReturnQuestions() {

            doReturn(GET_CERTIFICATE_MESSAGE_INTERNAL_RESPONSE_DTO).when(responseSpec).body(GetCertificateMessageInternalResponseDTO.class);
            final var response = csIntegrationService.getQuestions(CERTIFICATE_ID);

            assertEquals(QUESTIONS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getQuestions(CERTIFICATE_ID));
        }

        @Test
        void shouldSetUrlCorrect() {

            doReturn(GET_CERTIFICATE_MESSAGE_INTERNAL_RESPONSE_DTO).when(responseSpec).body(GetCertificateMessageInternalResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getQuestions(CERTIFICATE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/message/" + CERTIFICATE_ID, captor.getValue());
        }
    }

    @Nested
    class GetCertificateInternal {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/certificate/ID";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(GET_RESPONSE).when(responseSpec).body(CertificateServiceGetCertificateResponseDTO.class);
            final var response = csIntegrationService.getInternalCertificate(ID);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getInternalCertificate(ID)
            );
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(GET_RESPONSE).when(responseSpec).body(CertificateServiceGetCertificateResponseDTO.class);

            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getInternalCertificate(ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/certificate/" + ID, captor.getValue());
        }
    }

    @Nested
    class GetCertificateXmlInternal {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/certificate/ID/xml";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldReturnXml() {
            doReturn(INTERAL_CERTIFICATE_XML_RESPONSE_DTO).when(responseSpec).body(InternalCertificateXmlResponseDTO.class);
            final var response = csIntegrationService.getInternalCertificateXml(ID);

            assertEquals(XML_DATA, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getInternalCertificateXml(ID)
            );
        }

        @Test
        void shouldSetUrlCorrect() {

            doReturn(INTERAL_CERTIFICATE_XML_RESPONSE_DTO).when(responseSpec).body(InternalCertificateXmlResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getInternalCertificateXml(ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/certificate/ID/xml", captor.getValue());
        }
    }

    @Nested
    class DeleteMessage {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/delete";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(DeleteMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldSetHttpMethod() {

            doReturn(new ResponseEntity<>(HttpStatus.OK)).when(responseSpec).body(Void.class);

            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            csIntegrationService.deleteMessage(MESSAGE_ID, DELETE_MESSAGE_REQUEST_DTO);

            verify(restClient).method(captor.capture());

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldSetUrlCorrect() {
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.deleteMessage(
                "messageId",
                DELETE_MESSAGE_REQUEST_DTO
            );

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/messageId/delete", captor.getValue());
        }
    }

    @Nested
    class CreateMessageTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/certificateId/create";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CreateMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(CREATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(CreateMessageResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(CreateMessageRequestDTO.class);

            csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(CREATE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {

            doReturn(CREATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(CreateMessageResponseDTO.class);
            final var response = csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID));
        }

        @Test
        void shouldSetUrlCorrect() {

            doReturn(CREATE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(CreateMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.createMessage(CREATE_MESSAGE_REQUEST_DTO, CERTIFICATE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + CERTIFICATE_ID + "/create", captor.getValue());
        }
    }

    @Nested
    class DeleteAnswerTest {

        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/deleteanswer";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(DeleteAnswerRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformDeleteUsingRequest() {

            doReturn(DELETE_ANSWER_RESPONSE_DTO).when(responseSpec).body(DeleteAnswerResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(DeleteAnswerRequestDTO.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(DELETE_ANSWER_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {

            doReturn(DELETE_ANSWER_RESPONSE_DTO).when(responseSpec).body(DeleteAnswerResponseDTO.class);
            final var response = csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldSetHttpMethod() {

            doReturn(DELETE_ANSWER_RESPONSE_DTO).when(responseSpec).body(DeleteAnswerResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(HttpMethod.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);

            verify(restClient).method(captor.capture());

            assertEquals(HttpMethod.DELETE, captor.getValue());
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO));
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(DELETE_ANSWER_RESPONSE_DTO).when(responseSpec).body(DeleteAnswerResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.deleteAnswer(MESSAGE_ID, DELETE_ANSWER_REQUEST_DTO);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/deleteanswer", captor.getValue());
        }
    }

    @Nested
    class SaveMessageTest {

        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/save";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SaveMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SAVE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SaveMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(SaveMessageRequestDTO.class);

            csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SAVE_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {

            doReturn(SAVE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SaveMessageResponseDTO.class);
            final var response = csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(SAVE_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SaveMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.saveMessage(SAVE_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/save", captor.getValue());
        }
    }

    @Nested
    class SaveAnswerTest {


        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/saveanswer";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SaveAnswerRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SAVE_ANSWER_RESPONSE_DTO).when(responseSpec).body(SaveAnswerResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(SaveAnswerRequestDTO.class);

            csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SAVE_ANSWER_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {

            doReturn(SAVE_ANSWER_RESPONSE_DTO).when(responseSpec).body(SaveAnswerResponseDTO.class);
            final var response = csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {

            doReturn(SAVE_ANSWER_RESPONSE_DTO).when(responseSpec).body(SaveAnswerResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.saveAnswer(SAVE_ANSWER_REQUEST_DTO, MESSAGE_ID);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/saveanswer", captor.getValue());
        }
    }

    @Nested
    class SendMessageTest {

        private static final String MESSAGE_ID = "messageId";

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/send";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SendMessageRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SEND_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SendMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(SendMessageRequestDTO.class);

            csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SEND_MESSAGE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {

            doReturn(SEND_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SendMessageResponseDTO.class);
            final var response = csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            doReturn(SEND_MESSAGE_RESPONSE_DTO).when(responseSpec).body(SendMessageResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.sendMessage(SEND_MESSAGE_REQUEST_DTO, MESSAGE_ID);

            verify(requestBodyUriSpec).uri(captor.capture());

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

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/message/messageId/sendanswer";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SendAnswerRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(SEND_ANSWER_RESPONSE_DTO).when(responseSpec).body(SendAnswerResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(SendAnswerRequestDTO.class);

            csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(SEND_ANSWER_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnQuestion() {
            doReturn(SEND_ANSWER_RESPONSE_DTO).when(responseSpec).body(SendAnswerResponseDTO.class);
            final var response = csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);

            assertEquals(QUESTION, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID));
        }


        @Test
        void shouldSetUrlCorrect() {
            doReturn(SEND_ANSWER_RESPONSE_DTO).when(responseSpec).body(SendAnswerResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.sendAnswer(SEND_ANSWER_REQUEST_DTO, MESSAGE_ID);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/message/" + MESSAGE_ID + "/sendanswer", captor.getValue());
        }
    }

    @Nested
    class ForwardCertificateTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/forward";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(ForwardCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(FORWARD_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(ForwardCertificateResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(ForwardCertificateRequestDTO.class);

            csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(FORWARD_CERTIFICATE_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(FORWARD_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(ForwardCertificateResponseDTO.class);

            final var response = csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(FORWARD_CERTIFICATE_RESPONSE_DTO).when(responseSpec).body(ForwardCertificateResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.forwardCertificate(ID, FORWARD_CERTIFICATE_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + ID + "/forward", captor.getValue());
        }
    }

    @Nested
    class GetCertificateEventsTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/events";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCertificateEventsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO).when(responseSpec).body(GetCertificateEventsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(GetCertificateEventsRequestDTO.class);

            csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_CERTIFICATE_EVENTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO).when(responseSpec).body(GetCertificateEventsResponseDTO.class);
            final var response = csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);

            assertEquals(EVENTS.size(), response.length);
            assertEquals(EVENTS.getFirst(), response[0]);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(GET_CERTIFICATE_EVENTS_RESPONSE_DTO).when(responseSpec).body(GetCertificateEventsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificateEvents(ID, GET_CERTIFICATE_EVENTS_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + ID + "/events", captor.getValue());
        }
    }

    @Nested
    class GetPatientCertificatesWithQATest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/certificate/qa";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CertificatesWithQARequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO).when(responseSpec).body(CertificatesWithQAResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(CertificatesWithQARequestDTO.class);

            csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnString() {

            doReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO).when(responseSpec).body(CertificatesWithQAResponseDTO.class);
            final var response = csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);

            assertEquals(LIST, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(GET_PATIENT_CERTIFICATES_WITH_QA_RESPONSE_DTO).when(responseSpec).body(CertificatesWithQAResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getCertificatesWithQA(GET_PATIENT_CERTIFICATES_WITH_QA_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/certificate/qa", captor.getValue());
        }
    }

    @Nested
    class LockDraftsTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/certificate/lock";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(LockDraftsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO).when(responseSpec).body(LockDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(LockDraftsRequestDTO.class);

            csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(LOCK_OLD_DRAFTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificates() {

            doReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO).when(responseSpec).body(LockDraftsResponseDTO.class);
            final var response = csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);

            assertEquals(List.of(CERTIFICATE), response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(LOCK_OLD_DRAFTS_RESPONSE_DTO).when(responseSpec).body(LockDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.lockDrafts(LOCK_OLD_DRAFTS_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/certificate/lock", captor.getValue());
        }
    }

    @Nested
    class ListStaleDraftsTest {

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/draft/list";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(ListObsoleteDraftsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPerformPostUsingRequest() {
            doReturn(LIST_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(ListObsoleteDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(ListObsoleteDraftsRequestDTO.class);

            csIntegrationService.listObsoleteDrafts(LIST_STALE_DRAFTS_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(LIST_STALE_DRAFTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificateIds() {
            doReturn(LIST_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(ListObsoleteDraftsResponseDTO.class);
            final var response = csIntegrationService.listObsoleteDrafts(LIST_STALE_DRAFTS_REQUEST_DTO);

            assertEquals(List.of(CERTIFICATE_ID), response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.listObsoleteDrafts(LIST_STALE_DRAFTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(LIST_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(ListObsoleteDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.listObsoleteDrafts(LIST_STALE_DRAFTS_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/draft/list", captor.getValue());
        }
    }

    @Nested
    class DeleteStaleDraftsTest {

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final var uri = "baseUrl/internalapi/draft";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(DisposeObsoleteDraftsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPerformPostUsingRequest() {
            doReturn(DELETE_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(DisposeObsoleteDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(DisposeObsoleteDraftsRequestDTO.class);

            csIntegrationService.disposeObsoleteDraft(DELETE_STALE_DRAFTS_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(DELETE_STALE_DRAFTS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(DELETE_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(DisposeObsoleteDraftsResponseDTO.class);
            final var response = csIntegrationService.disposeObsoleteDraft(DELETE_STALE_DRAFTS_REQUEST_DTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.disposeObsoleteDraft(DELETE_STALE_DRAFTS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(DELETE_STALE_DRAFTS_RESPONSE_DTO).when(responseSpec).body(DisposeObsoleteDraftsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.disposeObsoleteDraft(DELETE_STALE_DRAFTS_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/draft", captor.getValue());
        }
    }

    @Nested
    class GetStatisticsTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/unit/certificates/statistics";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(UnitStatisticsRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(STATISTICS_RESPONSE_DTO).when(responseSpec).body(UnitStatisticsResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(UnitStatisticsRequestDTO.class);

            csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(STATISTICS_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnUserStatistics() {
            doReturn(STATISTICS_RESPONSE_DTO).when(responseSpec).body(UnitStatisticsResponseDTO.class);
            final var response = csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);

            assertEquals(USER_STATISTICS, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {
            doReturn(STATISTICS_RESPONSE_DTO).when(responseSpec).body(UnitStatisticsResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getStatistics(STATISTICS_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/unit/certificates/statistics", captor.getValue());
        }
    }

    @Nested
    class MarkCertificateReadyForSignTest {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/ID/readyForSign";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(ReadyForSignRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        }

        @Test
        void shouldPreformPostUsingRequest() {

            doReturn(READY_FOR_SIGN_RESPONSE_DTO).when(responseSpec).body(ReadyForSignResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(ReadyForSignRequestDTO.class);

            csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(READY_FOR_SIGN_REQUEST_DTO, captor.getValue());
        }

        @Test
        void shouldReturnCertificate() {
            doReturn(READY_FOR_SIGN_RESPONSE_DTO).when(responseSpec).body(ReadyForSignResponseDTO.class);
            final var response = csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO));
        }

        @Test
        void shouldSetUrlCorrect() {

            doReturn(READY_FOR_SIGN_RESPONSE_DTO).when(responseSpec).body(ReadyForSignResponseDTO.class);
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.markCertificateReadyForSign(ID, READY_FOR_SIGN_REQUEST_DTO);

            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + ID + "/readyForSign", captor.getValue());
        }
    }

    @Nested
    class RenewLegacyCertificate {

        @BeforeEach
        void setUp() {

            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/certificateId/renew/external";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(RenewLegacyCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldThrowExceptionIfResponseIsNull() {

            doReturn(null).when(responseSpec).body(RenewCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.renewLegacyCertificate(CERTIFICATE_ID, RENEWLEGACY_CERTIFICATE_REQUEST)
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
                final var captor = ArgumentCaptor.forClass(RenewLegacyCertificateRequestDTO.class);

                csIntegrationService.renewLegacyCertificate(CERTIFICATE_ID, RENEWLEGACY_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).body(captor.capture());

                assertEquals(RENEWLEGACY_CERTIFICATE_REQUEST, captor.getValue());
            }

            @Test
            void shouldReturnCertificate() {
                final var response = csIntegrationService.renewLegacyCertificate(CERTIFICATE_ID, RENEWLEGACY_CERTIFICATE_REQUEST);

                assertEquals(CERTIFICATE, response);
            }

            @Test
            void shouldSetUrlCorrect() {
                ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
                final var captor = ArgumentCaptor.forClass(String.class);

                csIntegrationService.renewLegacyCertificate(CERTIFICATE_ID, RENEWLEGACY_CERTIFICATE_REQUEST);
                verify(requestBodyUriSpec).uri(captor.capture());

                assertEquals("baseUrl/api/certificate/certificateId/renew/external", captor.getValue());
            }
        }
    }

    @Nested
    class CreateCertificateFromTemplate {

        private static final String CERTIFICATE_ID = "certificateId";
        private static final CreateCertificateFromTemplateRequestDTO CREATE_FROM_TEMPLATE_REQUEST = CreateCertificateFromTemplateRequestDTO.builder()
            .build();
        private static final CreateCertificateFromTemplateResponseDTO CREATE_FROM_TEMPLATE_RESPONSE = CreateCertificateFromTemplateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/" + CERTIFICATE_ID + "/draft";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(CreateCertificateFromTemplateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPerformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(CreateCertificateFromTemplateRequestDTO.class);
            doReturn(CREATE_FROM_TEMPLATE_RESPONSE).when(responseSpec).body(CreateCertificateFromTemplateResponseDTO.class);

            csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, CREATE_FROM_TEMPLATE_REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(CREATE_FROM_TEMPLATE_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnCreateCertificateFromTemplateResponseDTO() {
            doReturn(CREATE_FROM_TEMPLATE_RESPONSE).when(responseSpec).body(CreateCertificateFromTemplateResponseDTO.class);

            final var response = csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, CREATE_FROM_TEMPLATE_REQUEST);

            assertEquals(CREATE_FROM_TEMPLATE_RESPONSE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            final var captor = ArgumentCaptor.forClass(String.class);
            doReturn(CREATE_FROM_TEMPLATE_RESPONSE).when(responseSpec).body(CreateCertificateFromTemplateResponseDTO.class);

            csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, CREATE_FROM_TEMPLATE_REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/draft", captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(CreateCertificateFromTemplateResponseDTO.class);

            final var response = csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, CREATE_FROM_TEMPLATE_REQUEST);

            assertNull(response);
        }
    }

    @Nested
    class GetCandidateCertificate {

        private static final String CERTIFICATE_ID = "certificateId";
        private static final GetCandidateCertificateRequestDTO REQUEST = GetCandidateCertificateRequestDTO.builder().build();
        private static final GetCandidateCertificateResponseDTO RESPONSE = GetCandidateCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/" + CERTIFICATE_ID + "/candidate";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCandidateCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPerformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(GetCandidateCertificateRequestDTO.class);
            doReturn(RESPONSE).when(responseSpec).body(GetCandidateCertificateResponseDTO.class);

            csIntegrationService.getCandidateCertificate(CERTIFICATE_ID, REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnGetCandidateCertificateResponseDTO() {
            doReturn(RESPONSE).when(responseSpec).body(GetCandidateCertificateResponseDTO.class);

            final var response = csIntegrationService.getCandidateCertificate(CERTIFICATE_ID, REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            final var captor = ArgumentCaptor.forClass(String.class);
            doReturn(RESPONSE).when(responseSpec).body(GetCandidateCertificateResponseDTO.class);

            csIntegrationService.getCandidateCertificate(CERTIFICATE_ID, REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/candidate", captor.getValue());
        }

        @Test
        void shouldReturnNullIfResponseIsNull() {
            doReturn(GetCandidateCertificateResponseDTO.builder().build())
                .when(responseSpec).body(GetCandidateCertificateResponseDTO.class);

            final var response = csIntegrationService.getCandidateCertificate(CERTIFICATE_ID, REQUEST);

            assertNull(response);
        }
    }

    @Nested
    class UpdateWithCandidateCertificate {

        private static final String CERTIFICATE_ID = "certificateId";
        private static final String CANDIDATE_CERTIFICATE_ID = "candidateCertificateId";
        private static final UpdateWithCandidateCertificateRequestDTO REQUEST = UpdateWithCandidateCertificateRequestDTO.builder().build();
        private static final UpdateWithCandidateCertificateResponseDTO RESPONSE = UpdateWithCandidateCertificateResponseDTO.builder()
            .certificate(CERTIFICATE)
            .build();

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/api/certificate/" + CERTIFICATE_ID + "/candidate/" + CANDIDATE_CERTIFICATE_ID;
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(UpdateWithCandidateCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldPerformPostUsingRequest() {
            final var captor = ArgumentCaptor.forClass(UpdateWithCandidateCertificateRequestDTO.class);
            doReturn(RESPONSE).when(responseSpec).body(UpdateWithCandidateCertificateResponseDTO.class);

            csIntegrationService.updateWithCandidateCertificate(CERTIFICATE_ID, CANDIDATE_CERTIFICATE_ID, REQUEST);
            verify(requestBodyUriSpec).body(captor.capture());

            assertEquals(REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnUpdateWithCandidateCertificateResponseDTO() {
            doReturn(RESPONSE).when(responseSpec).body(UpdateWithCandidateCertificateResponseDTO.class);

            final var response = csIntegrationService.updateWithCandidateCertificate(CERTIFICATE_ID, CANDIDATE_CERTIFICATE_ID, REQUEST);

            assertEquals(CERTIFICATE, response);
        }

        @Test
        void shouldSetUrlCorrect() {
            final var captor = ArgumentCaptor.forClass(String.class);
            doReturn(RESPONSE).when(responseSpec).body(UpdateWithCandidateCertificateResponseDTO.class);

            csIntegrationService.updateWithCandidateCertificate(CERTIFICATE_ID, CANDIDATE_CERTIFICATE_ID, REQUEST);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/api/certificate/" + CERTIFICATE_ID + "/candidate/" + CANDIDATE_CERTIFICATE_ID, captor.getValue());
        }

        @Test
        void shouldThrowIfResponseIsNull() {
            doReturn(UpdateWithCandidateCertificateResponseDTO.builder().build())
                .when(responseSpec).body(UpdateWithCandidateCertificateResponseDTO.class);

            assertThrows(IllegalStateException.class, () ->
                csIntegrationService.updateWithCandidateCertificate(CERTIFICATE_ID, CANDIDATE_CERTIFICATE_ID, REQUEST)
            );

        }
    }

    @Nested
    class GetSickLeaveCertificate {

        private static final String CERTIFICATE_ID = "certificate-id-123";
        private RestClient.RequestBodyUriSpec requestBodyUriSpec;
        private RestClient.ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            final String uri = "baseUrl/internalapi/certificate/" + CERTIFICATE_ID + "/sickleave";
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetSickLeaveCertificateInternalIgnoreModelRulesDTO.class)))
                .thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldReturnEmptyOptionalWhenResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            final var result = csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenCertificateIsNotAvailable() {
            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(false)
                .sickLeaveCertificate(SickLeaveCertificateDTO.builder()
                    .id(CERTIFICATE_ID)
                    .diagnoseCode("F438A")
                    .build())
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            final var result = csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenSickLeaveCertificateIsNull() {
            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(true)
                .sickLeaveCertificate(null)
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            final var result = csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnResponseWhenCertificateIsAvailable() {
            final var sickLeaveCert = SickLeaveCertificateDTO.builder()
                .id(CERTIFICATE_ID)
                .diagnoseCode("F438A")
                .build();

            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(true)
                .sickLeaveCertificate(sickLeaveCert)
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            final var result = csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);

            assertTrue(result.isPresent());
            assertEquals(response, result.get());
        }

        @Test
        void shouldPerformPostWithCorrectUrl() {
            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(true)
                .sickLeaveCertificate(SickLeaveCertificateDTO.builder().build())
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);
            verify(requestBodyUriSpec).uri(captor.capture());

            assertEquals("baseUrl/internalapi/certificate/" + CERTIFICATE_ID + "/sickleave", captor.getValue());
        }

        @Test
        void shouldSendRequestWithIgnoreModelRulesTrue() {
            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(true)
                .sickLeaveCertificate(SickLeaveCertificateDTO.builder().build())
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            final var captor = ArgumentCaptor.forClass(GetSickLeaveCertificateInternalIgnoreModelRulesDTO.class);

            csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);
            verify(requestBodyUriSpec).body(captor.capture());

            assertTrue(captor.getValue().isIgnoreModelRules());
        }

        @Test
        void shouldSetAcceptHeaderToApplicationJson() {
            final var response = GetSickLeaveCertificateInternalResponseDTO.builder()
                .available(true)
                .sickLeaveCertificate(SickLeaveCertificateDTO.builder().build())
                .build();

            doReturn(response).when(responseSpec).body(GetSickLeaveCertificateInternalResponseDTO.class);

            csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID);

            verify(requestBodyUriSpec).accept(MediaType.APPLICATION_JSON);
        }
    }
}