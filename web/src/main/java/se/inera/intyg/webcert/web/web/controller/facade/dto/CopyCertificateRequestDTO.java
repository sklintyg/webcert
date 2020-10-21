package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class CopyCertificateRequestDTO {

    private String patientId;
    private String certificateType;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }
}
