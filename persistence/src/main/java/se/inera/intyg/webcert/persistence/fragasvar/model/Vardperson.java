/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Vardperson {
    @Column(name = "HSAID")
    private String hsaId;

    @Column(name = "NAMN")
    private String namn;

    @Column(name = "FORSKRIVAR_KOD")
    private String forskrivarKod;

    @Column(name = "ENHETS_ID")
    private String enhetsId;

    @Column(name = "ARBETSPLATS_KOD")
    private String arbetsplatsKod;

    @Column(name = "ENHETSNAMN")
    private String enhetsnamn;

    @Column(name = "POSTADRESS")
    private String postadress;

    @Column(name = "POSTNUMMER")
    private String postnummer;

    @Column(name = "POSTORT")
    private String postort;

    @Column(name = "TELEFONNUMMER")
    private String telefonnummer;

    @Column(name = "EPOST")
    private String epost;

    @Column(name = "VARDGIVAR_ID")
    private String vardgivarId;

    @Column(name = "VARDGIVARNAMN")
    private String vardgivarnamn;

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getForskrivarKod() {
        return forskrivarKod;
    }

    public void setForskrivarKod(String forskrivarKod) {
        this.forskrivarKod = forskrivarKod;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getArbetsplatsKod() {
        return arbetsplatsKod;
    }

    public void setArbetsplatsKod(String arbetsplatsKod) {
        this.arbetsplatsKod = arbetsplatsKod;
    }

    public String getEnhetsnamn() {
        return enhetsnamn;
    }

    public void setEnhetsnamn(String enhetsnamn) {
        this.enhetsnamn = enhetsnamn;
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

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public String getVardgivarnamn() {
        return vardgivarnamn;
    }

    public void setVardgivarnamn(String vardgivarnamn) {
        this.vardgivarnamn = vardgivarnamn;
    }

}
