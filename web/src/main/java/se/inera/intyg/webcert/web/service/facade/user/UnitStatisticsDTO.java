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
package se.inera.intyg.webcert.web.service.facade.user;

public class UnitStatisticsDTO {

    private long draftsOnUnit;
    private long questionsOnUnit;
    private long draftsOnSubUnits;
    private long questionsOnSubUnits;

    public UnitStatisticsDTO() {
    }

    public UnitStatisticsDTO(long draftsOnUnit, long questionsOnUnit) {
        this.draftsOnUnit = draftsOnUnit;
        this.questionsOnUnit = questionsOnUnit;
    }

    public UnitStatisticsDTO(long draftsOnUnit, long questionsOnUnit, long draftsOnSubUnits, long questionsOnSubUnits) {
        this.draftsOnUnit = draftsOnUnit;
        this.questionsOnUnit = questionsOnUnit;
        this.draftsOnSubUnits = draftsOnSubUnits;
        this.questionsOnSubUnits = questionsOnSubUnits;
    }

    public long getDraftsOnUnit() {
        return draftsOnUnit;
    }

    public void setDraftsOnUnit(long draftsOnUnit) {
        this.draftsOnUnit = draftsOnUnit;
    }

    public long getQuestionsOnUnit() {
        return questionsOnUnit;
    }

    public void setQuestionsOnUnit(long questionsOnUnit) {
        this.questionsOnUnit = questionsOnUnit;
    }

    public long getDraftsOnSubUnits() {
        return draftsOnSubUnits;
    }

    public void setDraftsOnSubUnits(long draftsOnSubUnits) {
        this.draftsOnSubUnits = draftsOnSubUnits;
    }

    public long getQuestionsOnSubUnits() {
        return questionsOnSubUnits;
    }

    public void setQuestionsOnSubUnits(long questionsOnSubUnits) {
        this.questionsOnSubUnits = questionsOnSubUnits;
    }

    public void merge(UnitStatisticsDTO unitStatisticsDTO) {
        this.draftsOnUnit += unitStatisticsDTO.draftsOnUnit;
        this.questionsOnUnit += unitStatisticsDTO.questionsOnUnit;
        this.draftsOnSubUnits += unitStatisticsDTO.draftsOnSubUnits;
        this.questionsOnSubUnits += unitStatisticsDTO.questionsOnSubUnits;
    }

    public void addDraftsOnSubUnits(long draftsOnSubUnits) {
        this.draftsOnSubUnits += draftsOnSubUnits;
    }

    public void addQuestionsOnSubUnits(long questionsOnSubUnits) {
        this.questionsOnSubUnits += questionsOnSubUnits;
    }
}
