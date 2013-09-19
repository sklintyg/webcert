package se.inera.webcert.persistence;

import org.joda.time.LocalDateTime;

public class IntygsReferens {
    private String intygsId;
    private String intygsTyp;
    private String patientNamn;
    private String patientPersonNummer;

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

    private LocalDateTime signeringsDatum;

}
