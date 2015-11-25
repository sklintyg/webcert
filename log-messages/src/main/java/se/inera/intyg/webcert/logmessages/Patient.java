package se.inera.intyg.webcert.logmessages;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class Patient implements Serializable {

    private static final long serialVersionUID = -3089443403583678480L;

    private String patientId;
    private String patientNamn;

    public Patient() {
    }

    public Patient(Personnummer patientId) {
        this(patientId, null);
    }

    public Patient(Personnummer patientId, String patientNamn) {
        this.patientId = patientId.getPersonnummer();
        this.patientNamn = patientNamn;
    }

    public Personnummer getPatientId() {
        return new Personnummer(patientId);
    }

    public String getPatientNamn() {
        return patientNamn;
    }
}
