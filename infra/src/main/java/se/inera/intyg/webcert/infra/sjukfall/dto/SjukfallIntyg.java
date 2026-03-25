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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.engine.SjukfallLangdCalculator;
import se.inera.intyg.infra.sjukfall.util.SysselsattningMapper;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class SjukfallIntyg extends IntygData {

  private static final int HASH_SEED = 31;

  // Intygets startdatum
  private LocalDate startDatum;

  // Intygets slutdatum
  private LocalDate slutDatum;

  // Totalt antal sjukskrivningsdagar för intyget
  private Integer dagar;

  // Nedsättning (arbetsförmåga)
  private List<Integer> grader;

  private boolean aktivtIntyg;

  private boolean nyligenAvslutat;

  private List<String> sysselsattning;

  public SjukfallIntyg(SjukfallIntygBuilder builder) {
    super();

    this.startDatum = builder.startDatum;
    this.slutDatum = builder.slutDatum;
    this.aktivtIntyg = builder.aktivtIntyg;
    this.nyligenAvslutat = builder.nyligenAvslutat;
    this.dagar = builder.dagar;
    this.grader = builder.grader;
    this.sysselsattning = builder.sysselsattning;
    this.setSysselsattning(builder.sysselsattning);

    this.setIntygId(builder.intygData.getIntygId());
    this.setDiagnosKod(builder.intygData.getDiagnosKod());
    this.setBiDiagnoser(builder.intygData.getBiDiagnoser());
    this.setPatientId(builder.intygData.getPatientId());
    this.setPatientNamn(builder.intygData.getPatientNamn());
    this.setLakareId(builder.intygData.getLakareId());
    this.setLakareNamn(builder.intygData.getLakareNamn());
    this.setVardenhetId(builder.intygData.getVardenhetId());
    this.setVardenhetNamn(builder.intygData.getVardenhetNamn());
    this.setVardgivareId(builder.intygData.getVardgivareId());
    this.setVardgivareNamn(builder.intygData.getVardgivareNamn());
    this.setSigneringsTidpunkt(builder.intygData.getSigneringsTidpunkt());
    this.setFormagor(builder.intygData.getFormagor());
    this.setEnkeltIntyg(builder.intygData.isEnkeltIntyg());
  }

  // Getters and setters

  public LocalDate getStartDatum() {
    return startDatum;
  }

  public LocalDate getSlutDatum() {
    return slutDatum;
  }

  public Integer getDagar() {
    return dagar;
  }

  public List<Integer> getGrader() {
    return grader;
  }

  public boolean isAktivtIntyg() {
    return aktivtIntyg;
  }

  public void setAktivtIntyg(boolean aktivtIntyg) {
    this.aktivtIntyg = aktivtIntyg;
  }

  public boolean isNyligenAvslutat() {
    return nyligenAvslutat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof SjukfallIntyg)) {
      return false;
    }

    SjukfallIntyg that = (SjukfallIntyg) o;
    return startDatum.equals(that.startDatum) && slutDatum.equals(that.slutDatum);
  }

  @Override
  public int hashCode() {
    int result = startDatum.hashCode();
    result = HASH_SEED * result + slutDatum.hashCode();
    result = HASH_SEED * result + (aktivtIntyg ? 1 : 0);
    return result;
  }

  public static class SjukfallIntygBuilder {

    private final IntygData intygData;

    private LocalDate startDatum;
    private LocalDate slutDatum;

    private Integer dagar;
    private List<Integer> grader;

    private List<String> sysselsattning;

    private boolean aktivtIntyg;

    private boolean nyligenAvslutat;

    public SjukfallIntygBuilder(
        IntygData intygData, LocalDate aktivtDatum, int maxAntalDagarSedanSjukfallAvslut) {
      this.intygData = intygData;
      this.startDatum = lookupStartDatum(intygData.getFormagor());
      this.slutDatum = lookupSlutDatum(intygData.getFormagor());
      this.dagar = getDagar(intygData.getFormagor());
      this.grader = getGrader(intygData.getFormagor());
      this.sysselsattning = getSysselsattning(intygData.getSysselsattning());
      this.aktivtIntyg = isAktivtIntyg(intygData, aktivtDatum);
      this.nyligenAvslutat =
          !aktivtIntyg
              && slutDatum.isBefore(aktivtDatum)
              && slutDatum.plusDays(maxAntalDagarSedanSjukfallAvslut + 1).isAfter(aktivtDatum);
    }

    public SjukfallIntyg build() {
      return new SjukfallIntyg(this);
    }

    private Integer getDagar(List<Formaga> formagor) {
      return SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByFormaga(formagor);
    }

    private List<Integer> getGrader(List<Formaga> formagor) {
      return formagor.stream()
          .sorted(Comparator.comparing(Formaga::getStartdatum))
          .map(Formaga::getNedsattning)
          .collect(Collectors.toList());
    }

    private List<String> getSysselsattning(List<String> sysselsattning) {
      if (sysselsattning == null) {
        return new ArrayList<>();
      }
      return SysselsattningMapper.mapSysselsattning(sysselsattning);
    }

    private boolean hasAktivFormaga(List<Formaga> formagor, LocalDate aktivtDatum) {
      return formagor.stream().anyMatch(f -> isAktivFormaga(aktivtDatum, f));
    }

    private boolean isAktivFormaga(LocalDate aktivtDatum, Formaga f) {
      return f.getStartdatum().compareTo(aktivtDatum) < 1
          && f.getSlutdatum().compareTo(aktivtDatum) > -1;
    }

    private boolean isAktivtIntyg(IntygData intygData, LocalDate aktivtDatum) {
      return aktivtDatum != null && hasAktivFormaga(intygData.getFormagor(), aktivtDatum);
    }

    private LocalDate lookupStartDatum(List<Formaga> formagor) {
      Formaga formaga = formagor.stream().min(Comparator.comparing(Formaga::getStartdatum)).get();
      return formaga.getStartdatum();
    }

    private LocalDate lookupSlutDatum(List<Formaga> formagor) {
      Formaga formaga = formagor.stream().max(Comparator.comparing(Formaga::getSlutdatum)).get();
      return formaga.getSlutdatum();
    }
  }

  @Override
  public String toString() {
    return "SjukfallIntyg [startDatum="
        + startDatum
        + ", slutDatum="
        + slutDatum
        + ", dagar="
        + dagar
        + ", grader="
        + grader
        + ", aktivtIntyg="
        + aktivtIntyg
        + ", nyligenAvslutat="
        + nyligenAvslutat
        + "]";
  }
}
