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

package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class Enhet implements Serializable {

    private static final long serialVersionUID = 1L;
    private String enhetsId;
    private String enhetsNamn;

    private String vardgivareId;
    private String vardgivareNamn;

    public Enhet() {
    }

    public Enhet(String enhetsId, String vardgivareId) {
        this(enhetsId, null, vardgivareId, null);
    }

    public Enhet(String enhetsId, String enhetsNamn, String vardgivareId, String vardgivareNamn) {
        this.enhetsId = enhetsId;
        this.enhetsNamn = enhetsNamn;
        this.vardgivareId = vardgivareId;
        this.vardgivareNamn = vardgivareNamn;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public String getVardgivareId() {
        return vardgivareId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }
}
