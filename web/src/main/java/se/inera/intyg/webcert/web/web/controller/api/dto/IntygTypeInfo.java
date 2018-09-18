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
package se.inera.intyg.webcert.web.web.controller.api.dto;

/**
 * Created by marced on 2018-09-17.
 */
public class IntygTypeInfo {
    private String intygId;
    private String intygType;
    private String intygTypeVersion;

    public IntygTypeInfo(String intygId, String intygType, String intygTypeVersion) {
        this.intygId = intygId;
        this.intygType = intygType;
        this.intygTypeVersion = intygTypeVersion;
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public String getIntygId() {
        return intygId;
    }

    public String getIntygType() {
        return intygType;
    }
}
