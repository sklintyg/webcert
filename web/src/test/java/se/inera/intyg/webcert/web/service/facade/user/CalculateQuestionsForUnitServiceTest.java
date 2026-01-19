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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceStatisticService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CalculateQuestionsForUnitServiceTest {

    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String CARE_UNIT_ID_2 = "CARE_UNIT_ID_2";
    private static final String SUB_UNIT_ID = "SUB_UNIT_ID";
    private static final String SUB_UNIT_ID_2 = "SUB_UNIT_ID_2";
    private static final Set<String> CERTIFICATE_TYPES = Set.of("lisjp", "ag7804");

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @Mock
    private ArendeService arendeService;

    @Mock
    private CertificateServiceStatisticService certificateServiceStatisticService;

    @InjectMocks
    private CalculateQuestionsForUnitService calculateQuestionsForUnitService;

    private WebCertUser user;

    @BeforeEach
    void setUp() {
        user = mock(WebCertUser.class);
    }

    @Nested
    class CalculateUnitStatisticsTests {

        @BeforeEach
        void setUp() {
            doReturn(CERTIFICATE_TYPES).when(authoritiesHelper)
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
        }

        @Test
        void shouldAddUnitStatisticsForCareUnit() {
            final var questionsMap = Map.of(CARE_UNIT_ID, 5L);
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            assertNotNull(result);
            assertNotNull(result.getUnitStatistics().get(CARE_UNIT_ID));
            assertEquals(5L, result.getUnitStatistics().get(CARE_UNIT_ID).getQuestionsOnUnit());
        }

        @Test
        void shouldCalculateQuestionsOnSubUnits() {
            final var subUnit = new Mottagning(SUB_UNIT_ID, "Sub Unit");
            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            careUnit.setMottagningar(List.of(subUnit));

            final var questionsMap = Map.of(
                CARE_UNIT_ID, 3L,
                SUB_UNIT_ID, 7L
            );
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            assertNotNull(result);
            final var unitStatistics = result.getUnitStatistics().get(CARE_UNIT_ID);
            assertNotNull(unitStatistics);
            assertEquals(3L, unitStatistics.getQuestionsOnUnit());
            assertEquals(7L, unitStatistics.getQuestionsOnSubUnits());
        }

        @Test
        void shouldSumQuestionsFromMultipleSubUnits() {
            final var subUnit1 = new Mottagning(SUB_UNIT_ID, "Sub Unit 1");
            final var subUnit2 = new Mottagning(SUB_UNIT_ID_2, "Sub Unit 2");
            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            careUnit.setMottagningar(List.of(subUnit1, subUnit2));

            final var questionsMap = Map.of(
                CARE_UNIT_ID, 2L,
                SUB_UNIT_ID, 5L,
                SUB_UNIT_ID_2, 3L
            );
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            assertNotNull(result);

            final var unitStatistics = result.getUnitStatistics().get(CARE_UNIT_ID);
            assertNotNull(unitStatistics);
            assertEquals(2L, unitStatistics.getQuestionsOnUnit());
            assertEquals(8L, unitStatistics.getQuestionsOnSubUnits());
        }

        @Test
        void shouldHandleMultipleCareUnits() {
            final var careUnit1 = new Vardenhet(CARE_UNIT_ID, "Care Unit 1");
            final var careUnit2 = new Vardenhet(CARE_UNIT_ID_2, "Care Unit 2");

            final var questionsMap = Map.of(
                CARE_UNIT_ID, 4L,
                CARE_UNIT_ID_2, 6L
            );
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit1, careUnit2));

            assertNotNull(result);
            assertEquals(4L, result.getUnitStatistics().get(CARE_UNIT_ID).getQuestionsOnUnit());
            assertEquals(6L, result.getUnitStatistics().get(CARE_UNIT_ID_2).getQuestionsOnUnit());
        }

        @Test
        void shouldReturnZeroForUnitNotInQuestionsMap() {
            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            doReturn(Collections.emptyMap()).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            assertNotNull(result);
            assertEquals(0L, result.getUnitStatistics().get(CARE_UNIT_ID).getQuestionsOnUnit());
        }

        @Test
        void shouldSetDraftsToZero() {
            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            final var questionsMap = Map.of(CARE_UNIT_ID, 10L);
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            assertNotNull(result);
            assertEquals(0L, result.getUnitStatistics().get(CARE_UNIT_ID).getDraftsOnUnit());
            assertEquals(0L, result.getUnitStatistics().get(CARE_UNIT_ID).getDraftsOnSubUnits());
        }
    }

    @Nested
    class WhenUnitIsNotVardenhet {

        @BeforeEach
        void setUp() {
            doReturn(CERTIFICATE_TYPES).when(authoritiesHelper)
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
        }

        @Test
        void shouldHandleMottagningAsUnitWithEmptySubUnits() {
            final var mottagning = new Mottagning(SUB_UNIT_ID, "Mottagning");
            final var questionsMap = Map.of(SUB_UNIT_ID, 5L);
            doReturn(questionsMap).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            final var result = calculateQuestionsForUnitService.calculate(user, List.of(mottagning));

            assertNotNull(result);
            final var unitStatistics = result.getUnitStatistics().get(SUB_UNIT_ID);
            assertNotNull(unitStatistics);
            assertEquals(5L, unitStatistics.getQuestionsOnUnit());
            assertEquals(0L, unitStatistics.getQuestionsOnSubUnits());
        }
    }

    @Nested
    class CertificateTypesFiltering {

        @Test
        void shouldUseCertificateTypesFromAuthoritiesHelper() {
            final var specificCertificateTypes = Set.of("lisjp");
            doReturn(specificCertificateTypes).when(authoritiesHelper)
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
            doReturn(Collections.emptyMap()).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(specificCertificateTypes));

            calculateQuestionsForUnitService.calculate(user, Collections.emptyList());

            verify(arendeService).getNbrOfUnhandledArendenForCareUnits(anyList(), eq(specificCertificateTypes));
        }

        @Test
        void shouldPassCorrectUnitIdsToArendeService() {
            final var careUnit1 = new Vardenhet(CARE_UNIT_ID, "Care Unit 1");
            final var careUnit2 = new Vardenhet(CARE_UNIT_ID_2, "Care Unit 2");

            doReturn(CERTIFICATE_TYPES).when(authoritiesHelper)
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
            doReturn(Collections.emptyMap()).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));

            calculateQuestionsForUnitService.calculate(user, List.of(careUnit1, careUnit2));

            verify(arendeService).getNbrOfUnhandledArendenForCareUnits(eq(List.of(CARE_UNIT_ID, CARE_UNIT_ID_2)), eq(CERTIFICATE_TYPES));
        }
    }

    @Nested
    class CertificateServiceStatisticServiceIntegration {

        @BeforeEach
        void setUp() {
            doReturn(CERTIFICATE_TYPES).when(authoritiesHelper)
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
            doReturn(Collections.emptyMap()).when(arendeService)
                .getNbrOfUnhandledArendenForCareUnits(anyList(), eq(CERTIFICATE_TYPES));
        }

        @Test
        void shouldCallCertificateServiceStatisticServiceWithCorrectParameters() {
            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");

            calculateQuestionsForUnitService.calculate(user, List.of(careUnit));

            verify(certificateServiceStatisticService).add(
                any(UserStatisticsDTO.class),
                eq(List.of(CARE_UNIT_ID)),
                eq(user),
                eq(false)
            );
        }

        @Test
        void shouldCallCertificateServiceStatisticServiceWithEmptyListWhenNoUnits() {
            calculateQuestionsForUnitService.calculate(user, Collections.emptyList());

            verify(certificateServiceStatisticService).add(
                any(UserStatisticsDTO.class),
                eq(Collections.emptyList()),
                eq(user),
                eq(false)
            );
        }
    }
}