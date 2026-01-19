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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.ListQuestionsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterDateRangeConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterOrderConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPageSizeConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterSelectConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.service.facade.user.CalculateQuestionsForUnitService;
import se.inera.intyg.webcert.web.service.facade.user.UnitStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListQuestionsConfigFacadeServiceImplTest {

    @Mock
    private CalculateQuestionsForUnitService calculateQuestionsForUnitService;
    @Mock
    private GetStaffInfoFacadeService getStaffInfoFacadeService;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private HsaOrganizationsService hsaOrganizationsService;
    @InjectMocks
    private ListQuestionsConfigFacadeServiceImpl listQuestionsConfigFacadeService;

    private static final String TITLE = "Ej hanterade ärenden";
    private static final String HSA_ID = "HsaId";
    private static final String STAFF_NAME = "Name";
    private static final String DEFAULT_HSA_ID = "HsaIdDefault";
    private static final String DEFAULT_HSA_NAME = "Name default";

    private static final String UNIT_ID = "UNIT_ID";
    private static final String UNIT_NAME = "UNIT_NAME";
    private static final String A_UNIT = "OA_UNIT";
    private static final String A_UNIT_NAME = "A_UNIT_NAME";
    private static final String B_UNIT = "AB_UNIT";
    private static final String B_UNIT_NAME = "B_UNIT_NAME";
    private static final String C_UNIT = "C_UNIT";
    private static final String C_UNIT_NAME = "C_UNIT_NAME";
    private Vardenhet unit;

    @BeforeEach
    void setup() {
        unit = new Vardenhet();
        unit.setNamn(UNIT_NAME);
        unit.setId(UNIT_ID);

        final var aUnit = new Vardenhet();
        aUnit.setNamn(A_UNIT_NAME);
        aUnit.setId(A_UNIT);

        final var bUnit = new Vardenhet();
        bUnit.setNamn(B_UNIT_NAME);
        bUnit.setId(B_UNIT);

        final var cUnit = new Vardenhet();
        cUnit.setNamn(C_UNIT_NAME);
        cUnit.setId(C_UNIT);

        when(hsaOrganizationsService.getVardenhet(UNIT_ID)).thenReturn(unit);
        when(hsaOrganizationsService.getVardenhet(A_UNIT)).thenReturn(aUnit);
        when(hsaOrganizationsService.getVardenhet(B_UNIT)).thenReturn(bUnit);
        when(hsaOrganizationsService.getVardenhet(C_UNIT)).thenReturn(cUnit);

        unit.setMottagningar(
            List.of(new Mottagning(B_UNIT, B_UNIT_NAME), new Mottagning(A_UNIT, A_UNIT_NAME), new Mottagning(C_UNIT, C_UNIT_NAME)));

        final var statistics = new UserStatisticsDTO();
        final var unitStatistics = new UnitStatisticsDTO(1, 2, 3, 4);
        statistics.addUnitStatistics(UNIT_ID, unitStatistics);
        statistics.addUnitStatistics(A_UNIT, unitStatistics);
        statistics.addUnitStatistics(B_UNIT, unitStatistics);
        statistics.addUnitStatistics(C_UNIT, unitStatistics);
        when(calculateQuestionsForUnitService.calculate(any(), any())).thenReturn(statistics);

        when(getStaffInfoFacadeService.getLoggedInStaffHsaId()).thenReturn(DEFAULT_HSA_ID);
        when(getStaffInfoFacadeService.isLoggedInUserDoctor()).thenReturn(true);
        when(getStaffInfoFacadeService.get(any())).thenReturn(
            List.of(new StaffListInfo(HSA_ID, STAFF_NAME), new StaffListInfo(DEFAULT_HSA_ID, DEFAULT_HSA_NAME)));
    }

    @Nested
    class TestsForDoctor {

        @BeforeEach
        void setupUser() {
            ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                LuseEntryPoint.MODULE_ID, unit, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        }

        @Test
        void shouldSetSecondaryTitle() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertEquals(TITLE, config.getTitle());
        }

        @Test
        void shouldSetTitle() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertEquals("Ärenden visas för " + UNIT_NAME, config.getSecondaryTitle());
        }

        @Test
        void shouldSetOpenCertificateTooltip() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertTrue(config.getButtonTooltips().containsKey("OPEN_BUTTON"));
        }

        @Test
        void shouldSetFilters() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertEquals(10, config.getFilters().size());
        }

        @Test
        void shouldSetTableHeadings() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertEquals(8, config.getTableHeadings().length);
        }

        @Test
        void shouldSetSearchCertificateTooltip() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertTrue(config.getButtonTooltips().containsKey("SEARCH_BUTTON"));
        }

        @Test
        void shouldSetClearFiltersTooltip() {
            final var config = listQuestionsConfigFacadeService.get(UNIT_ID);
            assertTrue(config.getButtonTooltips().containsKey("RESET_BUTTON"));
        }

        @Nested
        class TestSent {

            ListFilterDateRangeConfig filter;
            ListConfig config;

            @BeforeEach
            void setupSent() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterDateRangeConfig) getFilterById(config, "SENT");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Skickat datum", filter.getTitle());
            }

            @Test
            void shouldSetTitleOfTo() {
                assertTrue(filter.getTo().getTitle().length() > 0);
            }

            @Test
            void shouldSetTitleOfFrom() {
                assertTrue(filter.getFrom().getTitle().length() > 0);
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.DATE_RANGE, filter.getType());
            }

            @Test
            void shouldSetDefaultValueFrom() {
                assertEquals(LocalDateTime.now().minusMonths(3).toLocalDate(), filter.getFrom().getDefaultValue().toLocalDate());
            }
        }

        @Nested
        class TestForwarded {

            ListFilterSelectConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterSelectConfig) getFilterById(config, "FORWARDED");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Vidarebefordrat", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.SELECT, filter.getType());
            }

            @Test
            void shouldSetList() {
                assertEquals(3, filter.getValues().size());
            }

            @Test
            void shouldSetFirstValueInListAsDefault() {
                assertTrue(filter.getValues().get(0).isDefaultValue());
            }
        }

        @Nested
        class TestQuestionStatus {

            ListFilterSelectConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterSelectConfig) getFilterById(config, "STATUS");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Åtgärd", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.SELECT, filter.getType());
            }

            @Test
            void shouldSetList() {
                assertEquals(7, filter.getValues().size());
            }

            @Test
            void shouldSetSecondValueInListAsDefault() {
                assertTrue(filter.getValues().get(1).isDefaultValue());
            }
        }

        @Nested
        class TestSender {

            ListFilterSelectConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterSelectConfig) getFilterById(config, "SENDER");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Avsändare", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.SELECT, filter.getType());
            }

            @Test
            void shouldSetList() {
                assertEquals(3, filter.getValues().size());
            }

            @Test
            void shouldSetFirstValueInListAsDefault() {
                assertTrue(filter.getValues().get(0).isDefaultValue());
            }
        }

        @Nested
        class TestUnit {

            ListFilterSelectConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterSelectConfig) getFilterById(config, "UNIT");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Enhet", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.SELECT, filter.getType());
            }

            @Test
            void shouldSetListOnlyIncludingSubunitsUnitAndShowAll() {
                assertEquals(5, filter.getValues().size());
            }

            @Test
            void shouldSetFirstValueInListAsDefault() {
                assertTrue(filter.getValues().get(0).isDefaultValue());
            }

            @Test
            void shouldSetUnitSelectName() {
                assertEquals(UNIT_NAME + " (2)", filter.getValues().get(1).getName());
            }

            @Test
            void shouldSetUnitSelectNameWithSpacingForSubunit() {
                assertEquals("&emsp; " + A_UNIT_NAME + " (2)", filter.getValues().get(2).getName());
            }

            @Test
            void shouldSetShowAllOption() {
                assertEquals("Visa alla (6)", filter.getValues().get(0).getName());
            }

            @Test
            void shouldOrderUnitsWithSelectedUnitFirst() {
                assertEquals(UNIT_ID, filter.getValues().get(1).getId());
            }

            @Test
            void shouldOrderUnitsAlphabetically() {
                assertEquals(A_UNIT, filter.getValues().get(2).getId());
            }
        }

        @Nested
        class TestPatientId {

            ListFilterPersonIdConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterPersonIdConfig) getFilterById(config, "PATIENT_ID");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertEquals("Patient", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.PERSON_ID, filter.getType());
            }

            @Test
            void shouldSetPlaceholder() {
                assertTrue(filter.getPlaceholder().length() > 0);
            }
        }

        @Nested
        class TestOrderBy {

            ListFilterOrderConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterOrderConfig) getFilterById(config, "ORDER_BY");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetEmptyTitle() {
                assertEquals("", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.ORDER, filter.getType());
            }

            @Test
            void shouldSetDefaultOrder() {
                assertEquals(ListColumnType.SENT_RECEIVED, filter.getDefaultValue());
            }
        }

        @Nested
        class TestPageSize {

            ListFilterPageSizeConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterPageSizeConfig) getFilterById(config, "PAGESIZE");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetTitle() {
                assertTrue(filter.getTitle().length() > 0);
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.PAGESIZE, filter.getType());
            }

            @Test
            void shouldSetList() {
                assertEquals(4, filter.getPageSizes().length);
            }
        }

        @Nested
        class TestAscending {

            ListFilterBooleanConfig filter;
            ListConfig config;

            @BeforeEach
            void setup() {
                config = listQuestionsConfigFacadeService.get(UNIT_ID);
                filter = (ListFilterBooleanConfig) getFilterById(config, "ASCENDING");
            }

            @Test
            void shouldCreateFilter() {
                assertNotNull(filter);
            }

            @Test
            void shouldSetEmptyTitle() {
                assertEquals("", filter.getTitle());
            }

            @Test
            void shouldSetType() {
                assertEquals(ListFilterType.BOOLEAN, filter.getType());
            }

            @Test
            void shouldSetDefaultValue() {
                assertFalse(filter.getDefaultValue());
            }
        }

        @Nested
        class TestEmptyUnitId {

            @Test
            void shouldReturnCorrectSecondaryTitle() {
                final var config = listQuestionsConfigFacadeService.get("");

                assertEquals("Ärenden visas för alla enheter", config.getSecondaryTitle());
            }
        }

        @Nested
        class TestUpdate {

            @Test
            void shouldUpdateSecondaryTitle() {
                final var config = listQuestionsConfigFacadeService.get("");
                final var originalSecondaryTitle = config.getSecondaryTitle();
                final var updatedConfig = listQuestionsConfigFacadeService.update(config, B_UNIT);

                assertNotEquals(originalSecondaryTitle, updatedConfig.getSecondaryTitle());
                assertTrue(updatedConfig.getSecondaryTitle().contains(B_UNIT_NAME));
            }
        }

        @Nested
        class TestColumnOrder {

            @Test
            void shouldHaveQuestionActionInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.QUESTION_ACTION, config.getTableHeadings()[0].getId());
            }

            @Test
            void shouldHaveQuestionSenderInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.SENDER, config.getTableHeadings()[1].getId());
            }

            @Test
            void shouldHaveQuestionPatientIdInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.PATIENT_ID, config.getTableHeadings()[2].getId());
            }

            @Test
            void shouldHaveQuestionSignedByInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.SIGNED_BY, config.getTableHeadings()[3].getId());
            }

            @Test
            void shouldHaveQuestionSentReceivedInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.SENT_RECEIVED, config.getTableHeadings()[4].getId());
            }

            @Test
            void shouldHaveQuestionForwardInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.FORWARDED, config.getTableHeadings()[5].getId());
            }

            @Test
            void shouldHaveQuestionForwardCertificateInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.FORWARD_CERTIFICATE, config.getTableHeadings()[6].getId());
            }

            @Test
            void shouldHaveQuestionOpenCertificateInCorrectPlace() {
                final var config = listQuestionsConfigFacadeService.get("");
                assertEquals(ListColumnType.OPEN_CERTIFICATE, config.getTableHeadings()[7].getId());
            }
        }
    }

    @Nested
    class TestSignedBy {

        ListFilterSelectConfig filter;
        ListConfig config;

        void setupUser(String userRole) {
            final var user = ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                LuseEntryPoint.MODULE_ID, unit, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
            user.setRoles(Map.of(userRole, new Role()));
        }

        void setupSignedBy() {
            config = listQuestionsConfigFacadeService.get(UNIT_ID);
            filter = (ListFilterSelectConfig) getFilterById(config, "SIGNED_BY");
        }

        @Test
        void shouldCreateFilter() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertNotNull(filter);
        }

        @Test
        void shouldSetTitle() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertEquals("Signerat av", filter.getTitle());
        }

        @Test
        void shouldSetType() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertEquals(ListFilterType.SELECT, filter.getType());
        }

        @Test
        void shouldSetList() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertEquals(3, filter.getValues().size());
        }

        @Test
        void shouldSetShowAll() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertEquals("Visa alla", filter.getValues().get(0).getName());
        }

        @Test
        void shouldSetDefaultValueOfSavedByAsLoggedInDoctor() {
            setupUser(AuthoritiesConstants.ROLE_LAKARE);
            setupSignedBy();
            assertTrue(filter.getValues().get(2).isDefaultValue());

        }

        @Test
        void shouldSetShowAllAsDefaultIfCareAdmin() {
            setupUser(AuthoritiesConstants.ROLE_ADMIN);
            setupSignedBy();
            assertTrue(filter.getValues().get(0).isDefaultValue());
        }
    }

    @Nested
    class TestsForPrivatePractitioner {

        ListFilterSelectConfig filter;
        ListConfig config;

        @BeforeEach
        void setupUser() {
            ListTestHelper.setupUser(webCertUserService, true, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                LuseEntryPoint.MODULE_ID, unit, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

            config = listQuestionsConfigFacadeService.get(UNIT_ID);
            filter = (ListFilterSelectConfig) getFilterById(config, "UNIT");
        }

        @Test
        void shouldSetEmptyListIfPrivatePractitioner() {
            assertEquals(0, filter.getValues().size());
        }

        @Test
        void shouldSetSecondaryTitleToShowAll() {
            assertEquals("Ärenden visas för alla enheter", config.getSecondaryTitle());
        }
    }

    private ListFilterConfig getFilterById(ListConfig config, String id) {
        return config.getFilters().stream().filter((ListFilterConfig filter) -> filter.getId().equals(id)).findAny().orElse(null);
    }
}