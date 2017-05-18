package se.inera.intyg.webcert.common.model;

import se.inera.intyg.common.support.common.enumerations.RelationKod;

import java.time.LocalDateTime;

/**
 * Encapsulates a single relation. Instances of this class should always exist in the context of a {@link Relations} instance,
 * either as parent relation or a child relation.
 *
 * Created by eriklupander on 2017-05-17.
 */
public class WebcertCertificateRelation {

    /**
     * Given the context of a certficate, the intygsId always denotes the other certificate in the relation.
     */
    private String intygsId;

    /**
     * The type of the relation. Note that the enumerations are bi-directional. E.g - an ERSATT is used both for parent
     * and child relations even though the swedish verbs would be: "Ersatt av" and "Ersätter" respectively.
     */
    private RelationKod relationKod;

    /**
     * Creation datetime of the relation.
     */
    private LocalDateTime skapad;

    /**
     * Given the context of a certificate, this status always denotes the status of the target (other) certificate.
     */
    private UtkastStatus status;

    public WebcertCertificateRelation(String intygsId, RelationKod relationKod, LocalDateTime skapad, UtkastStatus status) {
        this.intygsId = intygsId;
        this.relationKod = relationKod;
        this.skapad = skapad;
        this.status = status;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public RelationKod getRelationKod() {
        return relationKod;
    }

    public void setRelationKod(RelationKod relationKod) {
        this.relationKod = relationKod;
    }

    public LocalDateTime getSkapad() {
        return skapad;
    }

    public void setSkapad(LocalDateTime skapad) {
        this.skapad = skapad;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WebcertCertificateRelation)) {
            return false;
        }

        WebcertCertificateRelation that = (WebcertCertificateRelation) o;

        if (!intygsId.equals(that.intygsId)) {
            return false;
        }
        if (relationKod != that.relationKod) {
            return false;
        }
        return skapad.equals(that.skapad);
    }

    // CHECKSTYLE:OFF MagicNumber
    @Override
    public int hashCode() {
        int result = intygsId.hashCode();
        result = 31 * result + relationKod.hashCode();
        result = 31 * result + skapad.hashCode();
        return result;
    }
    // CHECKSTYLE:ON MagicNumber
}
