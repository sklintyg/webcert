package se.inera.intyg.webcert.persistence.integreradenhet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

/**
 * Entity for a Vardenhet that uses to integrate with Webcert using journaling systems.
 *
 * @author nikpet
 */
@Entity
@Table(name = "INTEGRERADE_VARDENHETER")
public class IntegreradEnhet {

    @Id
    @Column(name = "ENHETS_ID")
    private String enhetsId;

    @Column(name = "ENHETS_NAMN")
    private String enhetsNamn;

    @Column(name = "VARDGIVAR_ID")
    private String vardgivarId;

    @Column(name = "VARDGIVAR_NAMN")
    private String vardgivarNamn;

    @Column(name = "SKAPAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime skapadDatum;

    @Column(name = "SENASTE_KONTROLL_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime senasteKontrollDatum;

    @PrePersist
    void onPrePersist() {
        if (skapadDatum == null) {
            skapadDatum = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onPreUpdate() {
        senasteKontrollDatum = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IntegreradEnhet [enhetsId=" + enhetsId + ", enhetsNamn=" + enhetsNamn + ", vardgivarId=" + vardgivarId + ", vardgivarNamn="
                + vardgivarNamn + ", skapadDatum=" + skapadDatum + ", senasteKontrollDatum=" + senasteKontrollDatum + "]";
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public String getVardgivarNamn() {
        return vardgivarNamn;
    }

    public void setVardgivarNamn(String vardgivarNamn) {
        this.vardgivarNamn = vardgivarNamn;
    }

    public LocalDateTime getSkapadDatum() {
        return skapadDatum;
    }

    public void setSkapadDatum(LocalDateTime skapadDatum) {
        this.skapadDatum = skapadDatum;
    }

    public LocalDateTime getSenasteKontrollDatum() {
        return senasteKontrollDatum;
    }

    public void setSenasteKontrollDatum(LocalDateTime senasteKontrollDatum) {
        this.senasteKontrollDatum = senasteKontrollDatum;
    }
}
