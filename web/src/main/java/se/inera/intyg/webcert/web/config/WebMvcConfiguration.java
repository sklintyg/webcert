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
package se.inera.intyg.webcert.web.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(new CustomObjectMapper());
    converters.addFirst(converter);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(
        String.class,
        LocalDateTime.class,
        source -> {
          if (source.contains("T")) {
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
          }
          return LocalDate.parse(source, DateTimeFormatter.ISO_DATE).atStartOfDay();
        });
    registry.addConverter(
        String.class,
        LocalDate.class,
        source -> LocalDate.parse(source, DateTimeFormatter.ISO_DATE));
  }
}
