/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.infra.security.common.service.AuthenticationLogger;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.web.service.mail.MailNotification;

/**
 * Service that writes messages to the monitoring log.
 *
 * @author npet
 */
public interface MonitoringLogService extends AuthenticationLogger {

    void logMailSent(String unitHsaId, String reason, MailNotification mailNotification);

    void logMailMissingAddress(String unitHsaId, String reason, MailNotification mailNotification);

    void logQuestionReceived(String fragestallare, String intygsId, String externReferens, Long internReferens, String enhet, Amne amne,
        List<String> frageIds);

    void logAnswerReceived(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logQuestionSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logAnswerSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne);

    void logIntygRead(String intygsId, String intygsTyp);

    void logIntygRevokeStatusRead(String intygsId, String intygsTyp);

    void logIntygPrintPdf(String intygsId, String intygsTyp, boolean isEmployerCopy);

    void logIntygSigned(String intygsId, String intygsTyp, String userHsaId, String authScheme, RelationKod relationCode);

    void logIntygRegistered(String intygsId, String intygsTyp);

    void logIntygSent(String intygsId, String intygsTyp, String recipient);

    void logIntygRevoked(String intygsId, String intygsTyp, String hsaId, String reason);

    void logIntygCopied(String copyIntygsId, String originalIntygId);

    void logIntygCopiedRenewal(String copyIntygsId, String originalIntygId);

    void logIntygCopiedReplacement(String copyIntygsId, String originalIntygId);

    void logIntygCopiedCompletion(String copyIntygsId, String originalIntygId);

    void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId, String userHsaId, int nrPrefillElements);

    void logUtkastCreatedTemplateManual(String intygsId, String intygsTyp, String userHsaId, String unitHsaId,
        String originalIntygsId, String originalIntygsTyp);

    void logUtkastCreatedTemplateAuto(String intygsId, String intygsTyp, String userHsaId, String unitHsaId,
        String originalIntygsId, String originalIntygsTyp);

    void logUtkastEdited(String intygsId, String intygsTyp);

    void logUtkastConcurrentlyEdited(String intygsId, String intygsTyp);

    void logUtkastDeleted(String intygsId, String intygsTyp);

    void logUtkastRevoked(String intygsId, String hsaId, String reason, String revokeMessage);

    void logUtkastRead(String intygsId, String intygsTyp);

    void logUtkastPrint(String intygsId, String intygsTyp);

    void logUtkastSignFailed(String errorMessage, String intygsId);

    void logUtkastLocked(String intygsId, String intygsTyp);

    void logPULookup(Personnummer personNummer, String result);

    void logPrivatePractitionerTermsApproved(String userId, Personnummer personId, Integer avtalVersion);

    void logNotificationSent(String hanType, String unitId, String intygsId);

    // CHECKSTYLE:OFF ParameterNumber
    void logStatusUpdateQueued(String certificateId, String correlationId, String logicalAddress, String certificateType,
        String certificateVersion, String eventName, LocalDateTime eventTime, String currentUser);
    // CHECKSTYLE:ON ParameterNumber

    void logLoginAttemptMissingSubscription(String userId, String authMethod, String organizations);

    void logSubscriptionWarnings(String userId, String authMethod, String organizations);

    void logSubscriptionServiceCallFailure(Collection<String> queryIds, String exceptionMessage);

    void logArendeReceived(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, List<String> frageIds, boolean isAnswer);

    void logArendeCreated(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, boolean isAnswer);

    void logIntegratedOtherUnit(String intygsId, String intygsTyp, String unitId);

    void logIntegratedOtherCaregiver(String intygsId, String intygsTyp, String caregiverId, String unitId);

    void logDiagnoskodverkChanged(String intygsId, String intygsTyp);

    void logBrowserInfo(String browserName, String browserVersion, String osFamily, String osVersion, String width, String height,
        String netIdVersion);

    void logRevokedPrint(String intygsId, String intygsTyp);

    void logUtkastPatientDetailsUpdated(String intygsId, String intygsTyp);

    void logUtkastMarkedAsReadyToSignNotificationSent(String intygsId, String intygsTyp);

    void logIdpConnectivityCheck(String ip, String connectivity);

    // SRS

    void logSrsLoaded(String userClientContext, String intygsId, String caregiverId, String careUnitId, String diagnosisCode);

    void logSrsPanelActivated(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsConsentAnswered(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsQuestionAnswered(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsCalculateClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsHideQuestionsClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsShowQuestionsClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsMeasuresShowMoreClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsMeasuresExpandOneClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsMeasuresLinkClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsStatisticsActivated(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsStatisticsLinkClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logSrsMeasuresDisplayed(String userClientContext, String intygsId, String caregiverId, String careUnitId);

    void logGetSrsForDiagnose(String diagnosisCode);

    // Saml
    void logSamlStatusForFailedLogin(String issuer, String samlStatus);

    /**
     * Log that the test certificate has been erased.
     *
     * @param certificateId Id of the certificate.
     * @param careUnit Care unit from which the certificate was issued from
     * @param createdUser User that issued the certificate
     */
    void logTestCertificateErased(String certificateId, String careUnit, String createdUser);

    /**
     * Log that a message has been imported.
     *
     * @param certificateId Id of the certificate.
     * @param messageId Id of the message.
     * @param caregiverId HSA-Id of the care giver.
     * @param careUnitId HSA-Id of the care unit.
     * @param messageType Type of message.
     */
    void logMessageImported(String certificateId, String messageId, String caregiverId, String careUnitId, String messageType);

    // Signature service
    void logSignResponseReceived(String transactionId);

    void logSignResponseSuccess(String transactionId, String certificateId);

    void logSignResponseInvalid(String transactionId, String intygsId, String s);

    void logSignRequestCreated(String transactionId, String intygsId);

    void logSignServiceErrorReceived(String transactionId, String intygsId, String resultMajor, String resultMinor,
        String resultMessage);

    void logClientError(String errorId, String certificateId, String errorCode, String errorMessage, String stackTrace);
}
