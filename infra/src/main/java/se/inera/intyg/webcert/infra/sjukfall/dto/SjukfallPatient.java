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
import java.util.List;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class SjukfallPatient {

  // Patientuppgifter
  private Patient patient;

  // Huvuddiagnos
  private DiagnosKod diagnosKod;

  // Sjukfallets startdatum
  private LocalDate start;

  // Sjukfallets slutdatum
  private LocalDate slut;

  // Totalt antal sjukskrivningsdagar för sjukfallet
  private Integer dagar;

  // De intyg som ingår i sjukfallet
  private List<SjukfallIntyg> sjukfallIntygList;

  // getters and setters

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public DiagnosKod getDiagnosKod() {
    return diagnosKod;
  }

  public void setDiagnosKod(DiagnosKod diagnosKod) {
    this.diagnosKod = diagnosKod;
  }

  public LocalDate getStart() {
    return start;
  }

  public void setStart(LocalDate start) {
    this.start = start;
  }

  public LocalDate getSlut() {
    return slut;
  }

  public void setSlut(LocalDate slut) {
    this.slut = slut;
  }

  public Integer getDagar() {
    return dagar;
  }

  public void setDagar(Integer dagar) {
    this.dagar = dagar;
  }

  public List<SjukfallIntyg> getSjukfallIntygList() {
    return sjukfallIntygList;
  }

  public void setSjukfallIntygList(List<SjukfallIntyg> sjukfallIntygList) {
    this.sjukfallIntygList = sjukfallIntygList;
  }
}
