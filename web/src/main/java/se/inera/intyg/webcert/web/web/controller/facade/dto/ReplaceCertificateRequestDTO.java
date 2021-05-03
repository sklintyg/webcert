package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.facade.model.PersonId;

public class ReplaceCertificateRequestDTO {

    private PersonId patientId;
    private String certificateType;

    public PersonId getPatientId() {
        return patientId;
    }

    public void setPatientId(PersonId patientId) {
        this.patientId = patientId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }
}
