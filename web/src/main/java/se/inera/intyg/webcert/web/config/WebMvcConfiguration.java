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

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

/**
 * MVC configuration. All component scanning is handled by {@code @SpringBootApplication} on {@link
 * se.inera.intyg.webcert.WebcertApplication}.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {
  /**
   * Replaces the default Spring Boot Jackson 3 converter with one built from our auto-configured
   * {@code JsonMapper} bean (which picks up any {@code JsonMapperBuilderCustomizer} beans and
   * handles LocalDateTime as ISO strings).
   *
   * <p>Uses {@code withJsonConverter} to replace only the default JSON converter while preserving
   * all other default converters — ByteArrayHttpMessageConverter (PDF), StringHttpMessageConverter
   * (raw strings), etc.
   */
  private final JsonMapper objectMapper;

  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    builder.withJsonConverter(new JacksonJsonHttpMessageConverter(objectMapper));
  }
}
