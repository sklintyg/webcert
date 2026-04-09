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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration. All component scanning is handled by {@code @SpringBootApplication} on {@link
 * se.inera.intyg.webcert.WebcertApplication}.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  @Autowired private ObjectMapper objectMapper;

  /**
   * Swaps the ObjectMapper on the existing default Jackson converter to use our {@code
   * CustomObjectMapper} bean (which handles LocalDateTime as ISO strings).
   *
   * <p>Uses {@code extendMessageConverters} (not {@code configureMessageConverters}) to preserve
   * all default converters — ByteArrayHttpMessageConverter (PDF), StringHttpMessageConverter (raw
   * strings), etc. The ObjectMapper is set in-place so the converter keeps its original position
   * after StringHttpMessageConverter, avoiding double-encoding of String responses.
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    for (HttpMessageConverter<?> converter : converters) {
      if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
        jacksonConverter.setObjectMapper(objectMapper);
        break;
      }
    }
  }
}
