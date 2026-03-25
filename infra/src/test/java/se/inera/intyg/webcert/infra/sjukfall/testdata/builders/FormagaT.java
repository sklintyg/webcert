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
package se.inera.intyg.webcert.infra.sjukfall.testdata.builders;

import java.time.LocalDate;
import se.inera.intyg.infra.sjukfall.dto.Formaga;

/** Created by Magnus Ekstrand on 2016-02-10. */
public final class FormagaT {

  public FormagaT() {}

  public static class FormagaBuilder implements Builder<Formaga> {

    private LocalDate startdatum;
    private LocalDate slutdatum;
    private int nedsattning;

    public FormagaBuilder() {}

    public FormagaBuilder startdatum(LocalDate startdatum) {
      this.startdatum = startdatum;
      return this;
    }

    public FormagaBuilder slutdatum(LocalDate slutdatum) {
      this.slutdatum = slutdatum;
      return this;
    }

    public FormagaBuilder nedsattning(int nedsattning) {
      this.nedsattning = nedsattning;
      return this;
    }

    @Override
    public Formaga build() {
      Formaga formaga = new Formaga(startdatum, slutdatum, nedsattning);
      return formaga;
    }
  }
}
