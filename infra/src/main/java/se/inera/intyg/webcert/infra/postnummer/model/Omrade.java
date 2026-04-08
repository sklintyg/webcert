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
package se.inera.intyg.webcert.infra.postnummer.model;

public class Omrade {

  String postnummer;
  String postort;
  String kommun;
  String lan;

  public Omrade(String postnummer, String postort, String kommun, String lan) {
    this.postnummer = postnummer;
    this.postort = postort;
    this.kommun = kommun;
    this.lan = lan;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Omrade)) {
      return false;
    }
    Omrade other = (Omrade) o;
    return postnummer.equals(other.postnummer)
        && postort.equals(other.postort)
        && kommun.equals(other.kommun)
        && lan.equals(other.lan);
  }

  @Override
  public int hashCode() {
    // CHECKSTYLE:OFF MagicNumber
    int result = postnummer.hashCode();
    result = 31 * result + postort.hashCode();
    result = 31 * result + kommun.hashCode();
    result = 31 * result + lan.hashCode();
    return result;
    // CHECKSTYLE:ON MagicNumber
  }

  public String getPostnummer() {
    return postnummer;
  }

  public void setPostnummer(String postnummer) {
    this.postnummer = postnummer;
  }

  public String getPostort() {
    return postort;
  }

  public void setPostort(String postort) {
    this.postort = postort;
  }

  public String getKommun() {
    return kommun;
  }

  public void setKommun(String kommun) {
    this.kommun = kommun;
  }

  public String getLan() {
    return lan;
  }

  public void setLan(String lan) {
    this.lan = lan;
  }
}
