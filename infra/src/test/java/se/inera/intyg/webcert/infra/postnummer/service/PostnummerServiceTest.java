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
package se.inera.intyg.webcert.infra.postnummer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import se.inera.intyg.infra.integration.postnummer.model.Omrade;

@SpringJUnitConfig(classes = PostnummerServiceTest.TestConfiguration.class)
class PostnummerServiceTest {

  @Configuration
  @ComponentScan("se.inera.intyg.infra.integration.postnummer")
  @PropertySource(
      value = {"classpath:/test.properties"},
      ignoreResourceNotFound = false)
  static class TestConfiguration {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertiesResolver() {
      return new PropertySourcesPlaceholderConfigurer();
    }
  }

  @Autowired PostnummerService postnummerService;

  @Test
  void testGetPostnummer() {

    List<Omrade> omrade13061 =
        Arrays.asList(new Omrade("13061", "HÅRSFJÄRDEN", "HANINGE", "STOCKHOLM"));
    List<Omrade> omrade13100 = Arrays.asList(new Omrade("13100", "NACKA", "NACKA", "STOCKHOLM"));
    List<Omrade> omrade13155 = Arrays.asList(new Omrade("13155", "NACKA", "NACKA", "STOCKHOLM"));

    assertNull(postnummerService.getOmradeByPostnummer(null));
    assertNull(postnummerService.getOmradeByPostnummer(""));
    assertNull(postnummerService.getOmradeByPostnummer("xxyy"));
    assertEquals(omrade13061, postnummerService.getOmradeByPostnummer("13061"));
    assertEquals(omrade13100, postnummerService.getOmradeByPostnummer("13100"));
    assertEquals(omrade13155, postnummerService.getOmradeByPostnummer("13155"));
    assertNotEquals(omrade13061, postnummerService.getOmradeByPostnummer("13155"));
  }

  @Test
  void testGetKommunList() {
    String[] verify = {"HANINGE", "NACKA", "STOCKHOLM", "VÄSTERVIK", "LINKÖPING"};

    assertTrue(postnummerService.getKommunList().containsAll(Arrays.asList(verify)));
  }
}
