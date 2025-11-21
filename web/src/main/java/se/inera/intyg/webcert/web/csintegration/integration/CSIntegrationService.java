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

import static se.inera.intyg.webcert.logging.MdcLogConstants.EVENT_TYPE_ACCESS;
import static se.inera.intyg.webcert.logging.MdcLogConstants.EVENT_TYPE_CHANGE;
import static se.inera.intyg.webcert.logging.MdcLogConstants.EVENT_TYPE_CREATION;
import static se.inera.intyg.webcert.logging.MdcLogConstants.EVENT_TYPE_DELETION;
import static se.inera.intyg.webcert.logging.MdcLogConstants.EVENT_TYPE_INFO;
import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExternalTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceGetCertificateResponseDTO;
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
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service
public class CSIntegrationService {

    private static final String CERTIFICATE_ENDPOINT_URL = "/api/certificate";
    private static final String INTERNAL_CERTIFICATE_ENDPOINT_URL = "/internalapi/certificate";
    private static final String INTERNAL_DRAFT_ENDPOINT_URL = "/internalapi/draft";
    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";
    private static final String MESSAGE_ENDPOINT_URL = "/api/message";
    private static final String INTERNAL_MESSAGE_ENDPOINT_URL = "/internalapi/message";
    private static final String PATIENT_ENDPOINT_URL = "/api/patient";
    private static final String CERTIFICATE_TYPE_INFO_ENDPOINT_URL = "/api/certificatetypeinfo";
    private static final String UNIT_ENDPOINT_URL = "/api/unit";
    private static final String NULL_RESPONSE_EXCEPTION = "Certificate service returned null response";
    private static final String EXISTS = "/exists";

    private final IntygModuleRegistry intygModuleRegistry;
    private final CertificateTypeInfoConverter certificateTypeInfoConverter;
    private final ListIntygEntryConverter listIntygEntryConverter;
    private final ListQuestionConverter listQuestionConverter;
    private final RestClient restClient;

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public CSIntegrationService(IntygModuleRegistry intygModuleRegistry, CertificateTypeInfoConverter certificateTypeInfoConverter,
        ListIntygEntryConverter listIntygEntryConverter, ListQuestionConverter listQuestionConverter,
        @Qualifier("csRestClient") RestClient restClient) {
        this.intygModuleRegistry = intygModuleRegistry;
        this.certificateTypeInfoConverter = certificateTypeInfoConverter;
        this.listIntygEntryConverter = listIntygEntryConverter;
        this.listQuestionConverter = listQuestionConverter;
        this.restClient = restClient;
    }

    @PerformanceLogging(eventAction = "list-certificates-unit", eventType = EVENT_TYPE_ACCESS)
    public List<ListIntygEntry> listCertificatesForUnit(GetUnitCertificatesRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetListCertificatesResponseDTO.class);

        if (response.getCertificates() == null) {
            throw new IllegalStateException("Response from certificate service was null when getting unit certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .toList();
    }

    @PerformanceLogging(eventAction = "list-questions-unit", eventType = EVENT_TYPE_ACCESS)
    public List<ArendeListItem> listQuestionsForUnit(GetUnitQuestionsRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/messages";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetUnitQuestionsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting unit questions list");
        }

        return response.getQuestions().stream()
            .map(question -> listQuestionConverter.convert(
                response.getCertificates()
                    .stream()
                    .filter(certificate -> certificate.getMetadata().getId().equals(question.getCertificateId()))
                    .findFirst(), question)
            )
            .toList();
    }

    @PerformanceLogging(eventAction = "list-certificates-info-unit", eventType = EVENT_TYPE_ACCESS)
    public List<StaffListInfo> listCertificatesInfoForUnit(GetUnitCertificatesInfoRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates/info";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetUnitCertificatesInfoResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting certificate list info");
        }

        return response.getStaffs()
            .stream()
            .map(staff ->
                StaffListInfo.builder()
                    .hsaId(staff.getPersonId())
                    .name(staff.getFullName())
                    .build()
            )
            .toList();
    }

    @PerformanceLogging(eventAction = "list-certificates-patient", eventType = EVENT_TYPE_ACCESS)
    public List<ListIntygEntry> listCertificatesForPatient(GetPatientCertificatesRequestDTO request) {
        final var url = baseUrl + PATIENT_ENDPOINT_URL + "/certificates";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetListCertificatesResponseDTO.class);

        if (response.getCertificates() == null) {
            throw new IllegalStateException("Response from certificate service was null when getting patient certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .toList();
    }

    @PerformanceLogging(eventAction = "delete-certificate", eventType = EVENT_TYPE_DELETION)
    public Certificate deleteCertificate(String certificateId, long version, DeleteCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/" + version;

        final var response = restClient.method(HttpMethod.DELETE)
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(DeleteCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Deleting certificate '%s' returned empty response!", certificateId)
            );
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificate-type-info", eventType = EVENT_TYPE_ACCESS)
    public List<CertificateTypeInfoDTO> getTypeInfo(CertificateServiceTypeInfoRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CertificateServiceTypeInfoResponseDTO.class);

        if (response == null) {
            return Collections.emptyList();
        }

        return response.getList()
            .stream()
            .map(certificateTypeInfoConverter::convert)
            .toList();
    }

    @PerformanceLogging(eventAction = "create-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate createCertificate(CreateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "create-draft-from-certificate", eventType = EVENT_TYPE_CREATION)
    public CreateCertificateFromTemplateResponseDTO createDraftFromCertificate(String certificateId,
        CreateCertificateFromTemplateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/draft";

        return restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CreateCertificateFromTemplateResponseDTO.class);
    }

    @PerformanceLogging(eventAction = "get-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getCertificate(String certificateId, GetCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "replace-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate replaceCertificate(String certificateId, ReplaceCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/replace";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ReplaceCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "renew-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate renewCertificate(String certificateId, RenewCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/renew";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(RenewCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "renew-legacy-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate renewLegacyCertificate(String certificateId, RenewLegacyCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/renew/external";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(RenewCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "complement-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate complementCertificate(String certificateId, CertificateComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/complement";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CertificateComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "validate-certificate", eventType = EVENT_TYPE_INFO)
    public ValidationErrorDTO[] validateCertificate(ValidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId() + "/validate";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ValidateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getValidationErrors();
    }

    @PerformanceLogging(eventAction = "certificate-type-exists", eventType = EVENT_TYPE_INFO)
    public Optional<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
        final var certificateServiceTypeId = certificateServiceTypeId(certificateType);
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + certificateServiceTypeId + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    private String certificateServiceTypeId(String type) {
        if (intygModuleRegistry.moduleExists(type)) {
            try {
                final var moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(type);
                return moduleEntryPoint.certificateServiceTypeId();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return type;
    }

    @PerformanceLogging(eventAction = "certificate-external-type-exists", eventType = EVENT_TYPE_INFO)
    public Optional<CertificateModelIdDTO> certificateExternalTypeExists(String codeSystem, String code) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + codeSystem + "/" + code + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateExternalTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    @PerformanceLogging(eventAction = "certificate-exists", eventType = EVENT_TYPE_INFO)
    public Boolean certificateExists(String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "message-exists", eventType = EVENT_TYPE_INFO)
    public Boolean messageExists(String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(MessageExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }


    @PerformanceLogging(eventAction = "placeholder-certificate-exists", eventType = EVENT_TYPE_INFO)
    public Boolean placeholderCertificateExists(String certificateId) {
        final var url =
            baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/placeholder/" + certificateId + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "revoke-placeholder-certificate", eventType = EVENT_TYPE_INFO)
    public void revokePlaceholderCertificate(String certificateId) {
        final var url =
            baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/placeholder/" + certificateId + "/revoke";

        restClient.post()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(Void.class);
    }

    @PerformanceLogging(eventAction = "citizen-certificate-exists", eventType = EVENT_TYPE_INFO)
    public Boolean citizenCertificateExists(String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + EXISTS;

        final var response = restClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CitizenCertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "save-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate saveCertificate(SaveCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId();

        final var response = restClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SaveCertificateResponseDTO.class);

        if (response.getCertificate() == null) {
            throw new IllegalStateException(
                String.format("Saving certificate '%s' returned empty response",
                    request.getCertificate().getMetadata().getId()
                )
            );
        }
        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificate-xml", eventType = EVENT_TYPE_ACCESS)
    public GetCertificateXmlResponseDTO getCertificateXml(GetCertificateXmlRequestDTO request, String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        return restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCertificateXmlResponseDTO.class);
    }

    @PerformanceLogging(eventAction = "sign-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate signCertificate(SignCertificateRequestDTO request, String certificateId, long version) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/sign/" + version;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SignCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Sign certificate request for '%s' returned empty response", certificateId)
            );
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "sign-certificate-without-signature", eventType = EVENT_TYPE_CHANGE)
    public Certificate signCertificateWithoutSignature(SignCertificateWithoutSignatureRequestDTO request, String certificateId,
        long version) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/signwithoutsignature/" + version;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SignCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Sign certificate without signature request for '%s' returned empty response", certificateId)
            );
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "print-certificate", eventType = EVENT_TYPE_ACCESS)
    public IntygPdf printCertificate(String certificateId, PrintCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/pdf";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(PrintCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return new IntygPdf(response.getPdfData(), response.getFileName());
    }

    @PerformanceLogging(eventAction = "send-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate sendCertificate(String certificateId, SendCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/send";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SendCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "revoke-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate revokeCertificate(String certificateId, RevokeCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/revoke";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(RevokeCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-citizen-certificate", eventType = EVENT_TYPE_ACCESS)
    public GetCitizenCertificateResponseDTO getCitizenCertificate(GetCitizenCertificateRequestDTO request, String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCitizenCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }

    @PerformanceLogging(eventAction = "get-citizen-certificate-pdf", eventType = EVENT_TYPE_ACCESS)
    public GetCitizenCertificatePdfResponseDTO getCitizenCertificatePdf(GetCitizenCertificatePdfRequestDTO request,
        String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + "/print";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCitizenCertificatePdfResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }

    @PerformanceLogging(eventAction = "answer-complement-on-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate answerComplementOnCertificate(String certificateId,
        AnswerComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/answerComplement";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(AnswerComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "post-message", eventType = EVENT_TYPE_CREATION)
    public void postMessage(IncomingMessageRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL;

        restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(Void.class);
    }

    @PerformanceLogging(eventAction = "get-questions", eventType = EVENT_TYPE_ACCESS)
    public List<Question> getQuestions(GetCertificateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCertificateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    @PerformanceLogging(eventAction = "handle-message", eventType = EVENT_TYPE_CHANGE)
    public Question handleMessage(HandleMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/handle";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(HandleMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "get-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getCertificate(GetCertificateFromMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/certificate";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCertificateFromMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-questions", eventType = EVENT_TYPE_ACCESS)
    public List<Question> getQuestions(String certificateId) {
        final var url = baseUrl + INTERNAL_MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(GetCertificateMessageInternalResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    @PerformanceLogging(eventAction = "get-internal-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getInternalCertificate(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-internal-certificate-xml", eventType = EVENT_TYPE_ACCESS)
    public String getInternalCertificateXml(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(InternalCertificateXmlResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getXml();
    }

    @PerformanceLogging(eventAction = "delete-message", eventType = EVENT_TYPE_DELETION)
    public void deleteMessage(String messageId, DeleteMessageRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/delete";

        restClient.method(HttpMethod.DELETE)
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(Void.class);
    }

    @PerformanceLogging(eventAction = "delete-answer", eventType = EVENT_TYPE_DELETION)
    public Question deleteAnswer(String messageId, DeleteAnswerRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/deleteanswer";

        final var response = restClient.method(HttpMethod.DELETE)
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(DeleteAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "create-message", eventType = EVENT_TYPE_CREATION)
    public Question createMessage(CreateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId + "/create";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CreateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "save-message", eventType = EVENT_TYPE_CHANGE)
    public Question saveMessage(SaveMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/save";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SaveMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "save-answer", eventType = EVENT_TYPE_CHANGE)
    public Question saveAnswer(SaveAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/saveanswer";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SaveAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "send-message", eventType = EVENT_TYPE_CHANGE)
    public Question sendMessage(SendMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/send";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SendMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "send-answer", eventType = EVENT_TYPE_CHANGE)
    public Question sendAnswer(SendAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/sendanswer";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(SendAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "forward-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate forwardCertificate(String certificateId, ForwardCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/forward";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ForwardCertificateResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificates-with-qa", eventType = EVENT_TYPE_ACCESS)
    public String getCertificatesWithQA(CertificatesWithQARequestDTO request) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/qa";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(CertificatesWithQAResponseDTO.class);

        if (response == null || response.getList() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getList();
    }

    @PerformanceLogging(eventAction = "lock-drafts", eventType = EVENT_TYPE_CHANGE)
    public List<Certificate> lockDrafts(LockDraftsRequestDTO request) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/lock";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(LockDraftsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificates();
    }

    @PerformanceLogging(eventAction = "internal-dispose-obsolete-draft", eventType = EVENT_TYPE_DELETION)
    public Certificate disposeObsoleteDraft(DisposeObsoleteDraftsRequestDTO request) {
        final var url = baseUrl + INTERNAL_DRAFT_ENDPOINT_URL;

        final var response = restClient.method(HttpMethod.DELETE)
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(DisposeObsoleteDraftsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }
        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "internal-list-obsolete-drafts", eventType = EVENT_TYPE_ACCESS)
    public List<String> listObsoleteDrafts(ListObsoleteDraftsRequestDTO request) {
        final var url = baseUrl + INTERNAL_DRAFT_ENDPOINT_URL + "/list";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ListObsoleteDraftsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificateIds();
    }

    @PerformanceLogging(eventAction = "get-certificate-events", eventType = EVENT_TYPE_ACCESS)
    public CertificateEventDTO[] getCertificateEvents(String certificateId, GetCertificateEventsRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/events";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCertificateEventsResponseDTO.class);

        if (response == null || response.getEvents() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getEvents().toArray(CertificateEventDTO[]::new);
    }

    @PerformanceLogging(eventAction = "get-statistics", eventType = EVENT_TYPE_ACCESS)
    public Map<String, StatisticsForUnitDTO> getStatistics(UnitStatisticsRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates/statistics";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(UnitStatisticsResponseDTO.class);

        if (response == null || response.getUnitStatistics() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getUnitStatistics();
    }

    @PerformanceLogging(eventAction = "mark-certificate-ready-for-sign", eventType = EVENT_TYPE_CHANGE)
    public Certificate markCertificateReadyForSign(String certificateId, ReadyForSignRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/readyForSign";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ReadyForSignResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public Certificate getCandidateCertificate(String certificateId, GetCandidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/candidate";

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(GetCandidateCertificateResponseDTO.class);

        return response.getCertificate();
    }

    public Certificate updateWithCandidateCertificate(String certificateId, String candidateCertificateId,
        UpdateWithCandidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/candidate/" + candidateCertificateId;

        final var response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(UpdateWithCandidateCertificateResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-sick-leave-certificate", eventType = EVENT_TYPE_ACCESS)
    public Optional<GetSickLeaveCertificateInternalResponseDTO> getSickLeaveCertificate(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/sickleave";

        final var response = restClient.post()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(GetSickLeaveCertificateInternalIgnoreModelRulesDTO.builder()
                .ignoreModelRules(true)
                .build())
            .retrieve()
            .body(GetSickLeaveCertificateInternalResponseDTO.class);

        if (response == null || !response.isAvailable() || response.getSickLeaveCertificate() == null) {
            return Optional.empty();
        }

        return Optional.of(response);
    }
}

