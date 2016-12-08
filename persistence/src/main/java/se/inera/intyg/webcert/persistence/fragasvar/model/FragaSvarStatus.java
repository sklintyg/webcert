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

package se.inera.intyg.webcert.persistence.fragasvar.model;

import se.inera.intyg.webcert.persistence.model.Status;

public class FragaSvarStatus {

    private Long fragaSvarId;

    private String frageStallare;

    private String svarsText;

    private Status status;

    public FragaSvarStatus(Long fragaSvarId, String frageStallare, String svarsText, Status status) {
        super();
        this.fragaSvarId = fragaSvarId;
        this.frageStallare = frageStallare;
        this.svarsText = svarsText;
        this.status = status;
    }

    public Long getFragaSvarId() {
        return fragaSvarId;
    }

    public String getFrageStallare() {
        return frageStallare;
    }

    public String getSvarsText() {
        return svarsText;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasAnswerSet() {
        return svarsText != null;
    }

    public boolean isClosed() {
        return status.equals(Status.CLOSED);
    }

}
