package se.inera.webcert.persistence.intyg.model;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "OMSANDNING")
public class Omsandning {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
