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

import java.io.Serializable;

public abstract class AbstractVardenhet implements SelectableVardenhet, Comparable<AbstractVardenhet>, Serializable {

    private static final long serialVersionUID = 304219756695002501L;

    private String id;

    private String namn;

    private String epost;

    private String postadress;

    private String postnummer;

    private String postort;

    private String telefonnummer;

    private String arbetsplatskod;

    public AbstractVardenhet() {
    }

    public AbstractVardenhet(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public String getArbetsplatskod() {
        return arbetsplatskod;
    }

    public void setArbetsplatskod(String arbetsplatskod) {
        this.arbetsplatskod = arbetsplatskod;
    }

    @Override
    public int compareTo(AbstractVardenhet annanVardenhet) {
        return getNamn().compareTo(annanVardenhet.getNamn());
    }

    @Override
    public String toString() {
        return new StringBuilder(getNamn()).append(":").append(getId()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractVardenhet)) {
            return false;
        }

        AbstractVardenhet that = (AbstractVardenhet) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
