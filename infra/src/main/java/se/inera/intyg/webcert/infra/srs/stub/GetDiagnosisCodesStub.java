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
package se.inera.intyg.webcert.infra.srs.stub;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.Diagnos;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponseType;

public class GetDiagnosisCodesStub implements GetDiagnosisCodesResponderInterface {

  public static List<String> allValidDiagnosis = ImmutableList.of("M18", "J20", "Q10");

  @Override
  public GetDiagnosisCodesResponseType getDiagnosisCodes(
      GetDiagnosisCodesRequestType getDiagnosisCodesRequestType) {
    GetDiagnosisCodesResponseType response = new GetDiagnosisCodesResponseType();
    response
        .getDiagnos()
        .addAll(allValidDiagnosis.stream().map(this::createDiagnos).collect(Collectors.toList()));
    return response;
  }

  private Diagnos createDiagnos(String diagnosisCode) {
    Diagnos diagnos = new Diagnos();
    diagnos.setCode(diagnosisCode);
    diagnos.setCodeSystem("1.2.752.116.1.1.1.1.3");
    return diagnos;
  }
}
