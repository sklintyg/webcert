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
package se.inera.intyg.webcert.web.web.controller.testability;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.webcert.infra.security.common.model.Feature;

@RestController
@RequestMapping("/testability/config")
@Profile({"dev", "testability-api"})
public class ConfigurationResource {

  @Autowired private SecurityConfigurationLoader configLoader;

  private final HashMap<String, Feature> replacedFeatures = new HashMap<>();

  @PostMapping("/setfeatures")
  public ResponseEntity<String> setFeatures(@RequestBody List<Feature> features) {
    final var currentFeatures = getCurrentFeatures();
    for (var feature : features) {
      try {
        final var replacedFeature = switchFeature(feature, currentFeatures);
        replacedFeatures.putIfAbsent(replacedFeature.getName(), replacedFeature);
      } catch (NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Feature " + feature.getName() + " does not exist.");
      }
    }
    return ResponseEntity.<String>ok().build();
  }

  @GetMapping("/resetfeatures")
  public ResponseEntity<Void> resetFeatures() {
    final var currentFeatures = getCurrentFeatures();
    for (var feature : replacedFeatures.entrySet()) {
      switchFeature(feature.getValue(), currentFeatures);
    }
    replacedFeatures.clear();
    return ResponseEntity.ok().build();
  }

  private Feature switchFeature(Feature feature, List<Feature> currentFeatures) {
    final var featureToSwitch =
        currentFeatures.stream()
            .filter(f -> f.getName().equals(feature.getName()))
            .findFirst()
            .orElseThrow();
    final var indexToSwitch = currentFeatures.indexOf(featureToSwitch);
    currentFeatures.set(indexToSwitch, feature);
    return featureToSwitch;
  }

  private List<Feature> getCurrentFeatures() {
    return configLoader.getFeaturesConfiguration().getFeatures();
  }
}
