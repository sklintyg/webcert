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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Feature implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty private String name;
  @JsonProperty private String desc;
  @JsonProperty private Boolean global;
  @JsonProperty private List<String> intygstyper;

  public Feature() {}

  public Feature(Feature template) {
    this.name = template.name;
    this.desc = template.desc;
    this.global = template.global;
    if (template.intygstyper != null) {
      this.intygstyper = new ArrayList<>(template.intygstyper);
    } else {
      this.intygstyper = new ArrayList<>();
    }
  }

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

  public Boolean getGlobal() {
    return Optional.ofNullable(global).orElse(false);
  }

  public void setGlobal(Boolean global) {
    this.global = global;
  }

  public List<String> getIntygstyper() {
    if (intygstyper == null) {
      return new ArrayList<>();
    }
    return intygstyper;
  }

  public void setIntygstyper(List<String> intygstyper) {
    if (intygstyper == null) {
      this.intygstyper = new ArrayList<>();
    } else {
      this.intygstyper = new ArrayList<>(intygstyper);
    }
  }
}
