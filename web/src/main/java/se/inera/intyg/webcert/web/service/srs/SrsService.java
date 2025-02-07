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
package se.inera.intyg.webcert.web.service.srs;

import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.infra.integration.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

import java.util.List;

/**
 * Defines a service with support functionality for SRS - Stöd för rätt sjukskrivning.
 */
public interface SrsService {

    //CHECKSTYLE:OFF ParameterNumber

    /**
     * Returns recommended measures (SW: åtgärder), sick leave statistics and calculate risk predictions based
     * on diagnosis, personal data, regional data and answers to questions.
     *
     * Takes input about the patient and answers to prediction questions to predict a personal risk for a long
     * sick leave (i.e. a sick leave longer than 90 days), also returns the general risk of the population for the given diagnosis.
     * Can also respond with recommended measures and sick leave statistics for the given diagnosis.
     *
     * Historic predictions for the user and diagnosis combination will always be included in the response.
     *
     * Performs PDL logging when required
     *
     * @param user The webcert user
     * @param certificateId The id of the certificate (SW: intygId) connected to the prediction
     * @param personalIdentificationNumber A Swedish personal identification number (SW: personnummer) on form YYYYMMDDNNNN identifying
     * the subject of the risk prediction
     * @param diagnosisCode The main diagnosis of the current certificate
     * @param performRiskPrediction true to perform a new risk prediction and add to the response, any historic predictions
     * on the same person and diagnosis are always in the response.
     * @param addMeasures true to include a recommended measurements in the response
     * @param addStatistics true to include sick leave statistics in the response
     * @param answers A list of answers to questions used as input for the risk prediction
     * @param daysIntoSickLeave Number of days into the current sick leave, used as input to to the prediction
     * @return An object containing the requested response.
     * @throws InvalidPersonNummerException If the format of the personal identification number is incorrect
     * @throws IllegalArgumentException If other input parameters are found to be incorrect
     */
    SrsResponse getSrs(WebCertUser user,
        String certificateId,
        String personalIdentificationNumber,
        String diagnosisCode,
        boolean performRiskPrediction,
        boolean addMeasures,
        boolean addStatistics,
        List<SrsQuestionResponse> answers,
        Integer daysIntoSickLeave)
        throws InvalidPersonNummerException, IllegalArgumentException;
    //CHECKSTYLE:ON ParameterNumber

    /**
     * Returns questions to be used as input when performing risk predictions for a given diagnosis.
     *
     * @param diagnosisCode A diagnosis code (ICD-10)
     * @param modelVersion Wanted prediction model version
     * @return A list of questions used for predicting the given diagnosis
     * @throws IllegalArgumentException If the diagnosis code is missing
     */
    List<SrsQuestion> getQuestions(String diagnosisCode, String modelVersion) throws IllegalArgumentException;

    /**
     * Returns the consent status for SRS for a given person at a given care unit.
     * The status can be YES if the person has consented, NO if the person has actively declined the consent
     * or NONE if the person yet haven't answered regarding consent for SRS on the given care unit
     *
     * @param personalIdentityNumber A Swedish personal identification number (SW: personnummer) of the
     * consenting person on form YYYYMMDDNNNN
     * @param careUnitHsaId HSA-Id of the care unit
     * @return The consent status
     * @throws InvalidPersonNummerException If the personal identification number was incorrect
     */
    Samtyckesstatus getConsent(String personalIdentityNumber, String careUnitHsaId) throws InvalidPersonNummerException;

    /**
     * Sets the consent status for SRS for a given person at a given care unit.
     *
     * @param personalIdentificationNumber A Swedish personal identification number (SW: personnummer) of the
     * prediction subject person on form YYYYMMDDNNNN
     * @param careUnitHsaId HSA-Id of the care unit
     * @param consent true if the person consents, false if the person actively declines the consent
     * @return Result of the update operation
     * @throws InvalidPersonNummerException If the personal identification number was incorrect
     */
    ResultCodeEnum setConsent(String personalIdentificationNumber, String careUnitHsaId, boolean consent)
        throws InvalidPersonNummerException;

    /**
     * Sets the practitioners opinion regarding a certain risk prediction.
     *
     * @param personalIdentificationNumber A Swedish personal identification number (SW: personnummer) of the
     * prediction subject person on form YYYYMMDDNNNN
     * @param careGiverHsaId HSA-id of the care giver where the practitioner currently works, we don't store the actual practitioner
     * @param careUnitHsaId HSA-Id of the care unit where the practitioner currently works
     * @param certificateId The id of the active certificate when the opinion was given
     * @param diagnosisCode The diagnosis code for which the risk prediction was performed
     * @param opinion The opinion about the prediction, see EgenBedomningRiskType for valid values
     * @return Result of the update operation
     * @throws IllegalArgumentException If an incorrect opinion value was given
     */
    ResultCodeEnum setOwnOpinion(String personalIdentificationNumber, String careGiverHsaId, String careUnitHsaId,
        String certificateId, String diagnosisCode, String opinion) throws IllegalArgumentException;

    /**
     * Returns a list of all the diagnoses that has SRS support.
     *
     * @param modelVersion Prediction model version
     * @return A list of diagnosis codes
     */
    List<String> getAllDiagnosisCodes(String modelVersion);

    /**
     * Gets non personalized SRS information for a given diagnosis code.
     *
     * @param diagnosisCode the diagnos code (ICD-10)
     * @return SRS information for the given diagnosis
     */
    SrsForDiagnosisResponse getSrsForDiagnosis(String diagnosisCode);

}
