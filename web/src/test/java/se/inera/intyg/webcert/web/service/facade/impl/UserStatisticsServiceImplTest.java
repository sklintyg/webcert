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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
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
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceStatisticService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsServiceImpl;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class UserStatisticsServiceImplTest {

    final static String SELECTED_UNIT_ID = "UNITID";
    final static String NOT_SELECTED_UNIT_ID = "NOT_SELECTED_UNIT_ID";
    final static String SUB_UNIT_TO_SELECTED = "SUB_UNIT_ID";
    @Mock
    private CertificateServiceStatisticService certificateServiceStatisticService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @Mock
    private FragaSvarService fragaSvarService;

    @Mock
    private ArendeService arendeService;

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

        doReturn(List.of(SELECTED_UNIT_ID))
            .when(user)
            .getIdsOfSelectedVardenhet();

        doReturn(List.of(SELECTED_UNIT_ID, NOT_SELECTED_UNIT_ID, SUB_UNIT_TO_SELECTED)).when(user).getIdsOfAllVardenheter();
    }

    private Vardgivare getCareProvider() {
        final var careProvider = new Vardgivare();
        careProvider.setId("CARE_PROVIDER");

        final var selectedUnit = new Vardenhet(SELECTED_UNIT_ID, SELECTED_UNIT_ID);
        selectedUnit.setMottagningar(List.of(new Mottagning(SUB_UNIT_TO_SELECTED, SUB_UNIT_TO_SELECTED)));

        careProvider.setVardenheter(List.of(new Vardenhet(NOT_SELECTED_UNIT_ID, NOT_SELECTED_UNIT_ID), selectedUnit));

        return careProvider;
    }

    @Nested
    class ErrorHandling {

        @Test
        void shouldReturnNullIfUserIsNull() {
            final var result = userStatisticsService.getUserStatistics();
            assertNull(result);
        }

        @Test
        void shouldReturnNullIfUserHasOriginIntegrated() {
            setUpUser();
            user.setOrigin("DJUPINTEGRATION");

            final var result = userStatisticsService.getUserStatistics();

            assertNull(result);
        }

        @Test
        void shouldReturnNullIfUnitIdsAreNull() {
            setUpUser();
            final var result = userStatisticsService.getUserStatistics();
            assertNull(result);
        }
    }

    @Nested
    class ValuesForSelectedUnit {

        final long expectedValue = 100L;
        final Map<String, Long> map = new HashMap<String, Long>();

        @BeforeEach
        void setup() {
            setUpUser();
            setUpUnit();

            doReturn(new HashSet<String>())
                .when(authoritiesHelper)
                .getIntygstyperForPrivilege(any(), any());

            map.put(SELECTED_UNIT_ID, expectedValue);
        }

        @Test
        void shouldReturnNbrOfDraftsForSelectedUnit() {
            doReturn(map).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

            final var result = userStatisticsService.getUserStatistics().getNbrOfDraftsOnSelectedUnit();

            assertEquals(expectedValue, result);
        }

        @Test
        void shouldReturnNbrOfQuestionsForSelectedUnit() {
            doReturn(map).when(arendeService).getNbrOfUnhandledArendenForCareUnits(any(), any());
            doReturn(map).when(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(any(), any());

            final var result = userStatisticsService.getUserStatistics().getNbrOfUnhandledQuestionsOnSelectedUnit();

            assertEquals(expectedValue * 2, result);
        }

        @Nested
        class CareProviderStatistics {

            final Map<String, Long> draftsMap = new HashMap<String, Long>();
            final Map<String, Long> questionsMap = new HashMap<String, Long>();

            final long expectedDraftsSelected = 100;
            final long expectedQuestionsSelected = 200;
            final long expectedDraftsNotSelected = 1;
            final long expectedQuestionsNotSelected = 2;
            final long expectedDraftsSubUnit = 10;
            final long expectedQuestionsSubUnit = 10;

            @BeforeEach
            void setup() {
                doReturn(List.of(getCareProvider()))
                    .when(user)
                    .getVardgivare();

                draftsMap.put(SELECTED_UNIT_ID, expectedDraftsSelected);
                draftsMap.put(NOT_SELECTED_UNIT_ID, expectedDraftsNotSelected);
                draftsMap.put(SUB_UNIT_TO_SELECTED, expectedDraftsSubUnit);

                questionsMap.put(SELECTED_UNIT_ID, expectedQuestionsSelected);
                questionsMap.put(NOT_SELECTED_UNIT_ID, expectedQuestionsNotSelected);
                questionsMap.put(SUB_UNIT_TO_SELECTED, expectedQuestionsSubUnit);

                doReturn(questionsMap).when(arendeService).getNbrOfUnhandledArendenForCareUnits(any(), any());
                doReturn(draftsMap).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

            }

            @Test
            void shouldAddStatisticsForAllUnitsAndSubUnits() {
                final var result = userStatisticsService.getUserStatistics();

                assertEquals(3, result.getUnitStatistics().size());
            }

            @Nested
            class CareUnit {

                @Test
                void shouldAddDrafts() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedDraftsSelected, result.getUnitStatistics().get(SELECTED_UNIT_ID).getDraftsOnUnit());
                }

                @Test
                void shouldAddQuestions() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedQuestionsSelected, result.getUnitStatistics().get(SELECTED_UNIT_ID).getQuestionsOnUnit());
                }

                @Test
                void shouldAddDraftsOfSubUnits() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedDraftsSubUnit, result.getUnitStatistics().get(SELECTED_UNIT_ID).getDraftsOnSubUnits());
                }

                @Test
                void shouldAddQuestionsOfSubUnits() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedQuestionsSubUnit, result.getUnitStatistics().get(SELECTED_UNIT_ID).getQuestionsOnSubUnits());
                }
            }

            @Nested
            class SubUnit {

                @Test
                void shouldAddDrafts() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedDraftsSubUnit, result.getUnitStatistics().get(SUB_UNIT_TO_SELECTED).getDraftsOnUnit());
                }

                @Test
                void shouldAddQuestions() {
                    final var result = userStatisticsService.getUserStatistics();

                    assertEquals(expectedQuestionsSubUnit, result.getUnitStatistics().get(SUB_UNIT_TO_SELECTED).getQuestionsOnUnit());
                }
            }
        }

        @Nested
        class totalDraftsAndUnhandledQuestionsOnOtherUnits {

            @Test
            void shouldReturn0IfOnlySelectedUnitStatisticsInMap() {
                doReturn(map).when(arendeService).getNbrOfUnhandledArendenForCareUnits(any(), any());
                doReturn(map).when(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(any(), any());
                doReturn(map).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

                final var result = userStatisticsService.getUserStatistics().getTotalDraftsAndUnhandledQuestionsOnOtherUnits();

                assertEquals(0L, result);
            }

            @Test
            void shouldReturnNbrOfDrafts() {
                map.put(NOT_SELECTED_UNIT_ID, expectedValue);
                doReturn(map).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

                final var result = userStatisticsService.getUserStatistics().getTotalDraftsAndUnhandledQuestionsOnOtherUnits();

                assertEquals(expectedValue, result);
            }

            @Test
            void shouldReturnNbrOfQuestions() {
                map.put(NOT_SELECTED_UNIT_ID, expectedValue);
                doReturn(map).when(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(any(), any());
                doReturn(map).when(arendeService).getNbrOfUnhandledArendenForCareUnits(any(), any());

                final var result = userStatisticsService.getUserStatistics().getTotalDraftsAndUnhandledQuestionsOnOtherUnits();

                assertEquals(expectedValue * 2, result);
            }

            @Test
            void shouldReturnNbrOfQuestionsPlusDrafts() {
                map.put(NOT_SELECTED_UNIT_ID, expectedValue);
                doReturn(map).when(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(any(), any());
                doReturn(map).when(arendeService).getNbrOfUnhandledArendenForCareUnits(any(), any());
                doReturn(map).when(utkastService).getNbrOfUnsignedDraftsByCareUnits(any());

                final var result = userStatisticsService.getUserStatistics().getTotalDraftsAndUnhandledQuestionsOnOtherUnits();

                assertEquals(expectedValue * 3, result);
            }
        }

        @Test
        void shouldAddStatisticsFromCertificateService() {
            userStatisticsService.getUserStatistics();
            verify(certificateServiceStatisticService).add(any(UserStatisticsDTO.class),
                eq(List.of(SELECTED_UNIT_ID, NOT_SELECTED_UNIT_ID, SUB_UNIT_TO_SELECTED)), eq(user));
        }
    }
}
