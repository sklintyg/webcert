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

import se.inera.intyg.webcert.infra.sjukfall.dto.Vardenhet;

/** Created by Magnus Ekstrand on 2016-02-10. */
public final class VardenhetT {

  public VardenhetT() {}

  public static class VardenhetBuilder implements Builder<Vardenhet> {

    private String enhetsId;
    private String enhetsnamn;

    public VardenhetBuilder() {}

    public VardenhetBuilder enhetsId(String enhetsId) {
      this.enhetsId = enhetsId;
      return this;
    }

    public VardenhetBuilder enhetsnamn(String enhetsnamn) {
      this.enhetsnamn = enhetsnamn;
      return this;
    }

    @Override
    public Vardenhet build() {
      Vardenhet enhet = Vardenhet.create(enhetsId, enhetsnamn);
      return enhet;
    }
  }
}
