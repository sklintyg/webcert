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
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan("se.inera.intyg.webcert.web.web.controller")
@ComponentScan(
    value = "se.inera.intyg.webcert.notificationstub",
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
@ComponentScan(
    value = "se.inera.intyg.webcert.integration.fmb.stub",
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
@ComponentScan(
    value = {
      "se.inera.intyg.webcert.integration.servicenow.stub.api",
      "se.inera.intyg.webcert.integration.servicenow.stub.settings.api"
    },
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
public class WebMvcConfiguration implements WebMvcConfigurer {

  @Autowired private ObjectMapper objectMapper;

  /**
   * Replaces the default Jackson converter with one using our {@code CustomObjectMapper} bean
   * (which already handles LocalDateTime serialization as ISO strings). The converter is replaced
   * in-place so that StringHttpMessageConverter keeps its earlier position and handles raw String
   * responses from stubs correctly.
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    converters.add(converter);

    final var stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
    stringConverter.setSupportedMediaTypes(
        List.of(
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_HTML,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            MediaType.ALL));
    converters.add(stringConverter);
  }
}
