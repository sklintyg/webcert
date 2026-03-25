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
package se.inera.intyg.webcert.infra.srs.stub.repository.model;

import java.util.Objects;
import se.inera.intyg.schemas.contract.Personnummer;

public final class Individ {

  private final Personnummer personnummer;
  private final String caregiverId;

  public Individ(Personnummer personnummer, String caregiverId) {
    this.personnummer = personnummer;
    this.caregiverId = caregiverId;
  }

  public Personnummer getPersonnummer() {
    return personnummer;
  }

  public String getCaregiverId() {
    return caregiverId;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Individ) {
      return Objects.equals(this.personnummer, ((Individ) o).getPersonnummer())
          && Objects.equals(this.caregiverId, ((Individ) o).getCaregiverId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = personnummer.hashCode();
    result = prime * result + caregiverId.hashCode();
    return result;
  }
}
