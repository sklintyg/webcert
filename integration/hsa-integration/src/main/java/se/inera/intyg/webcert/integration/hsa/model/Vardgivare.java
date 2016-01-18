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

package se.inera.intyg.webcert.integration.hsa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Vardgivare implements SelectableVardenhet, Comparable<Vardgivare>, Serializable {

    private static final long serialVersionUID = 4462766290949153158L;

    private String id;
    private String namn;

    private List<Vardenhet> vardenheter;

    public Vardgivare() {
    }

    public Vardgivare(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Vardenhet> getVardenheter() {

        if (vardenheter == null) {
            vardenheter = new ArrayList<>();
        }

        return vardenheter;
    }

    public void setVardenheter(List<Vardenhet> vardenheter) {
        this.vardenheter = vardenheter;
    }

    @JsonIgnore
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        for (Vardenhet vardenhet : getVardenheter()) {
            ids.addAll(vardenhet.getHsaIds());
        }
        return ids;
    }

    public SelectableVardenhet findVardenhet(String id) {

        if (id == null) {
            return null;
        }

        SelectableVardenhet sve = null;

        for (Vardenhet vardenhet : getVardenheter()) {
            sve = vardenhet.findSelectableVardenhet(id);
            if (sve != null) {
                return sve;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Vardgivare that = (Vardgivare) o;

            if (id == null) {
                return that.id == null;
            } else {
                return id.equals(that.id);
            }
        }
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Vardgivare annanVardgivare) {
        return getNamn().compareTo(annanVardgivare.getNamn());
    }

    @Override
    public String toString() {
        return new StringBuilder(getNamn()).append(":").append(getId()).toString();
    }
}
