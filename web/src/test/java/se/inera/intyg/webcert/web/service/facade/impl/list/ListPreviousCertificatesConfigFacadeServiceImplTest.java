/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.config.ListPreviousCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterOrderConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPageSizeConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterRadioConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterType;

@ExtendWith(MockitoExtension.class)
class ListPreviousCertificatesConfigFacadeServiceImplTest {

  @InjectMocks
  private ListPreviousCertificatesConfigFacadeServiceImpl
      listPreviousCertificatesConfigFacadeService;

  @Test
  void shouldSetExcludeFilterButtons() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertTrue(config.isExcludeFilterButtons());
  }

  @Test
  void shouldSetSecondaryTitle() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertEquals(config.getSecondaryTitle(), "Tidigare intyg");
  }

  @Test
  void shouldSetDescription() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertFalse(config.getDescription().isEmpty());
  }

  @Test
  void shouldSetOpenCertificateTooltip() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertTrue(config.getButtonTooltips().containsKey("OPEN_BUTTON"));
  }

  @Test
  void shouldSetFilters() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertEquals(4, config.getFilters().size());
  }

  @Test
  void shouldSetTableHeadings() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertEquals(6, config.getTableHeadings().length);
  }

  @Test
  void shouldSetRenewCertificateTooltip() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertTrue(config.getButtonTooltips().containsKey("RENEW_BUTTON"));
  }

  @Test
  void shouldSetEmptyListText() {
    final var config = listPreviousCertificatesConfigFacadeService.get();
    assertTrue(config.getEmptyListText().length() > 0);
  }

  @Nested
  class RadioStatusFilter {

    ListFilterRadioConfig filter;
    ListConfig config;

    @BeforeEach
    void setup() {
      config = listPreviousCertificatesConfigFacadeService.get();
      filter = (ListFilterRadioConfig) getFilterById(config, "STATUS");
    }

    @Test
    void shouldCreateFilter() {
      assertNotNull(filter);
    }

    @Test
    void shouldNotSetTitle() {
      assertEquals(filter.getTitle(), "");
    }

    @Test
    void shouldSetType() {
      assertEquals(ListFilterType.RADIO, filter.getType());
    }

    @Test
    void shouldSetList() {
      assertEquals(3, filter.getValues().size());
    }

    @Test
    void shouldSetFirstValueInListAsDefault() {
      assertTrue(filter.getValues().getFirst().isDefaultValue());
    }
  }

  @Nested
  class OrderBy {

    ListFilterOrderConfig filter;
    ListConfig config;

    @BeforeEach
    void setup() {
      config = listPreviousCertificatesConfigFacadeService.get();
      filter = (ListFilterOrderConfig) getFilterById(config, "ORDER_BY");
    }

    @Test
    void shouldCreateFilter() {
      assertNotNull(filter);
    }

    @Test
    void shouldSetEmptyTitle() {
      assertEquals(filter.getTitle(), "");
    }

    @Test
    void shouldSetType() {
      assertEquals(ListFilterType.ORDER, filter.getType());
    }

    @Test
    void shouldSetDefaultOrder() {
      assertEquals(ListColumnType.SAVED, filter.getDefaultValue());
    }
  }

  @Nested
  class PageSize {

    ListFilterPageSizeConfig filter;
    ListConfig config;

    @BeforeEach
    void setup() {
      config = listPreviousCertificatesConfigFacadeService.get();
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
  class Ascending {

    ListFilterBooleanConfig filter;
    ListConfig config;

    @BeforeEach
    void setup() {
      config = listPreviousCertificatesConfigFacadeService.get();
      filter = (ListFilterBooleanConfig) getFilterById(config, "ASCENDING");
    }

    @Test
    void shouldCreateFilter() {
      assertNotNull(filter);
    }

    @Test
    void shouldSetEmptyTitle() {
      assertEquals(filter.getTitle(), "");
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

  private ListFilterConfig getFilterById(ListConfig config, String id) {
    return config.getFilters().stream()
        .filter((ListFilterConfig filter) -> filter.getId().equals(id))
        .findAny()
        .orElse(null);
  }
}
