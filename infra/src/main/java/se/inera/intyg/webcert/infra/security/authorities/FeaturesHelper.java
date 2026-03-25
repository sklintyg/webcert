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

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.infra.security.common.model.Feature;

@Component
public class FeaturesHelper {

  private CommonFeaturesResolver featuresResolver;

  @Autowired
  public FeaturesHelper(CommonFeaturesResolver featuresResolver) {
    this.featuresResolver = featuresResolver;
  }

  public boolean isFeatureActive(String feature) {
    return ofNullable(featuresResolver.getFeatures().get(feature))
        .filter(Feature::getGlobal)
        .isPresent();
  }

  public boolean isFeatureActive(String feature, String certificateType) {
    return getCertificateTypesForFeature(feature).contains(certificateType);
  }

  public List<String> getCertificateTypesForFeature(String feature) {
    return ofNullable(featuresResolver.getFeatures().get(feature))
        .filter(Feature::getGlobal)
        .map(Feature::getIntygstyper)
        .orElse(Collections.emptyList());
  }
}
