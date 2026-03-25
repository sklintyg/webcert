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
package se.inera.intyg.webcert.infra.srs.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Created by marced on 2017-11-06. */
public final class SrsForDiagnosisResponse {

  private final String diagnosisCode;
  private final String atgarderStatusCode;
  private final ImmutableList<String> atgarderObs;
  private final ImmutableList<String> atgarderRek;
  private final String statistikStatusCode;
  private final String statistikDiagnosisCode;

  public SrsForDiagnosisResponse(
      List<String> atgarderObs,
      List<String> atgarderRek,
      String diagnosisCode,
      String atgarderStatusCode,
      String statistikStatusCode,
      String statistikDiagnosisCode) {
    if (atgarderObs == null) {
      this.atgarderObs = null;
    } else {
      this.atgarderObs = ImmutableList.copyOf(atgarderObs);
    }
    if (atgarderRek == null) {
      this.atgarderRek = null;
    } else {
      this.atgarderRek = ImmutableList.copyOf(atgarderRek);
    }
    this.diagnosisCode = diagnosisCode;
    this.atgarderStatusCode = atgarderStatusCode;
    this.statistikStatusCode = statistikStatusCode;
    this.statistikDiagnosisCode = statistikDiagnosisCode;
  }

  public ImmutableList<String> getAtgarderObs() {
    return atgarderObs;
  }

  public ImmutableList<String> getAtgarderRek() {
    return atgarderRek;
  }

  public String getDiagnosisCode() {
    return diagnosisCode;
  }

  public String getAtgarderStatusCode() {
    return atgarderStatusCode;
  }

  public String getStatistikStatusCode() {
    return statistikStatusCode;
  }

  public String getStatistikDiagnosisCode() {
    return statistikDiagnosisCode;
  }
}
