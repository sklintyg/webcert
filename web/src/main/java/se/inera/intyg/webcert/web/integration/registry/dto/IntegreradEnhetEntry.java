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

package se.inera.intyg.webcert.web.integration.registry.dto;

public class IntegreradEnhetEntry implements Comparable<IntegreradEnhetEntry> {

    private String enhetsId;

    private String enhetsNamn;

    private String vardgivareId;

    private String vardgivareNamn;

    public IntegreradEnhetEntry(String enhetsId, String vardgivareId) {
        super();
        this.enhetsId = enhetsId;
        this.vardgivareId = vardgivareId;
    }

    public IntegreradEnhetEntry(String enhetsId, String enhetsNamn, String vardgivareId, String vardgivareNamn) {
        super();
        this.enhetsId = enhetsId;
        this.enhetsNamn = enhetsNamn;
        this.vardgivareId = vardgivareId;
        this.vardgivareNamn = vardgivareNamn;
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

    public String getVardgivareId() {
        return vardgivareId;
    }

    public void setVardgivareId(String vardgivareId) {
        this.vardgivareId = vardgivareId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }

    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }

    @Override
    public int compareTo(IntegreradEnhetEntry other) {
        int vgComp = getVardgivareId().compareTo(other.getVardgivareId());
        return (vgComp == 0) ? getEnhetsId().compareTo(other.getEnhetsId()) : vgComp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((enhetsId == null) ? 0 : enhetsId.hashCode());
        result = prime * result + ((vardgivareId == null) ? 0 : vardgivareId.hashCode());
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
        IntegreradEnhetEntry other = (IntegreradEnhetEntry) obj;
        if (enhetsId == null) {
            if (other.enhetsId != null) {
                return false;
            }
        } else if (!enhetsId.equals(other.enhetsId)) {
            return false;
        }
        if (vardgivareId == null) {
            if (other.vardgivareId != null) {
                return false;
            }
        } else if (!vardgivareId.equals(other.vardgivareId)) {
            return false;
        }
        return true;
    }

}
