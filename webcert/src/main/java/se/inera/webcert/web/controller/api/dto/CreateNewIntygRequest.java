package se.inera.webcert.web.controller.api.dto;

public class CreateNewIntygRequest {
    
    private String patientPersonnummer;
    
    private String intygType;
    
    public CreateNewIntygRequest() {

    }

    public String getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(String patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

}
