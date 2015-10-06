package se.inera.webcert.service.utkast.dto;

import org.apache.commons.lang3.StringUtils;

import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Vardenhet;

public class CreateNewDraftCopyRequest {

    private String originalIntygId;

    private String typ;

    private String patientPersonnummer;

    private String nyttPatientPersonnummer;

    private HoSPerson hosPerson;

    private Vardenhet vardenhet;

    private boolean djupintegrerad = false;

    public CreateNewDraftCopyRequest(String orginalIntygsId, String intygsTyp, String patientPersonnummer, HoSPerson hosPerson, Vardenhet vardenhet) {
        this.originalIntygId = orginalIntygsId;
        this.typ = intygsTyp;
        this.patientPersonnummer = patientPersonnummer;
        this.hosPerson = hosPerson;
        this.vardenhet = vardenhet;
    }

    public boolean containsNyttPatientPersonnummer() {
        return StringUtils.isNotBlank(nyttPatientPersonnummer);
    }

    public String getOriginalIntygId() {
        return originalIntygId;
    }

    public void setOriginalIntygId(String originalIntygId) {
        this.originalIntygId = originalIntygId;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
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

    public HoSPerson getHosPerson() {
        return hosPerson;
    }

    public void setHosPerson(HoSPerson hosPerson) {
        this.hosPerson = hosPerson;
    }

    public Vardenhet getVardenhet() {
        return vardenhet;
    }

    public void setVardenhet(Vardenhet vardenhet) {
        this.vardenhet = vardenhet;
    }

    public boolean isDjupintegrerad() {
        return djupintegrerad;
    }

    public void setDjupintegrerad(boolean djupintegrerad) {
        this.djupintegrerad = djupintegrerad;
    }
}
