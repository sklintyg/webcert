/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.monitoring;

import java.util.List;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.infra.security.common.service.AuthenticationLogger;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;

/**
 * Service that writes messages to the monitoring log.
 *
 * @author npet
 *
 */
public interface MonitoringLogService extends AuthenticationLogger {

    void logMailSent(String unitHsaId, String reason);

    void logMailMissingAddress(String unitHsaId, String reason);

    void logQuestionReceived(String fragestallare, String intygsId, String externReferens, Long internReferens, String enhet, Amne amne,
            List<String> frageIds);

    void logAnswerReceived(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logQuestionSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logAnswerSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logIntygRead(String intygsId, String intygsTyp);

    void logIntygRevokeStatusRead(String intygsId, String intygsTyp);

    void logIntygPrintPdf(String intygsId, String intygsTyp);

    void logIntygSigned(String intygsId, String intygsTyp, String userHsaId, String authScheme, RelationKod relationCode);

    void logIntygRegistered(String intygsId, String intygsTyp);

    void logIntygSent(String intygsId, String recipient);

    void logIntygRevoked(String intygsId, String hsaId, String reason);

    void logIntygCopied(String copyIntygsId, String originalIntygId);

    void logIntygCopiedRenewal(String copyIntygsId, String originalIntygId);

    void logIntygCopiedReplacement(String copyIntygsId, String originalIntygId);

    void logIntygCopiedCompletion(String copyIntygsId, String originalIntygId);

    void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId, String userHsaId);

    void logUtkastEdited(String intygsId, String intygsTyp);

    void logUtkastConcurrentlyEdited(String intygsId, String intygsTyp);

    void logUtkastDeleted(String intygsId, String intygsTyp);

    void logUtkastRevoked(String intygsId, String hsaId, String reason, String revokeMessage);

    void logUtkastRead(String intygsId, String intygsTyp);

    void logUtkastPrint(String intygsId, String intygsTyp);

    void logPULookup(Personnummer personNummer, String result);

    void logPrivatePractitionerTermsApproved(String userId, Personnummer personId, Integer avtalVersion);

    void logNotificationSent(String hanType, String unitId, String intygsId);

    void logArendeReceived(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, List<String> frageIds, boolean isAnswer);

    void logArendeCreated(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, boolean isAnswer);

    void logIntegratedOtherUnit(String intygsId, String intygsTyp, String unitId);

    void logIntegratedOtherCaregiver(String intygsId, String intygsTyp, String caregiverId, String unitId);

    void logDiagnoskodverkChanged(String intygsId, String intygsTyp);

    void logScreenResolution(String width, String height);

    void logRevokedPrint(String intygsId, String intygsTyp);

    void logUtkastPatientDetailsUpdated(String intygsId, String intygsTyp);

    void logUtkastMarkedAsReadyToSignNotificationSent(String intygsId, String intygsTyp);

    void logSetSrsConsent(Personnummer personnummer, boolean consent);

    void logSetSrsRiskOpinion(String intygsId, String vardgivareHsaId, String vardenhetHsaId, String opinion);

    void logListSrsQuestions(String diagnosisCode);

    void logSrsInformationRetreived(String diagnosisCode, String intygId);

    void logSrsShown();

    void logSrsAtgardClicked();

    void logSrsStatistikClicked();

    void logSrsClicked();

    void logGetSrsForDiagnose(String diagnosisCode);
}
