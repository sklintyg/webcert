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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
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
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockOldDraftsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.LockOldDraftsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateResponseDTO;
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
    public static final String NULL_RESPONSE_EXCEPTION = "Certificate service returned null response!";

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

    public List<ListIntygEntry> listCertificatesForUnit(GetUnitCertificatesRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates";
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting unit certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .collect(Collectors.toList());
    }

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
            .collect(Collectors.toList());
    }

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
            .collect(Collectors.toList());
    }

    public List<ListIntygEntry> listCertificatesForPatient(GetPatientCertificatesRequestDTO request) {
        final var url = baseUrl + PATIENT_ENDPOINT_URL + "/certificates";
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting patient certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .collect(Collectors.toList());
    }

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

    public List<CertificateTypeInfoDTO> getTypeInfo(CertificateServiceTypeInfoRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL;
        final var response = restTemplate.postForObject(url, request, CertificateServiceTypeInfoResponseDTO.class);

        if (response == null) {
            return Collections.emptyList();
        }

        return response.getList()
            .stream()
            .map(certificateTypeInfoConverter::convert)
            .collect(Collectors.toList());
    }

    public Certificate createCertificate(CreateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL;

        final var response = restTemplate.postForObject(url, request, CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public Certificate getCertificate(String certificateId, GetCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }

    public Certificate replaceCertificate(String certificateId, ReplaceCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/replace";

        final var response = restTemplate.postForObject(url, request, ReplaceCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public Certificate renewCertificate(String certificateId, RenewCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/renew";

        final var response = restTemplate.postForObject(url, request, RenewCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public Certificate complementCertificate(String certificateId, CertificateComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/complement";

        final var response = restTemplate.postForObject(url, request, CertificateComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public ValidationErrorDTO[] validateCertificate(ValidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId() + "/validate";

        final var response = restTemplate.postForObject(url, request, ValidateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getValidationErrors();
    }

    public Optional<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + certificateType + "/exists";
        final var response = restTemplate.getForObject(url, CertificateTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    public Optional<CertificateModelIdDTO> certificateExternalTypeExists(String codeSystem, String code) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + codeSystem + "/" + code + "/exists";
        final var response = restTemplate.getForObject(url, CertificateExternalTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    public Boolean certificateExists(String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/exists";

        final var response = restTemplate.getForObject(url, CertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }


    public Boolean messageExists(String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/exists";

        final var response = restTemplate.getForObject(url, MessageExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    public Boolean citizenCertificateExists(String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + "/exists";

        final var response = restTemplate.getForObject(url, CitizenCertificateExistsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return Boolean.TRUE.equals(response.getExists());
    }

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
                String.format("Saving certificate '%s' returned empty response!",
                    request.getCertificate().getMetadata().getId()
                )
            );
        }
        return response.getBody().getCertificate();
    }

    public GetCertificateXmlResponseDTO getCertificateXml(GetCertificateXmlRequestDTO request, String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        return restTemplate.postForObject(url, request, GetCertificateXmlResponseDTO.class);
    }

    public Certificate signCertificate(SignCertificateRequestDTO request, String certificateId, long version) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/sign/" + version;

        final var response = restTemplate.postForObject(url, request, SignCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Sign certificate request for '%s' returned empty response!", certificateId)
            );
        }

        return response.getCertificate();
    }

    public Certificate signCertificateWithoutSignature(SignCertificateWithoutSignatureRequestDTO request, String certificateId,
        long version) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/signwithoutsignature/" + version;

        final var response = restTemplate.postForObject(url, request, SignCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                String.format("Sign certificate without signature request for '%s' returned empty response!", certificateId)
            );
        }

        return response.getCertificate();
    }

    public IntygPdf printCertificate(String certificateId, PrintCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/pdf";

        final var response = restTemplate.postForObject(url, request, PrintCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return new IntygPdf(response.getPdfData(), response.getFileName());
    }

    public Certificate sendCertificate(String certificateId, SendCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/send";

        final var response = restTemplate.postForObject(url, request, SendCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public Certificate revokeCertificate(String certificateId, RevokeCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/revoke";

        final var response = restTemplate.postForObject(url, request, RevokeCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public GetCitizenCertificateResponseDTO getCitizenCertificate(GetCitizenCertificateRequestDTO request, String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }

    public GetCitizenCertificatePdfResponseDTO getCitizenCertificatePdf(GetCitizenCertificatePdfRequestDTO request,
        String certificateId) {
        final var url = baseUrl + CITIZEN_ENDPOINT_URL + "/" + certificateId + "/print";

        final var response = restTemplate.postForObject(url, request, GetCitizenCertificatePdfResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response;
    }


    public Certificate answerComplementOnCertificate(String certificateId,
        AnswerComplementRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/answerComplement";

        final var response = restTemplate.postForObject(url, request, AnswerComplementResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public void postMessage(IncomingMessageRequestDTO request) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL;
        restTemplate.postForObject(url, request, Void.class);
    }

    public List<Question> getQuestions(GetCertificateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, GetCertificateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    public Question handleMessage(HandleMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/handle";

        final var response = restTemplate.postForObject(url, request, HandleMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Certificate getCertificate(GetCertificateFromMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/certificate";

        final var response = restTemplate.postForObject(url, request, GetCertificateFromMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public List<Question> getQuestions(String certificateId) {
        final var url = baseUrl + INTERNAL_MESSAGE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, null, GetCertificateMessageInternalResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestions();
    }

    public Certificate getCertificate(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, null, CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public String getInternalCertificateXml(String certificateId) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        final var response = restTemplate.postForObject(url, null, InternalCertificateXmlResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getXml();
    }

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

    public Question createMessage(CreateMessageRequestDTO request, String certificateId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + certificateId + "/create";

        final var response = restTemplate.postForObject(url, request, CreateMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Question saveMessage(SaveMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/save";

        final var response = restTemplate.postForObject(url, request, SaveMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Question saveAnswer(SaveAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/saveanswer";

        final var response = restTemplate.postForObject(url, request, SaveAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Question sendMessage(SendMessageRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/send";

        final var response = restTemplate.postForObject(url, request, SendMessageResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Question sendAnswer(SendAnswerRequestDTO request, String messageId) {
        final var url = baseUrl + MESSAGE_ENDPOINT_URL + "/" + messageId + "/sendanswer";

        final var response = restTemplate.postForObject(url, request, SendAnswerResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getQuestion();
    }

    public Certificate forwardCertificate(String certificateId, ForwardCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/forward";

        final var response = restTemplate.postForObject(url, request, ForwardCertificateResponseDTO.class);

        if (response == null || response.getCertificate() == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getCertificate();
    }

    public int lockOldDrafts(LockOldDraftsRequestDTO request) {
        final var url = baseUrl + INTERNAL_CERTIFICATE_ENDPOINT_URL + "/lockOldDrafts";

        final var response = restTemplate.postForObject(url, request, LockOldDraftsResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(NULL_RESPONSE_EXCEPTION);
        }

        return response.getAmount();
    }
}
