package se.inera.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by pehr on 10/3/13.</br></br>
 *
 *   Patient_id_root can be either</br>
 *   personnummer : 1.2.752.129.2.1.3.1 </br>
 *   samordningsnummer : 1.2.752.129.2.1.3.3 </br>
 */
@Embeddable
public class Id {

    @Column(name = "PATIENT_ID_ROOT")
    private String patientIdRoot;
    @Column(name = "PATIENT_ID")
    private String patientId;

    public Id() {}

    public Id(String patientId) {
        this.patientId = patientId;
    }

    public Id(String patientIdRoot, String patientId) {
        this.patientIdRoot = patientIdRoot;
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String extension) {
        this.patientId = extension;
    }

    public String getPatientIdRoot() {
        return patientIdRoot;
    }

    public void setPatientIdRoot(String root) {
        this.patientIdRoot = root;
    }
}