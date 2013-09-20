package se.inera.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Embeddable
public class IntygsReferens {
    
    @Column(name = "INTYGS_ID")
    private String intygsId;
    
    @Column(name = "INTYGS_TYP")
    private String intygsTyp;
    
    @Column(name = "PATIENT_NAMN")
    private String patientNamn;
    
    @Column(name = "PATIENT_PERSON_NUMMER")
    private String patientPersonNummer;
    
    @Column(name = "SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime signeringsDatum;

    
    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public String getPatientNamn() {
        return patientNamn;
    }

    public void setPatientNamn(String patientNamn) {
        this.patientNamn = patientNamn;
    }

    public String getPatientPersonNummer() {
        return patientPersonNummer;
    }

    public void setPatientPersonNummer(String patientPersonNummer) {
        this.patientPersonNummer = patientPersonNummer;
    }

    public LocalDateTime getSigneringsDatum() {
        return signeringsDatum;
    }

    public void setSigneringsDatum(LocalDateTime signeringsDatum) {
        this.signeringsDatum = signeringsDatum;
    }

}
