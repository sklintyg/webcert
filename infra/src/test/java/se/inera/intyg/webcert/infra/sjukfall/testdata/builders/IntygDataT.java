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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.webcert.infra.sjukfall.dto.Formaga;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;

/** Created by Magnus Ekstrand on 2016-02-10. */
public final class IntygDataT {

  private IntygDataT() {}

  public static class IntygDataBuilder implements Builder<IntygData> {

    private String intygId;
    private String diagnoskod;
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

    private List<String> biDiagnoser;
    private List<String> sysselsattning;

    private boolean enkeltIntyg;

    public IntygDataBuilder() {}

    public IntygDataBuilder intygsId(String intygsId) {
      this.intygId = intygsId;
      return this;
    }

    public IntygDataBuilder diagnoskod(String diagnoskod) {
      this.diagnoskod = diagnoskod;
      return this;
    }

    public IntygDataBuilder patientId(String patientId) {
      this.patientId = patientId;
      return this;
    }

    public IntygDataBuilder patientNamn(String patientNamn) {
      this.patientNamn = patientNamn;
      return this;
    }

    public IntygDataBuilder lakareId(String lakareId) {
      this.lakareId = lakareId;
      return this;
    }

    public IntygDataBuilder lakareNamn(String lakareNamn) {
      this.lakareNamn = lakareNamn;
      return this;
    }

    public IntygDataBuilder vardenhetId(String vardenhetId) {
      this.vardenhetId = vardenhetId;
      return this;
    }

    public IntygDataBuilder vardenhetNamn(String vardenhetNamn) {
      this.vardenhetNamn = vardenhetNamn;
      return this;
    }

    public IntygDataBuilder vardgivareId(String vardgivareId) {
      this.vardgivareId = vardgivareId;
      return this;
    }

    public IntygDataBuilder vardgivareNamn(String vardgivareNamn) {
      this.vardgivareNamn = vardgivareNamn;
      return this;
    }

    public IntygDataBuilder signeringsTidpunkt(LocalDateTime signeringsTidpunkt) {
      this.signeringsTidpunkt = signeringsTidpunkt;
      return this;
    }

    public IntygDataBuilder formagor(List<Formaga> formagor) {
      this.formagor = formagor;
      return this;
    }

    public IntygDataBuilder biDiagnoser(List<String> biDiagnoser) {
      this.biDiagnoser = biDiagnoser;
      return this;
    }

    public IntygDataBuilder sysselsattning(List<String> sysselsattning) {
      this.sysselsattning = sysselsattning;
      return this;
    }

    public IntygDataBuilder enkeltIntyg(boolean enkeltIntyg) {
      this.enkeltIntyg = enkeltIntyg;
      return this;
    }

    @Override
    public IntygData build() {
      IntygData intygData = new IntygData();
      intygData.setIntygId(this.intygId);
      intygData.setDiagnosKod(createDiagnosKod(this.diagnoskod));
      intygData.setBiDiagnoser(createDiagnosKoder(this.biDiagnoser));
      intygData.setPatientId(this.patientId);
      intygData.setPatientNamn(this.patientNamn);
      intygData.setLakareId(this.lakareId);
      intygData.setLakareNamn(this.lakareNamn);
      intygData.setVardenhetId(this.vardenhetId);
      intygData.setVardenhetNamn(this.vardenhetNamn);
      intygData.setVardgivareId(this.vardgivareId);
      intygData.setVardgivareNamn(this.vardgivareNamn);
      intygData.setFormagor(this.formagor);
      intygData.setSysselsattning(this.sysselsattning);
      intygData.setEnkeltIntyg(this.enkeltIntyg);
      intygData.setSigneringsTidpunkt(this.signeringsTidpunkt);

      return intygData;
    }

    private DiagnosKod createDiagnosKod(String diagnoskod) {
      return DiagnosKod.create(this.diagnoskod);
    }

    private List<DiagnosKod> createDiagnosKoder(List<String> diagnosKoder) {
      if (diagnosKoder == null || diagnosKoder.isEmpty()) {
        return new ArrayList<>();
      }

      return diagnosKoder.stream().map(DiagnosKod::create).collect(Collectors.toList());
    }
  }
}
