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
package se.inera.intyg.webcert.infra.security.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public final class Pilot implements Serializable {

  @JsonProperty private String name;
  @JsonProperty private String desc;
  @JsonProperty private List<String> hsaIds;
  @JsonProperty private List<Feature> activated;
  @JsonProperty private List<Feature> deactivated;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public List<String> getHsaIds() {
    return hsaIds;
  }

  public void setHsaIds(List<String> hsaIds) {
    if (hsaIds == null) {
      this.hsaIds = ImmutableList.of();
    } else {
      this.hsaIds = ImmutableList.copyOf(hsaIds);
    }
  }

  public List<Feature> getActivated() {
    if (activated == null) {
      return ImmutableList.of();
    }
    return activated;
  }

  public void setActivated(List<Feature> features) {
    if (features == null) {
      activated = ImmutableList.of();
    } else {
      activated =
          ImmutableList.copyOf(features.stream().map(Feature::new).collect(Collectors.toList()));
    }
  }

  public List<Feature> getDeactivated() {
    if (deactivated == null) {
      return ImmutableList.of();
    }
    return deactivated;
  }

  public void setDeactivated(List<Feature> features) {
    if (features == null) {
      deactivated = ImmutableList.of();
    } else {
      deactivated =
          ImmutableList.copyOf(features.stream().map(Feature::new).collect(Collectors.toList()));
    }
  }
}
