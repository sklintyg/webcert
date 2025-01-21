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

package se.inera.intyg.webcert.web.csintegration.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.StatisticsForUnitDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UnitStatisticsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.user.UnitStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CertificateServiceStatisticServiceTest {

    private static final String UNIT_ID = "unitId";
    private static final List<String> UNIT_IDS = List.of("unit1", "unit2");
    private static final List<String> SELECTED_UNIT_IDS = List.of("unit1", "unit2");
    private static final String SUB_UNIT = "subUnit";
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
        certificateServiceStatisticService.add(userStatisticsDTO, Collections.emptyList(), user, false);
        assertAll(
            () -> assertEquals(0, userStatisticsDTO.getNbrOfDraftsOnSelectedUnit()),
            () -> assertEquals(0, userStatisticsDTO.getTotalDraftsAndUnhandledQuestionsOnOtherUnits()),
            () -> assertEquals(0, userStatisticsDTO.getNbrOfUnhandledQuestionsOnSelectedUnit()),
            () -> assertTrue(userStatisticsDTO.getUnitStatistics().isEmpty())
        );
    }

    @Nested
    class UserHasSelectedVardenhet {

        @BeforeEach
        void setUp() {
            final var vardenhet = new Vardenhet();
            vardenhet.setId(UNIT_ID);
            vardenhet.getHsaIds().add(SUB_UNIT);
            final var statisticsRequestDTO = UnitStatisticsRequestDTO.builder().build();
            final var statistics = buildStatisticsForUnitDTO();
            doReturn(statisticsRequestDTO).when(csIntegrationRequestFactory).getStatisticsRequest(UNIT_IDS);
            doReturn(statistics).when(csIntegrationService).getStatistics(statisticsRequestDTO);
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(vardenhet).when(user).getValdVardenhet();
            doReturn(SELECTED_UNIT_IDS).when(user).getIdsOfSelectedVardenhet();
        }

        @Test
        void shallIncrementNbrOfDraftsOnSelectedUnit() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user, false);
            assertEquals(2, userStatisticsDTO.getNbrOfDraftsOnSelectedUnit());
        }

        @Test
        void shallIncrementNbrOfUnhandledQuestionsOnSelectedUnit() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user, false);
            assertEquals(2, userStatisticsDTO.getNbrOfUnhandledQuestionsOnSelectedUnit());
        }

        @Test
        void shallIncrementTotalDraftsAndUnhandledQuestionsOnOtherUnits() {
            final var userStatisticsDTO = buildUserStatisticsDTO();
            certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user, false);
            assertEquals(5, userStatisticsDTO.getTotalDraftsAndUnhandledQuestionsOnOtherUnits());
        }
    }

    @Test
    void shallNotIncrementSubUnitStatisticsIfNoSubUnitIsPresent() {
        final var vardgivare = new Vardgivare();
        final var vardenhet = new Vardenhet();
        vardenhet.setId(UNIT_ID);
        vardenhet.setMottagningar(Collections.emptyList());
        vardgivare.getVardenheter().add(vardenhet);

        final var statisticsRequestDTO = UnitStatisticsRequestDTO.builder().build();
        final var statistics = buildStatisticsForUnitDTO();

        doReturn(statisticsRequestDTO).when(csIntegrationRequestFactory).getStatisticsRequest(UNIT_IDS);
        doReturn(statistics).when(csIntegrationService).getStatistics(statisticsRequestDTO);
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(List.of(vardgivare)).when(user).getVardgivare();
        doReturn(vardenhet).when(user).getValdVardenhet();
        doReturn(SELECTED_UNIT_IDS).when(user).getIdsOfSelectedVardenhet();

        final var userStatisticsDTO = buildUserStatisticsDTO();
        certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user, false);
        assertAll(
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnUnit()),
            () -> assertEquals(1, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnUnit()),
            () -> assertEquals(1, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnSubUnits())
        );
    }

    @Test
    void shallMergeUnitStatistics() {
        final var vardgivare = new Vardgivare();
        final var vardenhet = new Vardenhet();
        final var mottagning = new Mottagning();
        mottagning.setId(SUB_UNIT);
        vardenhet.setId(UNIT_ID);
        vardenhet.setMottagningar(List.of(mottagning));
        vardgivare.getVardenheter().add(vardenhet);

        final var statisticsRequestDTO = UnitStatisticsRequestDTO.builder().build();
        final var statistics = buildStatisticsForUnitDTO();

        doReturn(statisticsRequestDTO).when(csIntegrationRequestFactory).getStatisticsRequest(UNIT_IDS);
        doReturn(statistics).when(csIntegrationService).getStatistics(statisticsRequestDTO);
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(List.of(vardgivare)).when(user).getVardgivare();
        doReturn(vardenhet).when(user).getValdVardenhet();
        doReturn(SELECTED_UNIT_IDS).when(user).getIdsOfSelectedVardenhet();

        final var userStatisticsDTO = buildUserStatisticsDTO();
        certificateServiceStatisticService.add(userStatisticsDTO, UNIT_IDS, user, false);
        assertAll(
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnUnit()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getDraftsOnSubUnits()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnUnit()),
            () -> assertEquals(2, userStatisticsDTO.getUnitStatistics().get(UNIT_ID).getQuestionsOnSubUnits())
        );
    }

    private static Map<String, StatisticsForUnitDTO> buildStatisticsForUnitDTO() {
        return Map.of(
            UNIT_ID, StatisticsForUnitDTO.builder()
                .draftCount(1)
                .unhandledMessageCount(1)
                .build(),
            SUB_UNIT, StatisticsForUnitDTO.builder()
                .draftCount(1)
                .unhandledMessageCount(1)
                .build());
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
