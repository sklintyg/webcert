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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

@Component
@RequiredArgsConstructor
public class CSIntegrationRequestFactory {

    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;
    private final CertificateServicePatientHelper certificateServicePatientHelper;
    private final CertificatesQueryCriteriaFactory certificatesQueryCriteriaFactory;

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

    public GetCertificateRequestDTO getCertificateRequest() {
        return GetCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .user(certificateServiceUserHelper.get())
            .build();
    }

    public SaveCertificateRequestDTO saveRequest(Certificate certificate) {
        return SaveCertificateRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .patient(
                certificateServicePatientHelper.get(
                    createPatientId(certificate.getMetadata().getPatient().getPersonId().getId())
                )
            )
            .user(certificateServiceUserHelper.get())
            .certificate(certificate)
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

    public GetCertificateXmlRequestDTO getCertificateXmlRequest(Certificate certificate) {
        return GetCertificateXmlRequestDTO.builder()
            .unit(certificateServiceUnitHelper.getUnit())
            .careUnit(certificateServiceUnitHelper.getCareUnit())
            .careProvider(certificateServiceUnitHelper.getCareProvider())
            .patient(
                certificateServicePatientHelper.get(
                    createPatientId(certificate.getMetadata().getPatient().getPersonId().getId())
                )
            )
            .user(certificateServiceUserHelper.get())
            .build();
    }

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }
}
