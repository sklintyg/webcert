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
package se.inera.intyg.webcert.infra.sjukfall.testdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import se.inera.intyg.webcert.infra.sjukfall.dto.Formaga;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.testdata.builders.FormagaT;
import se.inera.intyg.webcert.infra.sjukfall.testdata.builders.IntygDataT;

/** Created by Magnus Ekstrand on 2016-02-11. */
public class SjukfallIntygLineMapper {

  Set<String[]> fields;

  public SjukfallIntygLineMapper() {
    fields = new HashSet<>();
  }

  public static List<IntygData> map(List<String> lines) {
    SjukfallIntygLineMapper mapper = new SjukfallIntygLineMapper();

    for (String line : lines) {
      String[] splitData = mapper.toArray(line);
      if (splitData != null) {
        mapper.fields.add(splitData);
      }
    }

    return mapper.map(mapper.fields);
  }

  private List<IntygData> map(Set<String[]> fields) {
    List<IntygData> intygData = new ArrayList<>();

    Iterator<String[]> iter = fields.iterator();
    while (iter.hasNext()) {
      intygData.add(intygData(iter.next()));
    }

    return intygData;
  }

  private IntygData intygData(String[] data) {
    StringListMapper slm = new StringListMapper();
    FormagaFieldMapper ffm = new FormagaFieldMapper();

    // CHECKSTYLE:OFF MagicNumber
    return new IntygDataT.IntygDataBuilder()
        .intygsId(data[0])
        .diagnoskod(data[5])
        .biDiagnoser(slm.map(data[6]))
        .patientId(data[1])
        .patientNamn(patientNamn(data[2], data[3], data[4]))
        .lakareId(data[9])
        .lakareNamn(data[10])
        .vardenhetId(data[7])
        .vardenhetNamn(data[8])
        .formagor(ffm.map(data[11]))
        .sysselsattning(slm.map(data[12]))
        .enkeltIntyg(Boolean.valueOf(data[13]))
        .signeringsTidpunkt(LocalDateTime.parse(data[14]))
        .build();
    // CHECKSTYLE:ON MagicNumber
  }

  private String patientNamn(String fnamn, String mnamn, String enamn) {
    String pnamn = "";

    if (fnamn != null) {
      pnamn = fnamn;
    }
    if (mnamn != null) {
      pnamn = pnamn.isEmpty() ? mnamn : pnamn + " " + mnamn;
    }
    if (enamn != null) {
      pnamn = pnamn.isEmpty() ? enamn : pnamn + " " + enamn;
    }

    return pnamn;
  }

  private String[] toArray(String csv) {
    if (csv != null) {
      return csv.split("\\s*,\\s*");
    }

    return null;
  }

  class FormagaFieldMapper {

    public List<Formaga> map(String arbetsformaga) {
      List<Formaga> formagaList = new ArrayList<>();
      String[] formagor = arbetsformaga.replace("[", "").replace("]", "").split("\\|");

      for (String formaga : formagor) {
        String[] arr = formaga.split(";");
        formagaList.add(
            formaga(LocalDate.parse(arr[0]), LocalDate.parse(arr[1]), Integer.parseInt(arr[2])));
      }

      return formagaList;
    }

    private Formaga formaga(LocalDate start, LocalDate slut, int nedsatthet) {
      return new FormagaT.FormagaBuilder()
          .startdatum(start)
          .slutdatum(slut)
          .nedsattning(nedsatthet)
          .build();
    }
  }

  class StringListMapper {

    public List<String> map(String str) {
      if (str == null || str.length() == 0) {
        return new ArrayList<>();
      }

      String[] arr = str.replace("[", "").replace("]", "").split(";");
      return Arrays.asList(arr);
    }
  }
}
