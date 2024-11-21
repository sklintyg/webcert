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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import java.time.LocalDateTime;
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
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.ListDraftsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListDraftsConfigFacadeServiceImplTest {

    @Mock
    private GetStaffInfoFacadeService getStaffInfoFacadeService;
    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private ListDraftsConfigFacadeServiceImpl listDraftsConfigFacadeService;

    private final String TITLE = "Ej signerade utkast";
    private final String HSA_ID = "HsaId";
    private final String STAFF_NAME = "Name";
    private final String DEFAULT_HSA_ID = "HsaIdDefault";
    private final String DEFAULT_HSA_NAME = "Name default";

    @BeforeEach
    public void setup() {
        when(getStaffInfoFacadeService.getLoggedInStaffHsaId()).thenReturn(DEFAULT_HSA_ID);
        when(getStaffInfoFacadeService.isLoggedInUserDoctor()).thenReturn(true);
        when(getStaffInfoFacadeService.get()).thenReturn(
            List.of(new StaffListInfo(HSA_ID, STAFF_NAME), new StaffListInfo(DEFAULT_HSA_ID, DEFAULT_HSA_NAME)));
        ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
            LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
    }

    @Test
    public void shouldSetSecondaryTitle() {
        final var config = listDraftsConfigFacadeService.get();
        assertEquals(TITLE, config.getTitle());
    }

    @Test
    public void shouldSetTitle() {
        final var config = listDraftsConfigFacadeService.get();
        assertEquals("Intyg visas för Enhetsnamn", config.getSecondaryTitle());
    }

    @Test
    public void shouldSetOpenCertificateTooltip() {
        final var config = listDraftsConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("OPEN_BUTTON"));
    }

    @Test
    public void shouldSetFilters() {
        final var config = listDraftsConfigFacadeService.get();
        assertEquals(8, config.getFilters().size());
    }

    @Test
    public void shouldSetTableHeadings() {
        final var config = listDraftsConfigFacadeService.get();
        assertEquals(8, config.getTableHeadings().length);
    }

    @Test
    public void shouldSetSearchCertificateTooltip() {
        final var config = listDraftsConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("SEARCH_BUTTON"));
    }

    @Nested
    public class SavedBy {

        ListFilterSelectConfig filter;
        ListConfig config;

        public void setupSavedBy() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterSelectConfig) getFilterById(config, "SAVED_BY");
        }

        @Test
        public void shouldCreateFilter() {
            setupSavedBy();
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            setupSavedBy();
            assertEquals("Sparat av", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            setupSavedBy();
            assertEquals(ListFilterType.SELECT, filter.getType());
        }

        @Test
        public void shouldSetList() {
            setupSavedBy();
            assertEquals(3, filter.getValues().size());
        }

        @Test
        public void shouldSetShowAll() {
            setupSavedBy();
            assertEquals("Visa alla", filter.getValues().get(0).getName());
        }

        @Test
        public void shouldSetDefaultValueOfSavedByAsLoggedInDoctor() {
            when(getStaffInfoFacadeService.isLoggedInUserDoctor()).thenReturn(true);
            setupSavedBy();
            assertTrue(filter.getValues().get(2).isDefaultValue());
        }

        @Test
        public void shouldSetUserAsDefaultIfNotDoctor() {
            when(getStaffInfoFacadeService.isLoggedInUserDoctor()).thenReturn(false);
            setupSavedBy();
            assertTrue(filter.getValues().get(2).isDefaultValue());
        }
    }

    @Nested
    public class Saved {

        ListFilterDateRangeConfig filter;
        ListConfig config;

        @BeforeEach
        public void setupSaved() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterDateRangeConfig) getFilterById(config, "SAVED");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertEquals("Sparat datum", filter.getTitle());
        }

        @Test
        public void shouldSetTitleOfTo() {
            assertTrue(filter.getTo().getTitle().length() > 0);
        }

        @Test
        public void shouldSetTitleOfFrom() {
            assertTrue(filter.getFrom().getTitle().length() > 0);
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.DATE_RANGE, filter.getType());
        }

        @Test
         void shouldSetDefaultValueFrom() {
            assertEquals(LocalDateTime.now().minusMonths(3).toLocalDate(), filter.getFrom().getDefaultValue().toLocalDate());
        }
    }

    @Nested
    public class Forwarded {

        ListFilterSelectConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterSelectConfig) getFilterById(config, "FORWARDED");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertEquals("Vidarebefordrat", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.SELECT, filter.getType());
        }

        @Test
        public void shouldSetList() {
            assertEquals(3, filter.getValues().size());
        }

        @Test
        public void shouldSetFirstValueInListAsDefault() {
            assertTrue(filter.getValues().get(0).isDefaultValue());
        }
    }

    @Nested
    public class Status {

        ListFilterSelectConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterSelectConfig) getFilterById(config, "STATUS");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertEquals("Utkast", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.SELECT, filter.getType());
        }

        @Test
        public void shouldSetList() {
            assertEquals(4, filter.getValues().size());
        }

        @Test
        public void shouldSetFirstValueInListAsDefault() {
            assertTrue(filter.getValues().get(0).isDefaultValue());
        }
    }

    @Nested
    public class PatientId {

        ListFilterPersonIdConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterPersonIdConfig) getFilterById(config, "PATIENT_ID");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertEquals("Patient", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.PERSON_ID, filter.getType());
        }

        @Test
        public void shouldSetPlaceholder() {
            assertTrue(filter.getPlaceholder().length() > 0);
        }
    }

    @Nested
    public class OrderBy {

        ListFilterOrderConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterOrderConfig) getFilterById(config, "ORDER_BY");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetEmptyTitle() {
            assertEquals("", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.ORDER, filter.getType());
        }

        @Test
        public void shouldSetDefaultOrder() {
            assertEquals(ListColumnType.SAVED, filter.getDefaultValue());
        }
    }

    @Nested
    public class PageSize {

        ListFilterPageSizeConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterPageSizeConfig) getFilterById(config, "PAGESIZE");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertTrue(filter.getTitle().length() > 0);
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.PAGESIZE, filter.getType());
        }

        @Test
        public void shouldSetList() {
            assertEquals(4, filter.getPageSizes().length);
        }
    }

    @Nested
    public class Ascending {

        ListFilterBooleanConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listDraftsConfigFacadeService.get();
            filter = (ListFilterBooleanConfig) getFilterById(config, "ASCENDING");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetEmptyTitle() {
            assertEquals("", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.BOOLEAN, filter.getType());
        }

        @Test
        public void shouldSetDefaultValue() {
            assertFalse(filter.getDefaultValue());
        }
    }

    private ListFilterConfig getFilterById(ListConfig config, String id) {
        return config.getFilters().stream().filter((ListFilterConfig filter) -> filter.getId().equals(id)).findAny().orElse(null);
    }
}