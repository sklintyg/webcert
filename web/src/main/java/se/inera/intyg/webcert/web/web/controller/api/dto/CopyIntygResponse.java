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
package se.inera.intyg.webcert.web.web.controller.api.dto;

public class CopyIntygResponse {

    private String intygsUtkastId;
    private String intygsTyp;
    private String intygTypeVersion;

    public CopyIntygResponse() {
        super();
    }

    public CopyIntygResponse(String intygsUtkastId, String intygsTyp, String intygTypeVersion) {
        this.intygsUtkastId = intygsUtkastId;
        this.intygsTyp = intygsTyp;
        this.intygTypeVersion = intygTypeVersion;
    }

    public String getIntygsUtkastId() {
        return intygsUtkastId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public void setIntygsUtkastId(String intygsUtkastId) {
        this.intygsUtkastId = intygsUtkastId;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public void setIntygTypeVersion(String intygTypeVersion) {
        this.intygTypeVersion = intygTypeVersion;
    }

}
