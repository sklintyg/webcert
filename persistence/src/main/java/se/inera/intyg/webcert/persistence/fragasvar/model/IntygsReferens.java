package se.inera.intyg.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

@Embeddable
public class IntygsReferens {

    public IntygsReferens() {
    }

    public IntygsReferens(String intygsId, String intygsTyp, Personnummer patientId,
                          String patientName, LocalDateTime signeringsDatum) {
        this.intygsId = intygsId;
        this.intygsTyp = intygsTyp;
        this.patientId = patientId.getPersonnummer();
        this.patientNamn = patientName;
        this.signeringsDatum = signeringsDatum;
    }

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "INTYGS_TYP")
    private String intygsTyp;

    @Column(name = "PATIENT_ID")
    private String patientId;

    @Column(name = "PATIENT_NAMN")
    private String patientNamn;

    @Column(name = "SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime signeringsDatum;

    /*
    @Embedded
    private String patientId;
    */

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

    public Personnummer getPatientId() {
        return new Personnummer(patientId);
    }

    public void setPatientId(Personnummer patientId) {
        this.patientId = patientId.getPersonnummer();
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
}
