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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.list.config.ListSignedCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListSignedCertificatesConfigFacadeServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService;

    private final String TITLE = "Signerade intyg";

    @BeforeEach
    public void setup() {
        ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
            LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
    }

    @Test
    public void shouldSetSecondaryTitle() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertEquals(TITLE, config.getTitle());
    }

    @Test
    public void shouldSetTitle() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertEquals("Intyg visas för Enhetsnamn", config.getSecondaryTitle());
    }

    @Test
    public void shouldSetOpenCertificateTooltip() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("OPEN_BUTTON"));
    }

    @Test
    public void shouldSetFilters() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertEquals(5, config.getFilters().size());
    }

    @Test
    public void shouldSetTableHeadings() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertEquals(5, config.getTableHeadings().length);
    }

    @Test
    public void shouldSetSearchCertificateTooltip() {
        final var config = listSignedCertificatesConfigFacadeService.get();
        assertTrue(config.getButtonTooltips().containsKey("SEARCH_BUTTON"));
    }

    @Nested
    public class Signed {

        ListFilterDateRangeConfig filter;
        ListConfig config;

        @BeforeEach
        public void setupSaved() {
            config = listSignedCertificatesConfigFacadeService.get();
            filter = (ListFilterDateRangeConfig) getFilterById(config, "SIGNED");
        }

        @Test
        public void shouldCreateFilter() {
            assertNotNull(filter);
        }

        @Test
        public void shouldSetTitle() {
            assertEquals("Signeringsdatum", filter.getTitle());
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
    }

    @Nested
    public class PatientId {

        ListFilterPersonIdConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listSignedCertificatesConfigFacadeService.get();
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
            config = listSignedCertificatesConfigFacadeService.get();
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
            assertEquals(ListColumnType.SIGNED, filter.getDefaultValue());
        }
    }

    @Nested
    public class PageSize {

        ListFilterPageSizeConfig filter;
        ListConfig config;

        @BeforeEach
        public void setup() {
            config = listSignedCertificatesConfigFacadeService.get();
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
            config = listSignedCertificatesConfigFacadeService.get();
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
