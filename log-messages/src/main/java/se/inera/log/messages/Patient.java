package se.inera.log.messages;

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
