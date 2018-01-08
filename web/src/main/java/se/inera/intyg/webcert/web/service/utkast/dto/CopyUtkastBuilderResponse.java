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
package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public class CopyUtkastBuilderResponse {

    private Utkast utkastCopy;

    private String orginalEnhetsId;

    private String orginalEnhetsNamn;

    private String orginalVardgivarId;

    private String orginalVardgivarNamn;

    public Utkast getUtkastCopy() {
        return utkastCopy;
    }

    public void setUtkastCopy(Utkast utkastCopy) {
        this.utkastCopy = utkastCopy;
    }

    public String getOrginalEnhetsId() {
        return orginalEnhetsId;
    }

    public void setOrginalEnhetsId(String orginalEnhetsId) {
        this.orginalEnhetsId = orginalEnhetsId;
    }

    public String getOrginalEnhetsNamn() {
        return orginalEnhetsNamn;
    }

    public void setOrginalEnhetsNamn(String orginalEnhetsNamn) {
        this.orginalEnhetsNamn = orginalEnhetsNamn;
    }

    public String getOrginalVardgivarId() {
        return orginalVardgivarId;
    }

    public void setOrginalVardgivarId(String orginalVardgivarId) {
        this.orginalVardgivarId = orginalVardgivarId;
    }

    public String getOrginalVardgivarNamn() {
        return orginalVardgivarNamn;
    }

    public void setOrginalVardgivarNamn(String orginalVardgivarNamn) {
        this.orginalVardgivarNamn = orginalVardgivarNamn;
    }
}
