package se.inera.intyg.webcert.persistence.anvandarmetadata.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by eriklupander on 2016-06-22.
 *
 * Note that unique constraint is handled by liquibase DB setup.
 */
@Entity
@Table(name = "ANVANDARE_PREFERENCE")
public class AnvandarPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long internReferens;

    @Column(name = "HSA_ID", nullable = false)
    private String hsaId;

    @Column(name = "KEY", nullable = false)
    private String key;

    @Column(name = "VALUE", nullable = true)
    private String value;

    public AnvandarPreference() {

    }

    public AnvandarPreference(String hsaId, String key, String value) {
        this.hsaId = hsaId;
        this.key = key;
        this.value = value;
    }

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnvandarPreference)) return false;

        AnvandarPreference that = (AnvandarPreference) o;

        if (!hsaId.equals(that.hsaId)) return false;
        if (!key.equals(that.key)) return false;
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = hsaId.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
