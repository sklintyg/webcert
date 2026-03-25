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
package se.inera.intyg.webcert.infra.security.authorities;

import static se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil.toMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.infra.security.common.model.Feature;

/**
 * @Author Joy Zomborszki on 2018-06-18.
 */
@Service
public class CommonFeaturesResolver {

  private static final Logger LOG = LoggerFactory.getLogger(CommonFeaturesResolver.class);

  @Autowired private SecurityConfigurationLoader configurationLoader;

  public Map<String, Feature> getFeatures() {
    List<Feature> featureList =
        configurationLoader.getFeaturesConfiguration().getFeatures().stream()
            .map(Feature::new)
            .collect(Collectors.toList());

    return toMap(featureList, Feature::getName);
  }

  public SecurityConfigurationLoader getConfigurationLoader() {
    return configurationLoader;
  }

  public void setConfigurationLoader(SecurityConfigurationLoader configurationLoader) {
    this.configurationLoader = configurationLoader;
  }
}
