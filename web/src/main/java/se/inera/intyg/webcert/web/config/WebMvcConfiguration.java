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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import se.inera.intyg.common.util.integration.json.CustomLocalDateTimeDeserializer;

@Configuration
@EnableWebMvc
@ComponentScan("se.inera.intyg.webcert.web.web.controller")
@ComponentScan(
    value = "se.inera.intyg.webcert.notificationstub",
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
@ComponentScan(
    value = "se.inera.intyg.webcert.integration.fmb.stub",
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
public class WebMvcConfiguration implements WebMvcConfigurer {

  @Autowired private ObjectMapper objectMapper;

  /**
   * Ensures {@link CustomLocalDateTimeDeserializer} is used for all {@code LocalDateTime} JSON
   * deserialization, overriding any module-registered deserializer (e.g. from JavaTimeModule).
   * The mixin takes precedence over module registrations, so date-only strings such as
   * {@code "2025-12-31"} are correctly parsed to start-of-day without per-field annotations.
   */
  @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
  private abstract static class LocalDateTimeMixin {}

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    objectMapper.addMixIn(LocalDateTime.class, LocalDateTimeMixin.class);
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
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
