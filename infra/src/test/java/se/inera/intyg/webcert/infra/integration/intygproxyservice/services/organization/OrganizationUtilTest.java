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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.DEFAULT_ARBETSPLATSKOD;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.getWorkplaceCode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrganizationUtilTest {

  @Nested
  class IsActive {

    @Test
    void shouldReturnTrueIfOnlyFromAndFromIsBeforeNow() {
      assertTrue(OrganizationUtil.isActive(LocalDateTime.now().minusDays(1), null));
    }

    @Test
    void shouldReturnFalseIfOnlyFromAndFromIsAfterNow() {
      assertFalse(OrganizationUtil.isActive(LocalDateTime.now().plusDays(1), null));
    }

    @Test
    void shouldReturnTrueIfOnlyToAndToIsAfterNow() {
      assertTrue(OrganizationUtil.isActive(null, LocalDateTime.now().plusDays(1)));
    }

    @Test
    void shouldReturnFalseIfOnlyToAndToIsBeforeNow() {
      assertFalse(OrganizationUtil.isActive(null, LocalDateTime.now().minusDays(1)));
    }

    @Test
    void shouldReturnFalseIfIntervalAndToIsBeforeNow() {
      assertFalse(
          OrganizationUtil.isActive(
              LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1)));
    }

    @Test
    void shouldReturnFalseIfIntervalAndFromIsAfterNow() {
      assertFalse(
          OrganizationUtil.isActive(
              LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)));
    }

    @Test
    void shouldReturnTrueIfIntervalIsCorrect() {
      assertTrue(
          OrganizationUtil.isActive(
              LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(3)));
    }

    @Test
    void shouldReturnFalseIfLateInterval() {
      assertFalse(
          OrganizationUtil.isActive(
              LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)));
    }

    @Test
    void shouldReturnFalseIfEarlyInterval() {
      assertFalse(
          OrganizationUtil.isActive(
              LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)));
    }

    @Test
    void shouldReturnTrueIfBothAreNull() {
      assertTrue(OrganizationUtil.isActive(null, null));
    }
  }

  @Nested
  class GetWorkplaceCode {

    @Test
    void shouldReturnDefaultIfEmptyList() {
      assertEquals(DEFAULT_ARBETSPLATSKOD, getWorkplaceCode(Collections.emptyList()));
    }

    @Test
    void shouldReturnDefaultIfNull() {
      assertEquals(DEFAULT_ARBETSPLATSKOD, getWorkplaceCode(null));
    }

    @Test
    void shouldReturnFirstCode() {
      assertEquals("CODE1", getWorkplaceCode(List.of("CODE1", "CODE2")));
    }
  }
}
