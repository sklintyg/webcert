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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.dto.AnswerComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateComplementRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificteFromMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.HandleMessageRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrintCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RenewCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RevokeInformationDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateWithoutSignatureRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
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

    public GetPatientCertificatesRequestDTO getPatientCertificatesRequest(String patientId) {
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
            .patient(certificateServicePatientHelper.get(createPatientId(certificate.getMetadata().getPatient().getPersonId().getId())))
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
            .patient(certificateServicePatientHelper.get(getPatientId(patient, integrationParameters)))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public RenewCertificateRequestDTO renewCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return RenewCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(getPatientId(patient, integrationParameters)))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    public CertificateComplementRequestDTO complementCertificateRequest(Patient patient, IntegrationParameters integrationParameters) {
        return CertificateComplementRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .patient(certificateServicePatientHelper.get(getPatientId(patient, integrationParameters)))
            .externalReference(getExternalReference(integrationParameters))
            .build();
    }

    private String convertReason(String reason) {
        switch (reason) {
            case "FEL_PATIENT":
                return "INCORRECT_PATIENT";
            case "ANNAT_ALLVARLIGT_FEL":
                return "OTHER_SERIOUS_ERROR";
            default:
                throw new IllegalArgumentException("Invalid revoke reason. Reason must be either 'FEL_PATIENT' or 'ANNAT_ALLVARLIGT_FEL'");
        }
    }

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }

    private boolean isAlternateSSNDefined(IntegrationParameters integrationParameters) {
        return integrationParameters != null && integrationParameters.getAlternateSsn() != null && !integrationParameters.getAlternateSsn()
            .isEmpty();
    }

    private String getExternalReference(IntegrationParameters integrationParameters) {
        return integrationParameters == null ? null : integrationParameters.getReference();
    }

    private Personnummer getPatientId(Patient patient, IntegrationParameters integrationParameters) {
        if (patient.isReserveId()) {
            return createPatientId(patient.getPreviousPersonId().getId());
        }

        return isAlternateSSNDefined(integrationParameters)
            ? createPatientId(integrationParameters.getAlternateSsn())
            : createPatientId(patient.getPersonId().getId());
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
            .isHandled(isHandled)
            .build();
    }

    public GetCertificteFromMessageRequestDTO getCertificateFromMessageRequestDTO() {
        return GetCertificteFromMessageRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }
}
