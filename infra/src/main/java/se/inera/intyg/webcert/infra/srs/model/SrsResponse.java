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
package se.inera.intyg.webcert.infra.srs.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

// CHECKSTYLE:OFF ParameterNumber
public class SrsResponse {

  private ImmutableList<SrsRecommendation> atgarderObs;
  private ImmutableList<SrsRecommendation> atgarderRek;
  private ImmutableList<SrsRecommendation> atgarderFrl;
  private ImmutableList<SrsRecommendation> atgarderReh;
  private ImmutableList<SrsPrediction> predictions;
  private String atgarderDiagnosisCode;
  private String atgarderDiagnosisDescription;
  private String atgarderStatusCode;
  private ImmutableList<Integer> statistikNationellStatistik;
  private String statistikDiagnosisCode;
  private String statistikDiagnosisDescription;
  private String statistikStatusCode;
  private List<SrsCertificate>
      extensionChain; // TODO: replace with startingPoint/active view etc enum

  public SrsResponse(
      List<SrsRecommendation> atgarderObs,
      List<SrsRecommendation> atgarderRek,
      List<SrsRecommendation> atgarderFrl,
      List<SrsRecommendation> atgarderReh,
      List<SrsPrediction> predictions,
      String atgarderDiagnosisCode,
      String atgarderStatusCode,
      String statistikDiagnosisCode,
      String statistikStatusCode,
      List<Integer> statistikNationellStatistikData) {

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
    if (atgarderFrl == null) {
      this.atgarderFrl = null;
    } else {
      this.atgarderFrl = ImmutableList.copyOf(atgarderFrl);
    }
    if (atgarderReh == null) {
      this.atgarderReh = null;
    } else {
      this.atgarderReh = ImmutableList.copyOf(atgarderReh);
    }

    if (statistikNationellStatistikData == null) {
      this.statistikNationellStatistik = null;
    } else {
      this.statistikNationellStatistik = ImmutableList.copyOf(statistikNationellStatistikData);
    }

    if (predictions == null) {
      this.predictions = null;
    } else {
      this.predictions = ImmutableList.copyOf(predictions);
    }

    this.atgarderDiagnosisCode = atgarderDiagnosisCode;
    this.atgarderStatusCode = atgarderStatusCode;

    this.statistikDiagnosisCode = statistikDiagnosisCode;
    this.statistikStatusCode = statistikStatusCode;
  }

  public ImmutableList<SrsRecommendation> getAtgarderObs() {
    return atgarderObs;
  }

  public ImmutableList<SrsRecommendation> getAtgarderRek() {
    return atgarderRek;
  }

  public ImmutableList<SrsRecommendation> getAtgarderFrl() {
    return atgarderFrl;
  }

  public ImmutableList<SrsRecommendation> getAtgarderReh() {
    return atgarderReh;
  }

  public String getAtgarderStatusCode() {
    return atgarderStatusCode;
  }

  public String getStatistikStatusCode() {
    return statistikStatusCode;
  }

  public String getAtgarderDiagnosisCode() {
    return atgarderDiagnosisCode;
  }

  public String getAtgarderDiagnosisDescription() {
    return atgarderDiagnosisDescription;
  }

  public String getStatistikDiagnosisCode() {
    return statistikDiagnosisCode;
  }

  public String getStatistikDiagnosisDescription() {
    return statistikDiagnosisDescription;
  }

  public List<Integer> getStatistikNationellStatistik() {
    return statistikNationellStatistik;
  }

  public ImmutableList<SrsPrediction> getPredictions() {
    return predictions;
  }

  public void setPredictions(ImmutableList<SrsPrediction> predictions) {
    this.predictions = predictions;
  }

  public List<SrsCertificate> getExtensionChain() {
    return this.extensionChain;
  }

  // Not a setter since we set a copy of the list
  public void replaceExtensionChain(List<SrsCertificate> chain) {
    this.extensionChain = ImmutableList.copyOf(chain);
  }

  public void setAtgarderDiagnosisDescription(String atgarderDiagnosisDescription) {
    this.atgarderDiagnosisDescription = atgarderDiagnosisDescription;
  }

  public void setStatistikDiagnosisDescription(String statistikDiagnosisDescription) {
    this.statistikDiagnosisDescription = statistikDiagnosisDescription;
  }
}
// CHECKSTYLE:ON ParameterNumber
