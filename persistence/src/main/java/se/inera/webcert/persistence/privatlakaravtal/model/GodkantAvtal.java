package se.inera.webcert.persistence.privatlakaravtal.model;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Table(name = "GODKANT_AVTAL_PRIVATLAKARE")
@Entity
public class GodkantAvtal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long internReferens;

    @Column(name = "AVTAL_VERSION")
    private Integer avtalVersion;

    // TODO refactor when we know the exact term used for identifiying privatlakare (personId, hsaId?)
    @Column(name = "ANVANDAR_ID")
    private String anvandarId;

    @Column(name = "GODKAND_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime godkandDatum;

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public Integer getAvtalVersion() {
        return avtalVersion;
    }

    public void setAvtalVersion(Integer avtalVersion) {
        this.avtalVersion = avtalVersion;
    }

    public String getAnvandarId() {
        return anvandarId;
    }

    public void setAnvandarId(String anvandarId) {
        this.anvandarId = anvandarId;
    }

    public LocalDateTime getGodkandDatum() {
        return godkandDatum;
    }

    public void setGodkandDatum(LocalDateTime goodkandDatum) {
        this.godkandDatum = goodkandDatum;
    }
}
