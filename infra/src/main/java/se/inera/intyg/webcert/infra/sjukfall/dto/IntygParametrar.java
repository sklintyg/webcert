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
 * @author Magnus Ekstrand on 2017-02-14.
 */
public class IntygParametrar {

  private int maxIntygsGlapp;
  private int maxAntalDagarSedanSjukfallAvslut;
  private LocalDate aktivtDatum;

  public IntygParametrar(int maxIntygsGlapp, LocalDate aktivtDatum) {
    this(maxIntygsGlapp, 0, aktivtDatum);
  }

  public IntygParametrar(
      int maxIntygsGlapp, int maxAntalDagarSedanSjukfallAvslut, LocalDate aktivtDatum) {
    this.maxIntygsGlapp = maxIntygsGlapp;
    this.maxAntalDagarSedanSjukfallAvslut = maxAntalDagarSedanSjukfallAvslut;
    this.aktivtDatum = aktivtDatum;
  }

  // getters

  public int getMaxIntygsGlapp() {
    return maxIntygsGlapp;
  }

  public int getMaxAntalDagarSedanSjukfallAvslut() {
    return maxAntalDagarSedanSjukfallAvslut;
  }

  public LocalDate getAktivtDatum() {
    return aktivtDatum;
  }
}
