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

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserStatisticsDTOTest {

    private static final String UNIT_ID = "unitId";
    private static final String NEW_UNIT_ID = "newUnitId";

    @Nested
    class MergeUnitStatistics {

        @Test
        void shallMergeExistingValues() {
            final var userStatistics = new UserStatisticsDTO();
            userStatistics.addUnitStatistics(UNIT_ID, new UnitStatisticsDTO(1, 1, 1, 1));
            userStatistics.mergeUnitStatistics(Map.of(UNIT_ID, new UnitStatisticsDTO(1, 1, 1, 1)));
            assertAll(
                () -> assertEquals(2, userStatistics.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
                () -> assertEquals(2, userStatistics.getUnitStatistics().get(UNIT_ID).getQuestionsOnSubUnits()),
                () -> assertEquals(2, userStatistics.getUnitStatistics().get(UNIT_ID).getQuestionsOnUnit()),
                () -> assertEquals(2, userStatistics.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits())
            );
        }

        @Test
        void shallAddNewValueIfItDontAlreadyExist() {
            final var userStatistics = new UserStatisticsDTO();
            userStatistics.addUnitStatistics(UNIT_ID, new UnitStatisticsDTO(1, 1, 1, 1));
            userStatistics.mergeUnitStatistics(Map.of(NEW_UNIT_ID, new UnitStatisticsDTO(1, 1, 1, 1)));
            assertAll(
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(UNIT_ID).getQuestionsOnSubUnits()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(UNIT_ID).getQuestionsOnUnit()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(NEW_UNIT_ID).getDraftsOnSubUnits()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(NEW_UNIT_ID).getQuestionsOnSubUnits()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(NEW_UNIT_ID).getQuestionsOnUnit()),
                () -> assertEquals(1, userStatistics.getUnitStatistics().get(NEW_UNIT_ID).getDraftsOnSubUnits())
            );
        }
    }

    @Nested
    class IncrementStatisticValues {

        @Test
        void shallIncrementNbrOfDraftsOnSelectedUnit() {
            final var userStatisticsDTO = new UserStatisticsDTO();
            userStatisticsDTO.setNbrOfDraftsOnSelectedUnit(1);
            userStatisticsDTO.addNbrOfDraftsOnSelectedUnit(1);
            assertEquals(2, userStatisticsDTO.getNbrOfDraftsOnSelectedUnit());
        }

        @Test
        void shallIncrementNbrOfUnhandledQuestionsOnSelectedUnit() {
            final var userStatisticsDTO = new UserStatisticsDTO();
            userStatisticsDTO.setNbrOfUnhandledQuestionsOnSelectedUnit(1);
            userStatisticsDTO.addNbrOfUnhandledQuestionsOnSelectedUnit(1);
            assertEquals(2, userStatisticsDTO.getNbrOfUnhandledQuestionsOnSelectedUnit());
        }

        @Test
        void shallIncrementTotalDraftsAndUnhandledQuestionsOnOtherUnits() {
            final var userStatisticsDTO = new UserStatisticsDTO();
            userStatisticsDTO.setTotalDraftsAndUnhandledQuestionsOnOtherUnits(1);
            userStatisticsDTO.addTotalDraftsAndUnhandledQuestionsOnOtherUnits(1);
            assertEquals(2, userStatisticsDTO.getTotalDraftsAndUnhandledQuestionsOnOtherUnits());
        }
    }
}
