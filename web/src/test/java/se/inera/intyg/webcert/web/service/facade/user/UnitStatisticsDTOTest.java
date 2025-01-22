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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnitStatisticsDTOTest {

    @Test
    void shallMergeValues() {
        final var unitStatistics = new UnitStatisticsDTO(1, 1, 1, 1);
        unitStatistics.merge(new UnitStatisticsDTO(2, 2, 2, 2));
        assertAll(
            () -> assertEquals(3, unitStatistics.getDraftsOnSubUnits()),
            () -> assertEquals(3, unitStatistics.getQuestionsOnSubUnits()),
            () -> assertEquals(3, unitStatistics.getQuestionsOnUnit()),
            () -> assertEquals(3, unitStatistics.getDraftsOnSubUnits())
        );
    }

    @Test
    void shallIncreaseDraftsOnSubUnits() {
        final var unitStatistics = new UnitStatisticsDTO(1, 1, 1, 1);
        unitStatistics.addDraftsOnSubUnits(1);
        assertEquals(2, unitStatistics.getDraftsOnSubUnits());
    }

    @Test
    void shallIncreaseQuestionsOnSubUnits() {
        final var unitStatistics = new UnitStatisticsDTO(1, 1, 1, 1);
        unitStatistics.addQuestionsOnSubUnits(1);
        assertEquals(2, unitStatistics.getQuestionsOnSubUnits());
    }
}
