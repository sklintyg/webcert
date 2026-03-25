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
package se.inera.intyg.webcert.infra.srs.services;

import java.util.List;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.srs.model.SrsCertificate;
import se.inera.intyg.webcert.infra.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestion;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestionResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsResponse;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

public interface SrsInfraService {

  /**
   * Perform a getSrsInformation for a given Personnummer and diagnosis.
   *
   * @param user user which made the request
   * @param personnummer {@link Personnummer} for the patient concerned.
   * @param certDiags List of certificateId and diagnosis code combinations (the signedDate
   *     attribute is not used/can be null). The first represents the current certificate and the
   *     current main diagnosis. If prediktion=true in utdatafilter this first entry is the one that
   *     will be used for calculating a new prediction. If prediktion=false in utdatafilter we will
   *     try to find an earlier prediction on this first certificate and diagnosis instead. The
   *     second entry is a cert with relation FRLNG from the first and the diagnosis to find earlier
   *     predictions for, if such certificate-diagnosis combination exists, The third entry is a
   *     cert with relation FRLNG from the second and the diagnosis to find earlier predictions for,
   *     if such certificate-diagnosis combination exists
   *     <p>I.e. The first certificate entry is an extension of the second which is an extension of
   *     the third.
   * @param filter Utdatafilter with desired response filters.
   * @param answers Answers from the user.
   * @param daysIntoSickLeave Number of days into the sick leave, used as input for the prediction.
   *     If null is given, it defaults to 15.
   * @return {@link SrsResponse} with {@link Diagnosprediktionstatus} OK or PREDIKTIONSMODELL_SAKNAS
   */
  SrsResponse getSrs(
      IntygUser user,
      Personnummer personnummer,
      List<SrsCertificate> certDiags,
      Utdatafilter filter,
      List<SrsQuestionResponse> answers,
      Integer daysIntoSickLeave)
      throws InvalidPersonNummerException;

  /**
   * Retreives the questions to be displayed in the GUI.
   *
   * @param diagnos the diagnosCode.
   * @param modelVersion Prediction model version
   * @return a sorted list of questions to be displayed
   */
  List<SrsQuestion> getQuestions(String diagnos, String modelVersion);

  Samtyckesstatus getConsent(String careUnitHsaId, Personnummer personId)
      throws InvalidPersonNummerException;

  ResultCodeEnum setConsent(String careUnitHsaId, Personnummer personId, boolean samtycke)
      throws InvalidPersonNummerException;

  /**
   * Sets the user's own opinion on the risk prediction.
   *
   * @param careGiverHsaId the HSA-id of the user's care giver
   * @param careUnitHsaId the HSA-id of the certificate's/user's care unit
   * @param certificateId the id of the certificate for which the risk was predicted
   * @param diagnosisCode the diagnosis code for which the risk was predicted
   * @param ownOpinion the users own opinion
   */
  ResultCodeEnum setOwnOpinion(
      String careGiverHsaId,
      String careUnitHsaId,
      String certificateId,
      String diagnosisCode,
      EgenBedomningRiskType ownOpinion);

  /**
   * Fetches all the diagnosis codes which are supported by SRS.
   *
   * @param modelVersion Prediction model version
   * @return a list containing all the supported diagnosis codes. All sub-diagnosis are also
   *     supported.
   */
  List<String> getAllDiagnosisCodes(String modelVersion);

  /**
   * Fetches all non-predictive parts of SRS info based on the supplied diagnose code.
   *
   * @param diagnosisCode string representation of the diagnosis code.
   * @return {@link SrsForDiagnosisResponse} with static srs info related to the supplied diagnosis
   *     code
   */
  SrsForDiagnosisResponse getSrsForDiagnose(String diagnosisCode);
}
