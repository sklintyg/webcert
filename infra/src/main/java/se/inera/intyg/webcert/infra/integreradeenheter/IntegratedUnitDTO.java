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
package se.inera.intyg.webcert.infra.integreradeenheter;

import java.time.LocalDateTime;

public class IntegratedUnitDTO {

  private String enhetsId;
  private String enhetsNamn;
  private String vardgivarId;
  private String vardgivarNamn;
  private LocalDateTime skapadDatum;
  private LocalDateTime senasteKontrollDatum;

  public IntegratedUnitDTO() {}

  public IntegratedUnitDTO(
      String enhetsId,
      String enhetsNamn,
      String vardgivarId,
      String vardgivarNamn,
      LocalDateTime skapadDatum,
      LocalDateTime senasteKontrollDatum) {
    this.enhetsId = enhetsId;
    this.enhetsNamn = enhetsNamn;
    this.vardgivarId = vardgivarId;
    this.vardgivarNamn = vardgivarNamn;
    this.skapadDatum = skapadDatum;
    this.senasteKontrollDatum = senasteKontrollDatum;
  }

  public String getEnhetsId() {
    return enhetsId;
  }

  public void setEnhetsId(String enhetsId) {
    this.enhetsId = enhetsId;
  }

  public String getEnhetsNamn() {
    return enhetsNamn;
  }

  public void setEnhetsNamn(String enhetsNamn) {
    this.enhetsNamn = enhetsNamn;
  }

  public String getVardgivarId() {
    return vardgivarId;
  }

  public void setVardgivarId(String vardgivarId) {
    this.vardgivarId = vardgivarId;
  }

  public String getVardgivarNamn() {
    return vardgivarNamn;
  }

  public void setVardgivarNamn(String vardgivarNamn) {
    this.vardgivarNamn = vardgivarNamn;
  }

  public LocalDateTime getSkapadDatum() {
    return skapadDatum;
  }

  public void setSkapadDatum(LocalDateTime skapadDatum) {
    this.skapadDatum = skapadDatum;
  }

  public LocalDateTime getSenasteKontrollDatum() {
    return senasteKontrollDatum;
  }

  public void setSenasteKontrollDatum(LocalDateTime senasteKontrollDatum) {
    this.senasteKontrollDatum = senasteKontrollDatum;
  }
}
