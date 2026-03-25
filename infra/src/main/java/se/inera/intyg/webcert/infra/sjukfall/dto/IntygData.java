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
package se.inera.intyg.webcert.infra.sjukfall.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class IntygData {

  private String intygId;
  private String patientId;
  private String patientNamn;
  private String lakareId;
  private String lakareNamn;
  private String vardenhetId;
  private String vardenhetNamn;
  private String vardgivareId;
  private String vardgivareNamn;

  private LocalDateTime signeringsTidpunkt;

  private List<Formaga> formagor;

  private DiagnosKod diagnosKod;
  private List<DiagnosKod> biDiagnoser;

  private List<String> sysselsattning;

  private boolean enkeltIntyg;

  // getter and setters

  public String getIntygId() {
    return intygId;
  }

  public void setIntygId(String intygId) {
    this.intygId = intygId;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getPatientNamn() {
    return patientNamn;
  }

  public void setPatientNamn(String patientNamn) {
    this.patientNamn = patientNamn;
  }

  public String getLakareId() {
    return lakareId;
  }

  public void setLakareId(String lakareId) {
    this.lakareId = lakareId;
  }

  public String getLakareNamn() {
    return lakareNamn;
  }

  public void setLakareNamn(String lakareNamn) {
    this.lakareNamn = lakareNamn;
  }

  public String getVardenhetId() {
    return vardenhetId;
  }

  public void setVardenhetId(String vardenhetId) {
    this.vardenhetId = vardenhetId;
  }

  public String getVardenhetNamn() {
    return vardenhetNamn;
  }

  public void setVardenhetNamn(String vardenhetNamn) {
    this.vardenhetNamn = vardenhetNamn;
  }

  public String getVardgivareId() {
    return vardgivareId;
  }

  public void setVardgivareId(String vardgivareId) {
    this.vardgivareId = vardgivareId;
  }

  public String getVardgivareNamn() {
    return vardgivareNamn;
  }

  public void setVardgivareNamn(String vardgivareNamn) {
    this.vardgivareNamn = vardgivareNamn;
  }

  public LocalDateTime getSigneringsTidpunkt() {
    return signeringsTidpunkt;
  }

  public void setSigneringsTidpunkt(LocalDateTime signeringsTidpunkt) {
    this.signeringsTidpunkt = signeringsTidpunkt;
  }

  public List<Formaga> getFormagor() {
    return formagor;
  }

  public void setFormagor(List<Formaga> formagor) {
    this.formagor = formagor;
  }

  public boolean isEnkeltIntyg() {
    return enkeltIntyg;
  }

  public void setEnkeltIntyg(boolean enkeltIntyg) {
    this.enkeltIntyg = enkeltIntyg;
  }

  public DiagnosKod getDiagnosKod() {
    return diagnosKod;
  }

  public void setDiagnosKod(DiagnosKod diagnosKod) {
    this.diagnosKod = diagnosKod;
  }

  public List<DiagnosKod> getBiDiagnoser() {
    return biDiagnoser;
  }

  public void setBiDiagnoser(List<DiagnosKod> biDiagnoser) {
    this.biDiagnoser = biDiagnoser;
  }

  public List<String> getSysselsattning() {
    return sysselsattning;
  }

  public void setSysselsattning(List<String> sysselsattning) {
    this.sysselsattning = sysselsattning;
  }
}
