package se.inera.intyg.webcert.web.csintegration.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;

@Component
@RequiredArgsConstructor
public class CSIntegrationRequestFactory {

    private final CertificateServiceUserHelper certificateServiceUserHelper;
    private final CertificateServiceUnitHelper certificateServiceUnitHelper;
    private final CertificateServicePatientHelper certificateServicePatientHelper;

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

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }
}
