package se.inera.webcert.persistence.intyg.model;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OMSANDNING")
public class Omsandning {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "OMSANDNING_ID")
    private Long id;

    @Column(name = "INTYG_ID")
    private String intygId;

    @Column(name = "NASTA_FORSOK")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime nastaForsok;

    @Column(name = "ANTAL_FORSOK")
    private int antalForsok;

    @Column(name = "GALLRINGSDATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime gallringsdatum;
    
    @Column(name = "OPERATION")
    @Enumerated(EnumType.STRING)
    private OmsandningOperation operation;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "KONFIGURATION")
    private String configuration;

    public Omsandning(OmsandningOperation operation, String intygId) {
        super();
        this.intygId = intygId;
        this.operation = operation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public LocalDateTime getNastaForsok() {
        return nastaForsok;
    }

    public void setNastaForsok(LocalDateTime nastaForsok) {
        this.nastaForsok = nastaForsok;
    }

    public int getAntalForsok() {
        return antalForsok;
    }

    public void setAntalForsok(int antalForsok) {
        this.antalForsok = antalForsok;
    }

    public LocalDateTime getGallringsdatum() {
        return gallringsdatum;
    }

    public void setGallringsdatum(LocalDateTime gallringsdatum) {
        this.gallringsdatum = gallringsdatum;
    }

    public OmsandningOperation getOperation() {
        return operation;
    }

    public void setOperation(OmsandningOperation operation) {
        this.operation = operation;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
}
