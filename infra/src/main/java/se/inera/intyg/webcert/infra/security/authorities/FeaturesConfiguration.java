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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.infra.security.common.model.Pilot;

public final class FeaturesConfiguration {

  @JsonProperty private List<String> knownIntygstyper;
  @JsonProperty private List<String> knownFeatures;
  @JsonProperty private List<Feature> features;
  @JsonProperty private List<Pilot> pilots;

  public List<String> getKnownIntygstyper() {
    return knownIntygstyper;
  }

  public void setKnownIntygstyper(List<String> knownIntygstyper) {
    if (knownIntygstyper == null) {
      this.knownIntygstyper = Collections.emptyList();
    } else {
      this.knownIntygstyper = knownIntygstyper;
    }
  }

  public List<String> getKnownFeatures() {
    return knownFeatures;
  }

  public void setKnownFeatures(List<String> knownFeatures) {
    if (knownFeatures == null) {
      this.knownFeatures = Collections.emptyList();
    } else {
      this.knownFeatures = knownFeatures;
    }
  }

  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(List<Feature> features) {
    if (features == null) {
      this.features = Collections.emptyList();
    } else {
      this.features = features;
    }
  }

  public List<Pilot> getPilots() {
    return pilots;
  }

  public void setPilots(List<Pilot> pilots) {
    if (pilots == null) {
      this.pilots = Collections.emptyList();
    } else {
      this.pilots = pilots;
    }
  }
}
