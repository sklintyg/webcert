package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class Patient implements Serializable {

    private String patientId;
    private String patientNamn;

    public Patient(String patientId) {
        this(patientId, null);
    }

    public Patient(String patientId, String patientNamn) {
        this.patientId = patientId;
        this.patientNamn = patientNamn;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientNamn() {
        return patientNamn;
    }
}
