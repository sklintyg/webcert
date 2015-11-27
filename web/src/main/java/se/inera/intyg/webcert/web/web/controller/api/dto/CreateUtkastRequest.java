package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang.StringUtils;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class CreateUtkastRequest {

    private String intygType;

    private Personnummer patientPersonnummer;

    private String patientFornamn;

    private String patientMellannamn;

    private String patientEfternamn;

    private String patientPostadress;

    private String patientPostnummer;

    private String patientPostort;

    public CreateUtkastRequest() {

    }

    public boolean isValid() {

        if (StringUtils.isBlank(intygType)) {
            return false;
        }

        if (patientPersonnummer == null || StringUtils.isBlank(patientPersonnummer.getPersonnummer())) {
            return false;
        }

        if (StringUtils.isBlank(patientFornamn) || StringUtils.isBlank(patientEfternamn)) {
            return false;
        }

        return true;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public Personnummer getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public String getPatientFornamn() {
        return patientFornamn;
    }

    public void setPatientFornamn(String patientFornamn) {
        this.patientFornamn = patientFornamn;
    }

    public String getPatientMellannamn() {
        return patientMellannamn;
    }

    public void setPatientMellannamn(String patientMellannamn) {
        this.patientMellannamn = patientMellannamn;
    }

    public String getPatientEfternamn() {
        return patientEfternamn;
    }

    public void setPatientEfternamn(String patientEfternamn) {
        this.patientEfternamn = patientEfternamn;
    }

    public String getPatientPostadress() {
        return patientPostadress;
    }

    public void setPatientPostadress(String patientPostadress) {
        this.patientPostadress = patientPostadress;
    }

    public String getPatientPostnummer() {
        return patientPostnummer;
    }

    public void setPatientPostnummer(String patientPostnummer) {
        this.patientPostnummer = patientPostnummer;
    }

    public String getPatientPostort() {
        return patientPostort;
    }

    public void setPatientPostort(String patientPostort) {
        this.patientPostort = patientPostort;
    }

}
