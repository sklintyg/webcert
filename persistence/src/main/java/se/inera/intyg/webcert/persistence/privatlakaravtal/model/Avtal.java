package se.inera.intyg.webcert.persistence.privatlakaravtal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Entity
@Table(name = "AVTAL_PRIVATLAKARE")
public class Avtal {

    @Id
    @Column(name = "AVTAL_VERSION")
    private Integer avtalVersion;

    @Lob
    @Column(name = "AVTAL_TEXT")
    private String avtalText;

    @Column(name = "VERSION_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime versionDatum;

    public String getAvtalText() {
        return avtalText;
    }

    public void setAvtalText(String avtalText) {
        this.avtalText = avtalText;
    }

    public Integer getAvtalVersion() {
        return avtalVersion;
    }

    public void setAvtalVersion(Integer avtalVersion) {
        this.avtalVersion = avtalVersion;
    }

    public LocalDateTime getVersionDatum() {
        return versionDatum;
    }

    public void setVersionDatum(LocalDateTime versionDatum) {
        this.versionDatum = versionDatum;
    }
}
