/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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
 * Parameter object for DiagnosService.
 *
 * @author npet
 *
 */
public class DiagnosParameter {

    private String codeFragment;
    private String codeSystem;

    private String descriptionSearchString;

    // This will by default return all matches
    private int nbrOfResults = -1;

    public DiagnosParameter() {

    }

    public String getCodeFragment() {
        return codeFragment;
    }

    public void setCodeFragment(String codeFragment) {
        this.codeFragment = codeFragment;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getDescriptionSearchString() {
        return descriptionSearchString;
    }

    public void setDescriptionSearchString(String descriptionSearchString) {
        this.descriptionSearchString = descriptionSearchString;
    }

    public int getNbrOfResults() {
        return nbrOfResults;
    }

    public void setNbrOfResults(int nbrOfResults) {
        this.nbrOfResults = nbrOfResults;
    }
}
