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
import java.time.LocalDateTime;
import java.util.List;

public class SrsPrediction {

  private String certificateId;
  private String diagnosisCode;
  private String diagnosisDescription;
  private String statusCode;
  private Integer level;
  private String description;
  private Double probabilityOverLimit;
  private Double prevalence;
  private ImmutableList<SrsQuestionResponse> questionsResponses;
  private String physiciansOwnOpinionRisk;
  private Integer daysIntoSickLeave;
  private String modelVersion;

  /** When this prediction was calculated */
  private LocalDateTime timestamp;

  public SrsPrediction() {}

  public SrsPrediction(
      String certificateId,
      String predictionDiagnosisCode,
      String predictionDiagnosisDescription,
      String predictionStatusCode,
      Integer predictionLevel,
      String predictionDescription,
      Double predictionProbabilityOverLimit,
      Double predictionPrevalence,
      List<SrsQuestionResponse> predictionQuestionsResponses,
      String predictionPhysiciansOwnOpinionRisk,
      LocalDateTime predictionTimestamp,
      Integer daysIntoSickLeave,
      String modelVersion) {
    this.certificateId = certificateId;
    this.diagnosisCode = predictionDiagnosisCode;
    this.diagnosisDescription = predictionDiagnosisDescription;
    this.statusCode = predictionStatusCode;
    this.level = predictionLevel;
    this.description = predictionDescription;
    this.probabilityOverLimit = predictionProbabilityOverLimit;
    this.prevalence = predictionPrevalence;
    this.questionsResponses =
        predictionQuestionsResponses != null
            ? ImmutableList.copyOf(predictionQuestionsResponses)
            : null;
    this.physiciansOwnOpinionRisk = predictionPhysiciansOwnOpinionRisk;
    this.timestamp = predictionTimestamp;
    this.daysIntoSickLeave = daysIntoSickLeave;
    this.modelVersion = modelVersion;
  }

  public String getModelVersion() {
    return modelVersion;
  }

  public void setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
  }

  public Integer getDaysIntoSickLeave() {
    return daysIntoSickLeave;
  }

  public void setDaysIntoSickLeave(Integer daysIntoSickLeave) {
    this.daysIntoSickLeave = daysIntoSickLeave;
  }

  public String getCertificateId() {
    return certificateId;
  }

  public void setCertificateId(String certificateId) {
    this.certificateId = certificateId;
  }

  public String getDiagnosisDescription() {
    return diagnosisDescription;
  }

  public void setDiagnosisDescription(String diagnosisDescription) {
    this.diagnosisDescription = diagnosisDescription;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Double getProbabilityOverLimit() {
    return probabilityOverLimit;
  }

  public void setProbabilityOverLimit(Double probabilityOverLimit) {
    this.probabilityOverLimit = probabilityOverLimit;
  }

  public Double getPrevalence() {
    return prevalence;
  }

  public void setPrevalence(Double prevalence) {
    this.prevalence = prevalence;
  }

  public ImmutableList<SrsQuestionResponse> getQuestionsResponses() {
    return questionsResponses;
  }

  public void setQuestionsResponses(ImmutableList<SrsQuestionResponse> questionsResponses) {
    this.questionsResponses = questionsResponses;
  }

  public String getPhysiciansOwnOpinionRisk() {
    return physiciansOwnOpinionRisk;
  }

  public void setPhysiciansOwnOpinionRisk(String physiciansOwnOpinionRisk) {
    this.physiciansOwnOpinionRisk = physiciansOwnOpinionRisk;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getDiagnosisCode() {
    return diagnosisCode;
  }

  public void setDiagnosisCode(String diagnosisCode) {
    this.diagnosisCode = diagnosisCode;
  }
}
