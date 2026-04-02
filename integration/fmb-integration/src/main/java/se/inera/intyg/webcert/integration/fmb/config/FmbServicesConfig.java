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
package se.inera.intyg.webcert.integration.fmb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumer;
import se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumerImpl;

@Configuration
@ComponentScans(
    value = {
      @ComponentScan("se.inera.intyg.webcert.integration.fmb.services"),
      @ComponentScan("se.inera.intyg.webcert.integration.fmb.stub"),
    })
public class FmbServicesConfig {

  @Bean
  public FmbConsumer fmbConsumer(@Value("${fmb.endpoint.url}") String baseUrl) {
    return new FmbConsumerImpl(baseUrl);
  }
}
