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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.dto.*;
import se.inera.intyg.webcert.web.csintegration.message.MessageRequestConverter;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdType;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceIntegrationUnitHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceIntegrationUserHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CSIntegrationRequestFactory {

    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceIntegrationUserHelper certificateServiceIntegrationUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;
    private final CertificateServiceIntegrationUnitHelper certificateServiceIntegrationUnitHelper;
    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final CertificatesQueryCriteriaFactory certificatesQueryCriteriaFactory;
    private final MessageRequestConverter messageRequestConverter;

    public CertificateServiceTypeInfoRequestDTO getCertificateTypesRequest(Personnummer patientId) {
        return CertificateServiceTypeInfoRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(patientId))
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .build();
    }

    public CreateCertificateRequestDTO createCertificateRequest(CertificateModelIdDTO modelId, String patientId) {
        return CreateCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .patient(
                certificateServicePatientHelper.get(
                    createPatientId(patientId)
                )
            )
            .user(certificateServiceUserHelper.get())
            .certificateModelId(modelId)
            .build();
    }

    public CreateCertificateRequestDTO createDraftCertificateRequest(CertificateModelIdDTO modelId, Intyg certificate, IntygUser user) {

        return CreateCertificateRequestDTO.builder()
            .unit(certificateServiceIntegrationUnitHelper.getUnit(user))
            .careUnit(certificateServiceIntegrationUnitHelper.getCareUnit(user))
            .careProvider(certificateServiceIntegrationUnitHelper.getCareProvider(user))
            .user(certificateServiceIntegrationUserHelper.get(user))
            .patient(
                certificateServicePatientHelper.get(
                    createPatientId(certificate.getPatient().getPersonId().getExtension())
                )
            )
            .externalReference(certificate.getRef())
            .certificateModelId(modelId)
            .prefillXml(PrefillXmlDTO.marshall(certificate.getForifyllnad()))
            .build();
    }

    public GetCertificateRequestDTO getCertificateRequest() {
        return GetCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public SaveCertificateRequestDTO saveRequest(Certificate certificate, String personId) {
        return saveRequest(certificate, personId, null);
    }

    public SaveCertificateRequestDTO saveRequest(Certificate certificate, String personId, String externalReference) {
        return SaveCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .patient(
                certificateServicePatientHelper.get(
                    createPatientId(personId)
                )
            )
            .user(certificateServiceUserHelper.get())
            .certificate(certificate)
            .externalReference(externalReference)
            .build();
    }

    public DeleteCertificateRequestDTO deleteCertificateRequest() {
        return DeleteCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public GetPatientCertificatesRequestDTO getPatientCertificatesRequest(
        String patientId) {
        return GetPatientCertificatesRequestDTO.builder()
            .patient(certificateServicePatientHelper.get(createPatientId(patientId)))
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public GetUnitCertificatesRequestDTO getUnitCertificatesRequest(ListFilter filter) {
        final var queryCriteria = certificatesQueryCriteriaFactory.create(filter);
        return getUnitCertificatesRequestDTO(queryCriteria);
    }

    public GetUnitCertificatesRequestDTO getUnitCertificatesRequest(QueryIntygParameter filter) {
        final var queryCriteria = certificatesQueryCriteriaFactory.create(filter);
        return getUnitCertificatesRequestDTO(queryCriteria);
    }

    public GetUnitCertificatesInfoRequestDTO getUnitCertificatesInfoRequest() {
        return GetUnitCertificatesInfoRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public ValidateCertificateRequestDTO getValidateCertificateRequest(Certificate certificate) {
        return ValidateCertificateRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .patient(
                certificateServicePatientHelper.get(createPatientId(certificate.getMetadata().getPatient().getActualPersonId().getId()))
            )
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .certificate(certificate)
            .build();
    }

    public GetCertificateXmlRequestDTO getCertificateXmlRequest() {
        return GetCertificateXmlRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public GetCertificateXmlRequestDTO getCertificateXmlRequest(IntygUser user) {
        return GetCertificateXmlRequestDTO.builder()
            .unit(certificateServiceIntegrationUnitHelper.getUnit(user))
            .careUnit(certificateServiceIntegrationUnitHelper.getCareUnit(user))
            .careProvider(certificateServiceIntegrationUnitHelper.getCareProvider(user))
            .user(certificateServiceIntegrationUserHelper.get(user))
            .build();
    }

    public SignCertificateRequestDTO signCertificateRequest(String signatureXml) {
        return SignCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .signatureXml(Base64.getEncoder().encodeToString(signatureXml.getBytes(StandardCharsets.UTF_8)))
            .build();
    }

    private GetUnitCertificatesRequestDTO getUnitCertificatesRequestDTO(CertificatesQueryCriteriaDTO queryCriteria) {
        final var patient = queryCriteria.getPersonId() == null ? null
            : certificateServicePatientHelper.get(createPatientId(queryCriteria.getPersonId().getId()));

        return GetUnitCertificatesRequestDTO.builder()
            .patient(patient)
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .certificatesQueryCriteria(queryCriteria)
            .build();
    }

    public PrintCertificateRequestDTO getPrintCertificateRequest(String additionalInfoText, String patientId) {
        return PrintCertificateRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .patient(certificateServicePatientHelper.get(createPatientId(patientId)))
            .additionalInfoText(additionalInfoText)
            .build();
    }

    public SendCertificateRequestDTO sendCertificateRequest() {
        return SendCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public SignCertificateWithoutSignatureRequestDTO signCertificateWithoutSignatureRequest() {
        return SignCertificateWithoutSignatureRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public RevokeCertificateRequestDTO revokeCertificateRequest(String reason, String message) {
        return RevokeCertificateRequestDTO.builder()
            .revoked(
                RevokeInformationDTO.builder()
                    .reason(convertReason(reason))
                    .message(message)
                    .build()
            )
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public ReplaceCertificateRequestDTO replaceCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return ReplaceCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(patient.getActualPersonId().getId())))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public RenewCertificateRequestDTO renewCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return RenewCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(patient.getActualPersonId().getId())))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public RenewLegacyCertificateRequestDTO renewLegacyCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return RenewLegacyCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(patient.getActualPersonId().getId())))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public CertificateComplementRequestDTO complementCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return CertificateComplementRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(patient.getActualPersonId().getId())))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public ForwardCertificateRequestDTO forwardCertificateRequest() {
        return ForwardCertificateRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .build();
    }

    public GetCertificateEventsRequestDTO getCertificateEventsRequest() {
        return GetCertificateEventsRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .build();
    }

    private String convertReason(String reason) {
        return switch (reason) {
            case "FEL_PATIENT" -> "INCORRECT_PATIENT";
            case "ANNAT_ALLVARLIGT_FEL" -> "OTHER_SERIOUS_ERROR";
            default ->
                    throw new IllegalArgumentException("Invalid revoke reason. Reason must be either 'FEL_PATIENT' or 'ANNAT_ALLVARLIGT_FEL'");
        };
    }

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }

    private String getExternalReference(IntegrationParameters integrationParameters) {
        return integrationParameters == null ? null : integrationParameters.getReference();
    }

    public GetCitizenCertificateRequestDTO getCitizenCertificateRequest(String personId) {
        final var patientId = createPatientId(personId);
        return GetCitizenCertificateRequestDTO.builder()
            .personId(
                PersonIdDTO.builder()
                    .id(patientId.getOriginalPnr())
                    .type(isCoordinationNumber(patientId) ? PersonIdType.COORDINATION_NUMBER : PersonIdType.PERSONAL_IDENTITY_NUMBER)
                    .build()
            )
            .build();
    }

    private boolean isCoordinationNumber(Personnummer personId) {
        return SamordningsnummerValidator.isSamordningsNummer(Optional.of(personId));
    }

    public GetCitizenCertificatePdfRequestDTO getCitizenCertificatePdfRequest(String personId) {
        final var citizenPersonId = createPatientId(personId);
        return GetCitizenCertificatePdfRequestDTO.builder()
            .personId(
                PersonIdDTO.builder()
                    .id(citizenPersonId.getOriginalPnr())
                    .type(isCoordinationNumber(citizenPersonId) ? PersonIdType.COORDINATION_NUMBER : PersonIdType.PERSONAL_IDENTITY_NUMBER)
                    .build()
            )
            .additionalInfo("Utskriven från 1177 intyg")
            .build();
    }

    public AnswerComplementRequestDTO answerComplementOnCertificateRequest(String message) {
        return AnswerComplementRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .message(message)
            .build();
    }

    public IncomingMessageRequestDTO getIncomingMessageRequest(SendMessageToCareType sendMessageToCare) {
        return messageRequestConverter.convert(sendMessageToCare);
    }

    public GetCertificateMessageRequestDTO getCertificateMessageRequest(String personId) {
        return GetCertificateMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(personId)))
            .build();
    }

    public HandleMessageRequestDTO handleMessageRequestDTO(boolean isHandled) {
        return HandleMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .handled(isHandled)
            .build();
    }

    public GetCertificateFromMessageRequestDTO getCertificateFromMessageRequestDTO() {
        return GetCertificateFromMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public DeleteMessageRequestDTO deleteMessageRequest() {
        return DeleteMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public DeleteAnswerRequestDTO deleteAnswerRequest() {
        return DeleteAnswerRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public CreateMessageRequestDTO createMessageRequest(QuestionType type, String message, String personId) {
        return CreateMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(personId)))
            .questionType(type)
            .message(message)
            .build();
    }

    public SaveMessageRequestDTO saveMessageRequest(Question question) {
        return SaveMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .question(question)
            .build();
    }

    public SaveAnswerRequestDTO saveAnswerRequest(String message) {
        return SaveAnswerRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .content(message)
            .build();
    }

    public SendMessageRequestDTO sendMessageRequest(String personId) {
        return SendMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(personId)))
            .build();
    }

    public SendAnswerRequestDTO sendAnswerRequest(String personId, String message) {
        return SendAnswerRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(createPatientId(personId)))
            .content(message)
            .build();
    }

    public GetUnitQuestionsRequestDTO getUnitQuestionsRequestDTO(MessageQueryCriteriaDTO messageQueryCriteriaDTO) {
        return GetUnitQuestionsRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .messagesQueryCriteria(messageQueryCriteriaDTO)
            .build();
    }

    public CertificatesWithQARequestDTO getCertificatesWithQARequestDTO(List<String> certificateIds) {
        return CertificatesWithQARequestDTO.builder()
            .certificateIds(certificateIds)
            .build();
    }

    public LockDraftsRequestDTO getLockDraftsRequestDTO(int lockedAfterDay) {
        final var cutoffDate = LocalDate.now().minusDays(lockedAfterDay).atStartOfDay();
        return LockDraftsRequestDTO.builder()
            .cutoffDate(cutoffDate)
            .build();
    }

    public UnitStatisticsRequestDTO getStatisticsRequest(List<String> unitIds) {
        return UnitStatisticsRequestDTO.builder()
            .user(certificateServiceUserHelper.get())
            .issuedByUnitIds(unitIds)
            .build();
    }

    public ReadyForSignRequestDTO readyForSignRequest() {
        return ReadyForSignRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }
}
