/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.StatisticsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.user.UnitStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CertificateServiceStatisticServiceTest {

    private static final String UNIT_ID = "unitId";
    private static final List<String> UNIT_IDS = List.of("unit1", "unit2");
    private static final List<String> SELECTED_UNIT_IDS = List.of("unit1", "unit2");
    @Mock
    private WebCertUser user;
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private CertificateServiceStatisticService certificateServiceStatisticService;


    @Test
    void shallNotAddAnythingIfCertificateServiceProfileIsNotActive() {
        final var userStatisticsDTO = new UserStatisticsDTO();
        doReturn(false).when(certificateServiceProfile).active();
        certificateServiceStatisticService.add(userStatisticsDTO, Collections.emptyList(), user);
        assertAll(
            () -> assertEquals(0, userStatisticsDTO.getNbrOfDraftsOnSelectedUnit()),
            () -> assertEquals(0, userStatisticsDTO.getTotalDraftsAndUnhandledQuestionsOnOtherUnits()),
            () -> assertEquals(0, userStatisticsDTO.getNbrOfUnhandledQuestionsOnSelectedUnit()),
            () -> assertNull(userStatisticsDTO.getUnitStatistics())
        );
    }

    @Nested
    class UserHasSelectedVardenhet {

        @BeforeEach
        void setUp() {
            final var vardenhet = new Vardenhet();
            final var statisticsRequestDTO = StatisticsRequestDTO.builder().build();
            final var userStatisticsDTO = buildUserStatisticsDTO();
            doReturn(statisticsRequestDTO).when(csIntegrationRequestFactory).getStatisticsRequest(UNIT_IDS, SELECTED_UNIT_IDS);
            doReturn(userStatisticsDTO).when(csIntegrationService).getStatistics(statisticsRequestDTO);
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(vardenhet).when(user).getValdVardenhet();
            doReturn(SELECTED_UNIT_IDS).when(user).getIdsOfSelectedVardenhet();
        }

        @Test
        void shallIncrementNbrOfDraftsOnSelectedUnit() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user);
            assertEquals(2, userStatisticsDTO.getNbrOfDraftsOnSelectedUnit());
        }

        @Test
        void shallIncrementNbrOfUnhandledQuestionsOnSelectedUnit() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user);
            assertEquals(2, userStatisticsDTO.getNbrOfUnhandledQuestionsOnSelectedUnit());
        }

        @Test
        void shallIncrementTotalDraftsAndUnhandledQuestionsOnOtherUnits() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user);
            assertEquals(2, userStatisticsDTO.getTotalDraftsAndUnhandledQuestionsOnOtherUnits());
        }
    }

    @Test
    void shallMergeUnitStatistics() {
        final var statisticsRequestDTO = StatisticsRequestDTO.builder().build();
        final var userStatisticsDTOFromCS = buildUserStatisticsDTO();
        doReturn(statisticsRequestDTO).when(csIntegrationRequestFactory).getStatisticsRequest(UNIT_IDS, SELECTED_UNIT_IDS);
        doReturn(userStatisticsDTOFromCS).when(csIntegrationService).getStatistics(statisticsRequestDTO);
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(SELECTED_UNIT_IDS).when(user).getIdsOfSelectedVardenhet();
        
        final var userStatisticsDTO = buildUserStatisticsDTO();
        certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user);
        assertAll(
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnUnit()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnUnit()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnSubUnits())
        );
    }

    private static UserStatisticsDTO buildUserStatisticsDTO() {
        final var userStatisticsDTO = new UserStatisticsDTO();
        userStatisticsDTO.setNbrOfDraftsOnSelectedUnit(1);
        userStatisticsDTO.setTotalDraftsAndUnhandledQuestionsOnOtherUnits(1);
        userStatisticsDTO.setNbrOfUnhandledQuestionsOnSelectedUnit(1);
        userStatisticsDTO.addUnitStatistics(
            UNIT_ID, new UnitStatisticsDTO(1, 1, 1, 1)
        );
        return userStatisticsDTO;
    }
}
