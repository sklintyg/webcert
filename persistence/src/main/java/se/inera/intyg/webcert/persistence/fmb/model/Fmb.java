package se.inera.intyg.webcert.persistence.fmb.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Locale;

@Entity
@Table(name = "FMB")
public class Fmb {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ICD10")
    private String icd10;

    @Column(name = "TYP")
    @Enumerated(EnumType.STRING)
    private FmbType typ;

    @Column(name = "URSPRUNG")
    @Enumerated(EnumType.STRING)
    private FmbCallType ursprung;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "SENAST_UPPDATERAD")
    private String lastUpdate;

    Fmb() {
    }

    public Fmb(String icd10, FmbType type, FmbCallType callType, String text, String lastUpdate) {
        this.icd10 = icd10 != null ? icd10.toUpperCase(Locale.ENGLISH) : null;
        this.typ = type;
        this.ursprung = callType;
        this.text = text;
        this.lastUpdate = lastUpdate != null ? lastUpdate : "unknown";
    }

    public Long getId() {
        return id;
    }

    public String getIcd10() {
        return icd10;
    }

    public FmbType getTyp() {
        return typ;
    }

    public FmbCallType getUrsprung() {
        return ursprung;
    }

    public String getText() {
        return text;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

}
