package se.inera.webcert.persistence.privatlakaravtal.model;

import javax.persistence.*;

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
}
