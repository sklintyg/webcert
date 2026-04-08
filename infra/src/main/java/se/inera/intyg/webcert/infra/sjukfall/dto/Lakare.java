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
package se.inera.intyg.webcert.infra.sjukfall.dto;

import java.util.Objects;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class Lakare {

  private String id;
  private String namn;

  public static Lakare create(String lakareId, String lakareNamn) {
    final var lakare = new Lakare();
    lakare.id = lakareId;
    lakare.namn = lakareNamn;
    return lakare;
  }

  public String getId() {
    return id;
  }

  public String getNamn() {
    return namn;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setNamn(String namn) {
    this.namn = namn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Lakare lakare = (Lakare) o;
    return Objects.equals(id, lakare.id) && Objects.equals(namn, lakare.namn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, namn);
  }

  @Override
  public String toString() {
    return "Lakare{" + "id='" + id + '\'' + ", namn='" + namn + '\'' + '}';
  }
}
