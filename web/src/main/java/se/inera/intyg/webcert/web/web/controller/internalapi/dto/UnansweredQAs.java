/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

public class UnansweredQAs {

    int complement;
    int others;

    public UnansweredQAs() {
    }

    public UnansweredQAs(int complement, int others) {
        this.complement = complement;
        this.others = others;
    }

    public void incrementComplement() {
        this.complement += 1;
    }

    public void incrementOther() {
        this.others += 1;
    }

    public int getComplement() {
        return complement;
    }

    public int getOthers() {
        return others;
    }

    public void setComplement(int complement) {
        this.complement = complement;
    }

    public void setOthers(int others) {
        this.others = others;
    }

    public UnansweredQAs add(UnansweredQAs newValue) {
        this.complement += newValue.getComplement();
        this.others += newValue.getOthers();
        return this;
    }
}
