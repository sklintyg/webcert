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
package se.inera.intyg.webcert.infra.monitoring.annotation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.webcert.infra.monitoring.MonitoringConfiguration;
import se.inera.intyg.webcert.infra.monitoring.TestController;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MonitoringConfiguration.class, TestController.class})
class TimeMethodTest {

  CollectorRegistry registry = CollectorRegistry.defaultRegistry;

  @Autowired TestController testController;

  @BeforeEach
  void before() throws InterruptedException {
    this.testController.named();
    this.testController.named2();
    this.testController.unnamed("", Collections.EMPTY_LIST);
  }

  @Test
  void instrumented_named_method() {

    final Optional<Collector.MetricFamilySamples> sample =
        Collections.list(registry.metricFamilySamples()).stream()
            .filter(s -> TestController.SAMPLE_NAME.equals(s.name))
            .findFirst();

    assertTrue(sample.isPresent());
    assertFalse(sample.get().samples.isEmpty());
    assertNotNull(sample.get().help);
  }

  @Test
  void instrumented_duplicate_named_method() {

    final Optional<Collector.MetricFamilySamples> sample =
        Collections.list(registry.metricFamilySamples()).stream()
            .filter(s -> s.name.equalsIgnoreCase(TestController.SAMPLE_NAME + "_1"))
            .findFirst();

    assertTrue(sample.isPresent());
    assertFalse(sample.get().samples.isEmpty());
    assertNotNull(sample.get().help);
  }

  @Test
  void instrumented_unnamed_method() {
    final Optional<Collector.MetricFamilySamples> sample =
        Collections.list(registry.metricFamilySamples()).stream()
            .filter(s -> s.name.startsWith("api_"))
            .findFirst();

    assertTrue(sample.isPresent());
    assertFalse(sample.get().samples.isEmpty());
    assertNotNull(sample.get().help);
  }
}
