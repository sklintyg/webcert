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

package se.inera.intyg.webcert.integration.hsa.stub;

import java.util.ArrayList;
import java.util.List;

public class HsaPerson {

    private String hsaId;

    private String forNamn;

    private String efterNamn;

    private List<HsaSpecialicering> specialiseringar = new ArrayList<HsaSpecialicering>();

    private List<String> enhetIds = new ArrayList<String>();

    private String titel;

    private List<String> legitimeradeYrkesgrupper = new ArrayList<String>();

    private String befattningsKod;

    private String forskrivarKod;


    // ~ Constructors
    // ~ =====================================================================================

    public HsaPerson() {
        super();
    }

    public HsaPerson(String hsaId, String forNamn, String efterNamn) {
        super();
        this.hsaId = hsaId;
        this.forNamn = forNamn;
        this.efterNamn = efterNamn;
    }


    // ~ Getters and setters
    // ~ =====================================================================================

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getForNamn() {
        return forNamn;
    }

    public void setForNamn(String forNamn) {
        this.forNamn = forNamn;
    }

    public String getEfterNamn() {
        return efterNamn;
    }

    public void setEfterNamn(String efterNamn) {
        this.efterNamn = efterNamn;
    }

    public List<HsaSpecialicering> getSpecialiseringar() {
        return specialiseringar;
    }

    public void setSpecialiseringar(List<HsaSpecialicering> specialiseringar) {
        this.specialiseringar = specialiseringar;
    }

    public List<String> getEnhetIds() {
        return enhetIds;
    }

    public void setEnhetIds(List<String> enhetIds) {
        this.enhetIds = enhetIds;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public List<String> getLegitimeradeYrkesgrupper() {
        return legitimeradeYrkesgrupper;
    }

    public void setLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        this.legitimeradeYrkesgrupper = legitimeradeYrkesgrupper;
    }

    public String getBefattningsKod() {
        return befattningsKod;
    }

    public void setBefattningsKod(String befattningsKod) {
        this.befattningsKod = befattningsKod;
    }

    public String getForskrivarKod() {
        return forskrivarKod;
    }

    public void setForskrivarKod(String forskrivarKod) {
        this.forskrivarKod = forskrivarKod;
    }

}
