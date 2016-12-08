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

package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

/**
 * Simple DTO for statistics for number of unsigned certificates and unhandled questions.
 *
 * @author marced
 */
public class StatEntry {
    private long unsignedCerts;
    private long unhandledQuestions;

    public StatEntry(long unsignedCerts, long unhandledQuestions) {
        super();
        this.unsignedCerts = unsignedCerts;
        this.unhandledQuestions = unhandledQuestions;
    }

    public long getUnsignedCerts() {
        return unsignedCerts;
    }

    public void setUnsignedCerts(long unsignedCerts) {
        this.unsignedCerts = unsignedCerts;
    }

    public long getUnhandledQuestions() {
        return unhandledQuestions;
    }

    public void setUnhandledQuestions(long unhandledQuestions) {
        this.unhandledQuestions = unhandledQuestions;
    }
}
