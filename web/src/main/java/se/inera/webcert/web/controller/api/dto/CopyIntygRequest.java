package se.inera.webcert.web.controller.api.dto;

import org.apache.commons.lang3.StringUtils;

public class CopyIntygRequest {

    private String patientPersonnummer;

    private String nyttPatientPersonnummer;

    public CopyIntygRequest() {

    }

    public String getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(String patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public String getNyttPatientPersonnummer() {
        return nyttPatientPersonnummer;
    }

    public void setNyttPatientPersonnummer(String nyttPatientPersonnummer) {
        this.nyttPatientPersonnummer = nyttPatientPersonnummer;
    }

    public boolean containsNewPersonnummer() {
        return StringUtils.isNotBlank(nyttPatientPersonnummer);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(patientPersonnummer);
    }
}
