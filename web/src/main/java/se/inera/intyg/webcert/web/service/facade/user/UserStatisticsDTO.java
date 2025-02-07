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

import java.util.HashMap;
import java.util.Map;

public class UserStatisticsDTO {

    private long nbrOfDraftsOnSelectedUnit;

    private long nbrOfUnhandledQuestionsOnSelectedUnit;

    private long totalDraftsAndUnhandledQuestionsOnOtherUnits;

    private Map<String, UnitStatisticsDTO> unitStatistics = new HashMap<>();

    public UserStatisticsDTO() {
    }

    public long getNbrOfDraftsOnSelectedUnit() {
        return nbrOfDraftsOnSelectedUnit;
    }

    public void setNbrOfDraftsOnSelectedUnit(long nbrOfDraftsOnSelectedUnit) {
        this.nbrOfDraftsOnSelectedUnit = nbrOfDraftsOnSelectedUnit;
    }

    public long getNbrOfUnhandledQuestionsOnSelectedUnit() {
        return nbrOfUnhandledQuestionsOnSelectedUnit;
    }

    public void setNbrOfUnhandledQuestionsOnSelectedUnit(long nbrOfUnhandledQuestionsOnSelectedUnit) {
        this.nbrOfUnhandledQuestionsOnSelectedUnit = nbrOfUnhandledQuestionsOnSelectedUnit;
    }

    public long getTotalDraftsAndUnhandledQuestionsOnOtherUnits() {
        return totalDraftsAndUnhandledQuestionsOnOtherUnits;
    }

    public void setTotalDraftsAndUnhandledQuestionsOnOtherUnits(long totalDraftsAndUnhandledQuestionsOnOtherUnits) {
        this.totalDraftsAndUnhandledQuestionsOnOtherUnits = totalDraftsAndUnhandledQuestionsOnOtherUnits;
    }

    public Map<String, UnitStatisticsDTO> getUnitStatistics() {
        return unitStatistics;
    }

    public void addUnitStatistics(String unitId, UnitStatisticsDTO statistics) {
        unitStatistics.put(unitId, statistics);
    }

    public void mergeUnitStatistics(Map<String, UnitStatisticsDTO> unitStatistics) {
        unitStatistics.forEach((key, value) ->
            this.unitStatistics.merge(key, value, (unitStatistics1, unitStatistics2) -> {
                unitStatistics1.merge(unitStatistics2);
                return unitStatistics1;
            })
        );
    }

    public void addNbrOfDraftsOnSelectedUnit(long nbrOfDraftsOnSelectedUnit) {
        this.nbrOfDraftsOnSelectedUnit += nbrOfDraftsOnSelectedUnit;
    }

    public void addNbrOfUnhandledQuestionsOnSelectedUnit(long nbrOfUnhandledQuestionsOnSelectedUnit) {
        this.nbrOfUnhandledQuestionsOnSelectedUnit += nbrOfUnhandledQuestionsOnSelectedUnit;
    }

    public void addTotalDraftsAndUnhandledQuestionsOnOtherUnits(long totalDraftsAndUnhandledQuestionsOnOtherUnits) {
        this.totalDraftsAndUnhandledQuestionsOnOtherUnits += totalDraftsAndUnhandledQuestionsOnOtherUnits;
    }
}
