package se.inera.webcert.service.utkast.dto;

import org.apache.commons.lang3.StringUtils;

import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Vardenhet;

public class CreateNewDraftCopyRequest {

    private String originalIntygId;
    
    private String typ;

    private String nyttPatientPersonnummer;

    private HoSPerson hosPerson;

    private Vardenhet vardenhet;

    public CreateNewDraftCopyRequest() {

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
}
