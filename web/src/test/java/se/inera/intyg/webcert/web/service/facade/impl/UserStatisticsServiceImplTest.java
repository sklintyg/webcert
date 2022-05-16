/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserStatisticsServiceImplTest {

    final static String SELECTED_UNIT_ID = "UNITID";

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private UserStatisticsServiceImpl userStatisticsService;

    private WebCertUser user;

    void setUpUser() {
        user = mock(WebCertUser.class);

       doReturn(user)
            .when(webCertUserService)
            .getUser();
    }

    void setUpUnit() {
        final var unit = new Vardenhet();
        unit.setId(SELECTED_UNIT_ID);


        doReturn(unit)
                .when(user)
                .getValdVardenhet();

        doReturn(List.of(SELECTED_UNIT_ID, "Unit2", "Unit3")).when(user).getIdsOfAllVardenheter();
    }

    @Nested
    class NumberOfDraftsForSelectedUnit {
        @Test
        void shouldReturn0IfUserIsNull() {
            final var result = userStatisticsService.getUserStatistics().getNbrOfDraftsOnSelectedUnit();
            assertEquals(0L, result);
        }

        @Test
        void shouldReturn0IfUserHasOriginIntegrated() {
            setUpUser();
            user.setOrigin("DJUPINTEGRATION");

            final var result = userStatisticsService.getUserStatistics().getNbrOfDraftsOnSelectedUnit();

            assertEquals(0L, result);
        }

        @Test
        void shouldReturnCorrectValue() {
            setUpUser();
            setUpUnit();
            final var expectedValue = 100L;
            final var map = new HashMap<String, Long>();
            map.put(SELECTED_UNIT_ID, expectedValue);
            doReturn(map).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

            final var result = userStatisticsService.getUserStatistics().getNbrOfDraftsOnSelectedUnit();

            assertEquals(expectedValue, result);
        }
    }
}
