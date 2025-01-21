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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
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
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteAnswerRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteAnswerResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ForwardCertificateResponseDTO;
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
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.InternalCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockDraftsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReadyForSignRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReadyForSignResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateResponseDTO;
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
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service
public class CSIntegrationService {

    private static final String CERTIFICATE_ENDPOINT_URL = "/api/certificate";
    private static final String INTERNAL_CERTIFICATE_ENDPOINT_URL = "/internalapi/certificate";
    private static final String CITIZEN_ENDPOINT_URL = "/api/citizen/certificate";
    private static final String MESSAGE_ENDPOINT_URL = "/api/message";
    private static final String INTERNAL_MESSAGE_ENDPOINT_URL = "/internalapi/message";
    private static final String PATIENT_ENDPOINT_URL = "/api/patient";
    private static final String CERTIFICATE_TYPE_INFO_ENDPOINT_URL = "/api/certificatetypeinfo";
    private static final String UNIT_ENDPOINT_URL = "/api/unit";
    private static final String NULL_RESPONSE_EXCEPTION = "Certificate service returned null response";
    private static final String EXISTS = "/exists";

    private final CertificateTypeInfoConverter certificateTypeInfoConverter;
    private final ListIntygEntryConverter listIntygEntryConverter;
    private final RestTemplate restTemplate;
    private final ListQuestionConverter listQuestionConverter;

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public CSIntegrationService(CertificateTypeInfoConverter certificateTypeInfoConverter, ListIntygEntryConverter listIntygEntryConverter,
        @Qualifier("csRestTemplate") RestTemplate restTemplate, ListQuestionConverter listQuestionConverter) {
        this.certificateTypeInfoConverter = certificateTypeInfoConverter;
        this.listIntygEntryConverter = listIntygEntryConverter;
        this.restTemplate = restTemplate;
        this.listQuestionConverter = listQuestionConverter;
    }

    @PerformanceLogging(eventAction = "list-certificates-unit", eventType = EVENT_TYPE_ACCESS)
    public List<ListIntygEntry> listCertificatesForUnit(GetUnitCertificatesRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates";
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
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
        final var response = restTemplate.postForObject(url, request, GetUnitQuestionsResponseDTO.class);

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
        final var response = restTemplate.postForObject(url, request, GetUnitCertificatesInfoResponseDTO.class);

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
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
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
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var response = restTemplate.<DeleteCertificateResponseDTO>exchange(
            url,
            HttpMethod.DELETE,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            }
        );

        if (response.getBody() == null) {
            throw new IllegalStateException(
                String.format("Deleting certificate '%s' returned empty response!", certificateId)
            );
        }

        return response.getBody().getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificate-type-info", eventType = EVENT_TYPE_ACCESS)
    public List<CertificateTypeInfoDTO> getTypeInfo(CertificateServiceTypeInfoRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL;
        final var response = restTemplate.postForObject(url, request, CertificateServiceTypeInfoResponseDTO.class);

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

        final var response = restTemplate.postForObject(url, request, CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getCertificate(String certificateId, GetCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "replace-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate replaceCertificate(String certificateId, ReplaceCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/replace";

        final var response = restTemplate.postForObject(url, request, ReplaceCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "renew-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate renewCertificate(String certificateId, RenewCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/renew";

        final var response = restTemplate.postForObject(url, request, RenewCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "complement-certificate", eventType = EVENT_TYPE_CREATION)
    public Certificate complementCertificate(String certificateId, CertificateComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/complement";

        final var response = restTemplate.postForObject(url, request, CertificateComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "validate-certificate", eventType = EVENT_TYPE_INFO)
    public ValidationErrorDTO[] validateCertificate(ValidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId() + "/validate";

        final var response = restTemplate.postForObject(url, request, ValidateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getValidationErrors();
    }

    @PerformanceLogging(eventAction = "certificate-type-exists", eventType = EVENT_TYPE_INFO)
    public Optional<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + certificateType + EXISTS;
        final var response = restTemplate.getForObject(url, CertificateTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    @PerformanceLogging(eventAction = "certificate-external-type-exists", eventType = EVENT_TYPE_INFO)
    public Optional<CertificateModelIdDTO> certificateExternalTypeExists(String codeSystem, String code) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + codeSystem + "/" + code + EXISTS;
        final var response = restTemplate.getForObject(url, CertificateExternalTypeExistsResponseDTO.class);

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

        final var response = restTemplate.getForObject(url, CertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "message-exists", eventType = EVENT_TYPE_INFO)
    public Boolean messageExists(String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + EXISTS;

        final var response = restTemplate.getForObject(url, MessageExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "citizen-certificate-exists", eventType = EVENT_TYPE_INFO)
    public Boolean citizenCertificateExists(String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + EXISTS;

        final var response = restTemplate.getForObject(url, CitizenCertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    @PerformanceLogging(eventAction = "save-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate saveCertificate(SaveCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId();
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final var response = restTemplate.<SaveCertificateResponseDTO>exchange(
            url,
            HttpMethod.PUT,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            },
            Collections.emptyMap()
        );
        if (response.getBody() == null) {
            throw new IllegalStateException(
                String.format("Saving certificate '%s' returned empty response",
                    request.getCertificate().getMetadata().getId()
                )
            );
        }
        return response.getBody().getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificate-xml", eventType = EVENT_TYPE_ACCESS)
    public GetCertificateXmlResponseDTO getCertificateXml(GetCertificateXmlRequestDTO request, String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        return restTemplate.postForObject(url, request, GetCertificateXmlResponseDTO.class);
    }

    @PerformanceLogging(eventAction = "sign-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate signCertificate(SignCertificateRequestDTO request, String certificateId, long version) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/sign/" + version;

        final var response = restTemplate.postForObject(url, request, SignCertificateResponseDTO.class);

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

        final var response = restTemplate.postForObject(url, request, SignCertificateResponseDTO.class);

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

        final var response = restTemplate.postForObject(url, request, PrintCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return new IntygPdf(response.getPdfData(), response.getFileName());
    }

    @PerformanceLogging(eventAction = "send-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate sendCertificate(String certificateId, SendCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/send";

        final var response = restTemplate.postForObject(url, request, SendCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "revoke-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate revokeCertificate(String certificateId, RevokeCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/revoke";

        final var response = restTemplate.postForObject(url, request, RevokeCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-citizen-certificate", eventType = EVENT_TYPE_ACCESS)
    public GetCitizenCertificateResponseDTO getCitizenCertificate(GetCitizenCertificateRequestDTO request, String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }

    @PerformanceLogging(eventAction = "get-citizen-certificate-pdf", eventType = EVENT_TYPE_ACCESS)
    public GetCitizenCertificatePdfResponseDTO getCitizenCertificatePdf(GetCitizenCertificatePdfRequestDTO request,
        String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + "/print";

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificatePdfResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }

    @PerformanceLogging(eventAction = "answer-complement-on-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate answerComplementOnCertificate(String certificateId,
        AnswerComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/answerComplement";

        final var response = restTemplate.postForObject(url, request, AnswerComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "post-message", eventType = EVENT_TYPE_CREATION)
    public void postMessage(IncomingMessageRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL;
        restTemplate.postForObject(url, request, Void.class);
    }

    @PerformanceLogging(eventAction = "get-questions", eventType = EVENT_TYPE_ACCESS)
    public List<Question> getQuestions(GetCertificateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, GetCertificateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    @PerformanceLogging(eventAction = "handle-message", eventType = EVENT_TYPE_CHANGE)
    public Question handleMessage(HandleMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/handle";

        final var response = restTemplate.postForObject(url, request, HandleMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "get-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getCertificate(GetCertificateFromMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/certificate";

        final var response = restTemplate.postForObject(url, request, GetCertificateFromMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-questions", eventType = EVENT_TYPE_ACCESS)
    public List<Question> getQuestions(String certificateId) {
        final var url = baseUrl + INTERNAL_MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, null, GetCertificateMessageInternalResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    @PerformanceLogging(eventAction = "get-internal-certificate", eventType = EVENT_TYPE_ACCESS)
    public Certificate getInternalCertificate(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, null, CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-internal-certificate-xml", eventType = EVENT_TYPE_ACCESS)
    public String getInternalCertificateXml(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        final var response = restTemplate.postForObject(url, null, InternalCertificateXmlResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getXml();
    }

    @PerformanceLogging(eventAction = "delete-message", eventType = EVENT_TYPE_DELETION)
    public void deleteMessage(String messageId, DeleteMessageRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/delete";

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.<Void>exchange(
            url,
            HttpMethod.DELETE,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            }
        );
    }

    @PerformanceLogging(eventAction = "delete-answer", eventType = EVENT_TYPE_DELETION)
    public Question deleteAnswer(String messageId, DeleteAnswerRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/deleteanswer";

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var response = restTemplate.<DeleteAnswerResponseDTO>exchange(
            url,
            HttpMethod.DELETE,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            }
        );

        if (response.getBody() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getBody().getQuestion();
    }

    @PerformanceLogging(eventAction = "create-message", eventType = EVENT_TYPE_CREATION)
    public Question createMessage(CreateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId + "/create";

        final var response = restTemplate.postForObject(url, request, CreateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "save-message", eventType = EVENT_TYPE_CHANGE)
    public Question saveMessage(SaveMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/save";

        final var response = restTemplate.postForObject(url, request, SaveMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "save-answer", eventType = EVENT_TYPE_CHANGE)
    public Question saveAnswer(SaveAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/saveanswer";

        final var response = restTemplate.postForObject(url, request, SaveAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "send-message", eventType = EVENT_TYPE_CHANGE)
    public Question sendMessage(SendMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/send";

        final var response = restTemplate.postForObject(url, request, SendMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "send-answer", eventType = EVENT_TYPE_CHANGE)
    public Question sendAnswer(SendAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/sendanswer";

        final var response = restTemplate.postForObject(url, request, SendAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    @PerformanceLogging(eventAction = "forward-certificate", eventType = EVENT_TYPE_CHANGE)
    public Certificate forwardCertificate(String certificateId, ForwardCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/forward";

        final var response = restTemplate.postForObject(url, request, ForwardCertificateResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    @PerformanceLogging(eventAction = "get-certificates-with-qa", eventType = EVENT_TYPE_ACCESS)
    public String getCertificatesWithQA(CertificatesWithQARequestDTO request) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/qa";

        final var response = restTemplate.postForObject(url, request, CertificatesWithQAResponseDTO.class);

        if (response == null || response.getList() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getList();
    }

    @PerformanceLogging(eventAction = "lock-drafts", eventType = EVENT_TYPE_CHANGE)
    public List<Certificate> lockDrafts(LockDraftsRequestDTO request) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/lock";

        final var response = restTemplate.postForObject(url, request, LockDraftsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificates();
    }

    @PerformanceLogging(eventAction = "get-certificate-events", eventType = EVENT_TYPE_ACCESS)
    public CertificateEventDTO[] getCertificateEvents(String certificateId, GetCertificateEventsRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/events";

        final var response = restTemplate.postForObject(url, request, GetCertificateEventsResponseDTO.class);

        if (response == null || response.getEvents() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getEvents().toArray(CertificateEventDTO[]::new);
    }

    @PerformanceLogging(eventAction = "get-statistics", eventType = EVENT_TYPE_ACCESS)
    public Map<String, StatisticsForUnitDTO> getStatistics(UnitStatisticsRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates/statistics";

        final var response = restTemplate.postForObject(url, request, UnitStatisticsResponseDTO.class);

        if (response == null || response.getUnitStatistics() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getUnitStatistics();
    }

    @PerformanceLogging(eventAction = "mark-certificate-ready-for-sign", eventType = EVENT_TYPE_CHANGE)
    public Certificate markCertificateReadyForSign(String certificateId, ReadyForSignRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/readyForSign";

        final var response = restTemplate.postForObject(url, request, ReadyForSignResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }
}
