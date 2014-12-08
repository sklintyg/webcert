package se.inera.webcert.web.controller.api.dto;

import org.apache.commons.lang3.StringUtils;

public class CopyIntygRequest {

    private String nyttPatientPersonnummer;

    public CopyIntygRequest() {

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
}
