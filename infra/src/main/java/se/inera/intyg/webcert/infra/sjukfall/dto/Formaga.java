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

import java.time.LocalDate;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class Formaga {

  private LocalDate startdatum;
  private LocalDate slutdatum;

  private int nedsattning;

  public Formaga(LocalDate startdatum, LocalDate slutdatum, int nedsattning) {
    this.startdatum = startdatum;
    this.slutdatum = slutdatum;
    this.nedsattning = nedsattning;
  }

  public LocalDate getStartdatum() {
    return this.startdatum;
  }

  public LocalDate getSlutdatum() {
    return this.slutdatum;
  }

  public int getNedsattning() {
    return this.nedsattning;
  }

  @Override
  public String toString() {
    return "Formaga [startdatum="
        + startdatum
        + ", slutdatum="
        + slutdatum
        + ", nedsattning="
        + nedsattning
        + "]";
  }
}
