/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.arende.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MedicinsktArende {

    @Column(name = "FRAGE_ID")
    private String frageId;

    @Column(name = "INSTANS")
    private Integer instans;

    @Column(name = "TEXT")
    private String text;

    public String getFrageId() {
        return frageId;
    }

    public void setFrageId(String frageId) {
        this.frageId = frageId;
    }

    public Integer getInstans() {
        return instans;
    }

    public void setInstans(Integer instans) {
        this.instans = instans;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((frageId == null) ? 0 : frageId.hashCode());
        result = prime * result + ((instans == null) ? 0 : instans.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MedicinsktArende other = (MedicinsktArende) obj;
        if (frageId == null) {
            if (other.frageId != null) {
                return false;
            }
        } else if (!frageId.equals(other.frageId)) {
            return false;
        }
        if (instans == null) {
            if (other.instans != null) {
                return false;
            }
        } else if (!instans.equals(other.instans)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }

}
