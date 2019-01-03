/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.common.model;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;

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

    /**
     * Given the context of a certificate, this status always denotes the status of the target (other) certificate.
     */
    private boolean makulerat;

    public WebcertCertificateRelation(String intygsId, RelationKod relationKod, LocalDateTime skapad,
                                      UtkastStatus status, boolean makulerat) {
        this.intygsId = intygsId;
        this.relationKod = relationKod;
        this.skapad = skapad;
        this.status = status;
        this.makulerat = makulerat;
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

    public boolean isMakulerat() {
        return makulerat;
    }

    public void setMakulerat(boolean makulerat) {
        this.makulerat = makulerat;
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
