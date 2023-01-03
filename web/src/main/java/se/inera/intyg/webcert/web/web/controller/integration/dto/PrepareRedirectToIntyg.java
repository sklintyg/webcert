/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integration.dto;

import java.io.Serializable;

/**
 * @author Magnus Ekstrand on 2017-10-12.
 */
public class PrepareRedirectToIntyg implements Serializable {

    private String intygTyp;
    private String intygTypeVersion;
    private String intygId;

    private boolean utkast;

    // getters and setters

    public String getIntygTyp() {
        return intygTyp;
    }

    public void setIntygTyp(String intygTyp) {
        this.intygTyp = intygTyp;
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public boolean isUtkast() {
        return utkast;
    }

    public void setUtkast(boolean utkast) {
        this.utkast = utkast;
    }

    @Override
    public String toString() {
        return "PrepareRedirectToIntyg {"
            + "intygTyp='" + intygTyp + '\''
            + "intygTypeVersion='" + intygTypeVersion + '\''
            + ", intygId='" + intygId + '\''
            + ", utkast=" + utkast
            + "}";
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public void setIntygTypeVersion(String intygTypeVersion) {
        this.intygTypeVersion = intygTypeVersion;
    }
}
