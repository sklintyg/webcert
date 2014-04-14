package se.inera.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Embeddable
public class IntygsReferens {

    public IntygsReferens() {
    }

    public IntygsReferens(String intygsId, String intygsTyp, String patientNamn,
                          LocalDateTime signeringsDatum) {
        this.intygsId = intygsId;
        this.intygsTyp = intygsTyp;
        this.patientNamn = patientNamn;
        this.signeringsDatum = signeringsDatum;
    }

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "INTYGS_TYP")
    private String intygsTyp;

    @Column(name = "PATIENT_NAMN")
    private String patientNamn;

    @Column(name = "SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime signeringsDatum;

    @Embedded
    private Id patientId;

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

    public LocalDateTime getSigneringsDatum() {
        return signeringsDatum;
    }

    public void setSigneringsDatum(LocalDateTime signeringsDatum) {
        this.signeringsDatum = signeringsDatum;
    }

    public Id getPatientId() {
        return patientId;
    }

    public void setPatientId(Id patientId) {
        this.patientId = patientId;
    }
}
