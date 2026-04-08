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
package se.inera.intyg.webcert.infra.integration.hsatk.model.legacy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Vardenhet extends AbstractVardenhet {

  private static final long serialVersionUID = 460424685142490104L;

  private LocalDateTime start;
  private LocalDateTime end;

  private String vardgivareHsaId;

  private List<Mottagning> mottagningar = new ArrayList<>();

  public Vardenhet() {
    // Needed for deserialization
  }

  public Vardenhet(String id, String namn) {
    super(id, namn);
  }

  public Vardenhet(String id, String namn, LocalDateTime start, LocalDateTime end) {
    super(id, namn);
    this.start = start;
    this.end = end;
  }

  public Vardenhet(
      String id, String namn, LocalDateTime start, LocalDateTime end, String vardgivareHsaId) {
    this(id, namn, start, end);
    this.vardgivareHsaId = vardgivareHsaId;
  }

  public void setMottagningar(List<Mottagning> mottagningar) {
    this.mottagningar = mottagningar;
  }

  @Override
  public List<String> getHsaIds() {
    List<String> ids = new ArrayList<>();
    ids.add(getId());
    for (Mottagning mottagning : getMottagningar()) {
      ids.add(mottagning.getId());
    }
    return ids;
  }

  public SelectableVardenhet findSelectableVardenhet(String id) {

    if (id.equals(getId())) {
      return this;
    }

    for (Mottagning m : getMottagningar()) {
      if (id.equals(m.getId())) {
        return m;
      }
    }

    return null;
  }
}
