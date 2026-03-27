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

import java.io.Serializable;
import lombok.Data;

@Data
public abstract class AbstractVardenhet
    implements SelectableVardenhet, Comparable<AbstractVardenhet>, Serializable {

  private static final long serialVersionUID = 304219756695002501L;

  private String id;

  private String namn;

  private String epost;

  private String postadress;

  private String postnummer;

  private String postort;

  private String telefonnummer;

  private String arbetsplatskod;

  // Store vardgivareOrgnr here instead of Vardgivare to match structure from HSA
  // If vardgivareOrgnr begins with 2 agandeFrom will be set to OFFENTLIG
  private String vardgivareOrgnr;

  private AgandeForm agandeForm;

  public AbstractVardenhet() {
    // Needed for deserialization
  }

  public AbstractVardenhet(String id, String namn) {
    this.id = id;
    this.namn = namn;
  }

  public AbstractVardenhet(String id, String namn, String vardgivareOrgnr) {
    this.id = id;
    this.namn = namn;
    this.vardgivareOrgnr = vardgivareOrgnr;
  }

  @Override
  public int compareTo(AbstractVardenhet annanVardenhet) {
    return getNamn().compareTo(annanVardenhet.getNamn());
  }

  @Override
  public String toString() {
    return new StringBuilder(getNamn()).append(":").append(getId()).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractVardenhet)) {
      return false;
    }

    AbstractVardenhet that = (AbstractVardenhet) o;

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getNamn() {
    return namn;
  }
}
