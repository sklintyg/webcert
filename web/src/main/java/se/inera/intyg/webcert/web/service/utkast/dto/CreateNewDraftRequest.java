package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Patient;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;

public class CreateNewDraftRequest {

    private String intygId;

    private String intygType;

    private UtkastStatus status;

    private Patient patient;

    private HoSPerson hosPerson;

    private Vardenhet vardenhet;

    public CreateNewDraftRequest() {

    }

    public CreateNewDraftRequest(String intygId, String intygType, UtkastStatus status, HoSPerson hosPerson,
            Vardenhet vardenhet, Patient patient) {
        this.intygId = intygId;
        this.intygType = intygType;
        this.status = status;
        this.hosPerson = hosPerson;
        this.vardenhet = vardenhet;
        this.patient = patient;
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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
