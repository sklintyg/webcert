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
package se.inera.intyg.webcert.infra.dynamiclink.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import se.inera.intyg.infra.dynamiclink.model.DynamicLink;
import se.inera.intyg.infra.dynamiclink.repository.DynamicLinkRepository;

/** Created by eriklupander on 2017-05-03. */
@Service
public class DynamicLinkRepositoryImpl implements DynamicLinkRepository {

  static final Logger LOG = LoggerFactory.getLogger(DynamicLinkRepositoryImpl.class);

  @Value("${dynamic.links.file}")
  String location;

  @Autowired ResourceLoader resourceLoader;

  Map<String, DynamicLink> linkMap;

  @PostConstruct
  void initialize() {
    // FIXME: Legacy support, can be removed when local config has been substituted by refdata
    // (INTYG-7701)
    if (!ResourceUtils.isUrl(location)) {
      location = "file:" + location;
    }

    try {
      List<DynamicLink> dynamicLinks =
          new ObjectMapper()
              .readValue(
                  resourceLoader.getResource(location).getInputStream(),
                  new TypeReference<List<DynamicLink>>() {});
      this.linkMap =
          Collections.unmodifiableMap(
              dynamicLinks.stream()
                  .collect(Collectors.toMap(DynamicLink::getKey, Function.identity())));
    } catch (IOException e) {
      LOG.error("Error loading dynamic links from: " + location);
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Map<String, DynamicLink> getAll() {
    return this.linkMap;
  }
}
