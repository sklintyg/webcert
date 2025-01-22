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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.config.ListPreviousCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ListPreviousCertificatesConfigFacadeServiceImplTest {

    @InjectMocks
    private ListPreviousCertificatesConfigFacadeServiceImpl listPreviousCertificatesConfigFacadeService;

    @Test
    public void shouldSetExcludeFilterButtons() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertTrue(config.isExcludeFilterButtons());
    }

    @Test
    public void shouldSetSecondaryTitle() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertEquals("Tidigare intyg", config.getSecondaryTitle());
    }

    @Test
    public void shouldSetOpenCertificateTooltip() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("OPEN_BUTTON"));
    }

    @Test
    public void shouldSetFilters() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertEquals(4, config.getFilters().size());
    }

    @Test
    public void shouldSetTableHeadings() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertEquals(6, config.getTableHeadings().length);
    }

    @Test
    public void shouldSetRenewCertificateTooltip() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("RENEW_BUTTON"));
    }

    @Test
    public void shouldSetEmptyListText() {
        final var config = listPreviousCertificatesConfigFacadeService.get();
        assertTrue(config.getEmptyListText().length() > 0);
    }

    @Nested
    public class RadioStatusFilter {

        ListFilterRadioConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listPreviousCertificatesConfigFacadeService.get();
            filter = (ListFilterRadioConfig) getFilterById(config, "STATUS");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldNotSetTitle() {
            assertEquals("", filter.getTitle());
        }

        @Test
        public void shouldSetType() {
            assertEquals(ListFilterType.RADIO, filter.getType());
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
    public class OrderBy {

        ListFilterOrderConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listPreviousCertificatesConfigFacadeService.get();
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
            config = listPreviousCertificatesConfigFacadeService.get();
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
            config = listPreviousCertificatesConfigFacadeService.get();
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
