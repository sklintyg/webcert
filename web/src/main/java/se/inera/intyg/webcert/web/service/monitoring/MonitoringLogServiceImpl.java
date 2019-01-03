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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;

import static se.inera.intyg.webcert.persistence.fragasvar.model.Amne.KOMPLETTERING_AV_LAKARINTYG;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Override
    public void logMailSent(String unitHsaId, String reason) {
        logEvent(MonitoringEvent.MAIL_SENT, unitHsaId, reason);
    }

    @Override
    public void logMailMissingAddress(String unitHsaId, String reason) {
        logEvent(MonitoringEvent.MAIL_MISSING_ADDRESS, unitHsaId, reason);
    }

    @Override
    public void logUserLogin(String userHsaId, String authScheme, String origin) {
        logEvent(MonitoringEvent.USER_LOGIN, userHsaId, authScheme, origin);
    }

    @Override
    public void logUserLogout(String userHsaId, String authScheme) {
        logEvent(MonitoringEvent.USER_LOGOUT, userHsaId, authScheme);
    }

    @Override
    public void logUserSessionExpired(String userHsaId, String authScheme) {
        logEvent(MonitoringEvent.USER_SESSION_EXPIRY, userHsaId, authScheme);
    }

    @Override
    public void logMissingMedarbetarUppdrag(String userHsaId) {
        logEvent(MonitoringEvent.USER_MISSING_MIU, userHsaId);
    }

    @Override
    public void logMissingMedarbetarUppdrag(String userHsaId, String enhetsId) {
        logEvent(MonitoringEvent.USER_MISSING_MIU_ON_ENHET, userHsaId, enhetsId);
    }

    @Override
    public void logQuestionReceived(String fragestallare, String intygsId, String externReferens, Long internReferens, String enhet,
            Amne amne,
            List<String> frageIds) {
        if (KOMPLETTERING_AV_LAKARINTYG == amne) {
            logEvent(MonitoringEvent.QUESTION_RECEIVED_COMPLETION, fragestallare, externReferens, internReferens, intygsId, enhet,
                    Joiner.on(",").join(frageIds));
        } else {
            logEvent(MonitoringEvent.QUESTION_RECEIVED, fragestallare, externReferens, internReferens, intygsId, enhet,
                    amne != null ? amne.name() : "NO AMNE");
        }
    }

    @Override
    public void logAnswerReceived(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne) {
        logEvent(MonitoringEvent.ANSWER_RECEIVED, externReferens, internReferens, intygsId, enhet, amne != null ? amne.name() : "NO AMNE");
    }

    @Override
    public void logQuestionSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne) {
        logEvent(MonitoringEvent.QUESTION_SENT, externReferens, internReferens, intygsId, enhet, amne != null ? amne.name() : "NO AMNE");
    }

    @Override
    public void logAnswerSent(String externReferens, Long internReferens, String intygsId, String enhet, Amne amne) {
        logEvent(MonitoringEvent.ANSWER_SENT, externReferens, internReferens, intygsId, enhet, amne != null ? amne.name() : "NO AMNE");
    }

    @Override
    public void logIntygRead(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_READ, intygsId, intygsTyp);
    }

    @Override
    public void logIntygRevokeStatusRead(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_REVOKE_STATUS_READ, intygsId, intygsTyp);
    }

    @Override
    public void logIntygPrintPdf(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_PRINT_PDF, intygsId, intygsTyp);
    }

    @Override
    public void logIntygSigned(String intygsId, String intygsTyp, String userHsaId, String authScheme, RelationKod relationCode) {
        logEvent(MonitoringEvent.INTYG_SIGNED, intygsId, intygsTyp, userHsaId, authScheme,
                relationCode != null ? relationCode.name() : "NO RELATION");
    }

    @Override
    public void logIntygRegistered(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_REGISTERED, intygsId, intygsTyp);
    }

    @Override
    public void logIntygSent(String intygsId, String recipient) {
        logEvent(MonitoringEvent.INTYG_SENT, intygsId, recipient);
    }

    @Override
    public void logIntygRevoked(String intygsId, String userHsaId, String reason) {
        logEvent(MonitoringEvent.INTYG_REVOKED, intygsId, userHsaId, reason);
    }

    @Override
    public void logIntygCopied(String copyIntygsId, String originalIntygId) {
        logEvent(MonitoringEvent.INTYG_COPIED, copyIntygsId, originalIntygId);
    }

    @Override
    public void logIntygCopiedRenewal(String copyIntygsId, String originalIntygId) {
        logEvent(MonitoringEvent.INTYG_COPIED_RENEWAL, copyIntygsId, originalIntygId);
    }

    @Override
    public void logIntygCopiedReplacement(String copyIntygsId, String originalIntygId) {
        logEvent(MonitoringEvent.INTYG_COPIED_REPLACEMENT, copyIntygsId, originalIntygId);
    }

    @Override
    public void logIntygCopiedCompletion(String copyIntygsId, String originalIntygId) {
        logEvent(MonitoringEvent.INTYG_COPIED_COMPLETION, copyIntygsId, originalIntygId);
    }

    @Override
    public void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId, String userHsaId) {
        logEvent(MonitoringEvent.UTKAST_CREATED, intygsId, intygsTyp, userHsaId, unitHsaId);
    }

    @Override
    public void logUtkastEdited(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_EDITED, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastConcurrentlyEdited(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_CONCURRENTLY_EDITED, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastDeleted(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_DELETED, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastRevoked(String intygsId, String hsaId, String reason, String revokeMessage) {
        logEvent(MonitoringEvent.UTKAST_REVOKED, intygsId, hsaId, reason, revokeMessage);
    }

    @Override
    public void logUtkastRead(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_READ, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastPrint(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_PRINT, intygsId, intygsTyp);
    }

    @Override
    public void logPULookup(Personnummer personNummer, String result) {
        logEvent(MonitoringEvent.PU_LOOKUP, Personnummer.getPersonnummerHashSafe(personNummer), result);
    }

    @Override
    public void logPrivatePractitionerTermsApproved(String userId, Personnummer personId, Integer avtalVersion) {
        logEvent(MonitoringEvent.PP_TERMS_ACCEPTED, userId, Personnummer.getPersonnummerHashSafe(personId), avtalVersion);
    }

    @Override
    public void logNotificationSent(String hanType, String unitId, String intygsId) {
        logEvent(MonitoringEvent.NOTIFICATION_SENT, hanType, unitId, intygsId);
    }

    @Override
    public void logArendeReceived(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, List<String> frageIds,
            boolean isAnswer) {
        if (ArendeAmne.KOMPLT == amne) {
            logEvent(MonitoringEvent.MEDICINSKT_ARENDE_RECEIVED, intygsId, intygsTyp, unitHsaId, frageIds);
        } else if (isAnswer) {
            logEvent(MonitoringEvent.ARENDE_RECEIVED_ANSWER, amne != null ? amne.name() : "NO AMNE", intygsId, intygsTyp, unitHsaId);
        } else {
            logEvent(MonitoringEvent.ARENDE_RECEIVED_QUESTION, amne != null ? amne.name() : "NO AMNE", intygsId, intygsTyp, unitHsaId);
        }
    }

    @Override
    public void logArendeCreated(String intygsId, String intygsTyp, String unitHsaId, ArendeAmne amne, boolean isAnswer) {
        if (isAnswer) {
            logEvent(MonitoringEvent.ARENDE_CREATED_ANSWER, amne != null ? amne.name() : "NO AMNE", intygsId, intygsTyp, unitHsaId);
        } else {
            logEvent(MonitoringEvent.ARENDE_CREATED_QUESTION, amne != null ? amne.name() : "NO AMNE", intygsId, intygsTyp, unitHsaId);
        }
    }

    @Override
    public void logIntegratedOtherUnit(String intygsId, String intygsTyp, String unitId) {
        logEvent(MonitoringEvent.LOGIN_OTHER_UNIT, intygsId, intygsTyp, unitId);
    }

    @Override
    public void logIntegratedOtherCaregiver(String intygsId, String intygsTyp, String caregiverId, String unitId) {
        logEvent(MonitoringEvent.LOGIN_OTHER_CAREGIVER, intygsId, intygsTyp, caregiverId, unitId);
    }

    @Override
    public void logDiagnoskodverkChanged(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.DIAGNOSKODVERK_CHANGED, intygsId, intygsTyp);
    }

    @Override
    public void logScreenResolution(String width, String height) {
        logEvent(MonitoringEvent.SCREEN_RESOLUTION, width, height);
    }

    @Override
    public void logRevokedPrint(String intygsId, String intygsType) {
        logEvent(MonitoringEvent.REVOKED_PRINT, intygsId, intygsType);
    }

    @Override
    public void logUtkastPatientDetailsUpdated(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_PATIENT_UPDATED, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastMarkedAsReadyToSignNotificationSent(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_READY_NOTIFICATION_SENT, intygsId, intygsTyp);
    }

    @Override
    public void logSetSrsConsent(Personnummer personnummer, boolean consent) {
        logEvent(MonitoringEvent.SRS_CONSENT_SET, personnummer.getPersonnummerHash(), consent);
    }

    @Override
    public void logListSrsQuestions(String diagnosisCode) {
        logEvent(MonitoringEvent.SRS_QUESTIONS_LISTED, diagnosisCode);
    }

    @Override
    public void logSrsInformationRetreived(String diagnosisCode, String intygId) {
        logEvent(MonitoringEvent.SRS_INFORMATION_RETREIVED, intygId, diagnosisCode);
    }

    @Override
    public void logSrsShown() {
        logEvent(MonitoringEvent.SRS_SHOWN);
    }

    @Override
    public void logSrsAtgardClicked() {
        logEvent(MonitoringEvent.SRS_ATGARD_CLICKED);
    }

    @Override
    public void logSrsStatistikClicked() {
        logEvent(MonitoringEvent.SRS_STATISTIK_CLICKED);
    }

    @Override
    public void logSrsClicked() {
        logEvent(MonitoringEvent.SRS_CLICKED);
    }

    @Override
    public void logGetSrsForDiagnose(String diagnosisCode) {
        logEvent(MonitoringEvent.SRS_GET_SRS_FOR_DIAGNOSIS_CODE, diagnosisCode);
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {
        MAIL_SENT("Mail sent to unit '{}' for {}"),
        MAIL_MISSING_ADDRESS("Mail sent to admin on behalf of unit '{}' for {}"),
        USER_LOGIN("Login user '{}' using scheme '{}' with origin '{}'"),
        USER_LOGOUT("Logout user '{}' using scheme '{}'"),
        USER_SESSION_EXPIRY("Session expired for user '{}' using scheme '{}'"),
        USER_MISSING_MIU("No valid MIU was found for user '{}'"),
        USER_MISSING_MIU_ON_ENHET("No valid MIU was found for user '{}' on unit '{}'"),
        QUESTION_RECEIVED("Received question from '{}' with external reference '{}' and internal reference '{}' regarding intyg '{}' "
                + "to unit '{}' with subject '{}'"),
        QUESTION_RECEIVED_COMPLETION("Received completion question from '{}' with external reference '{}' and internal reference '{}' "
                + "regarding intyg '{}' to unit '{}' with completion for questions '{}'"),
        ANSWER_RECEIVED("Received answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' "
                + "to unit '{}' with subject '{}'"),
        QUESTION_SENT("Sent question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' "
                + "with subject '{}'"),
        ANSWER_SENT("Sent answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' "
                + "with subject '{}'"),
        INTYG_READ("Intyg '{}' of type '{}' was read"),
        INTYG_REVOKE_STATUS_READ("Revoke status of Intyg '{}' of type '{}' was read."),
        INTYG_PRINT_PDF("Intyg '{}' of type '{}' was printed as PDF"),
        INTYG_SIGNED("Intyg '{}' of type '{}' signed by '{}' using scheme '{}' and relation code '{}'"),
        INTYG_REGISTERED("Intyg '{}' of type '{}' registered with Intygstjänsten"),
        INTYG_SENT("Intyg '{}' sent to recipient '{}'"),
        INTYG_REVOKED("Intyg '{}' revoked by '{}' reason '{}'"),
        INTYG_COPIED("Utkast '{}' created as a copy of '{}'"),
        INTYG_COPIED_RENEWAL("Utkast '{}' created as a renewal copy of '{}'"),
        INTYG_COPIED_REPLACEMENT("Utkast '{}' created as a replacement copy of '{}'"),
        INTYG_COPIED_COMPLETION("Utkast '{}' created as a completion copy of '{}'"),
        UTKAST_READ("Utkast '{}' of type '{}' was read"),
        UTKAST_CREATED("Utkast '{}' of type '{}' created by '{}' on unit '{}'"),
        UTKAST_EDITED("Utkast '{}' of type '{}' was edited"),
        UTKAST_PATIENT_UPDATED("Patient details for utkast '{}' of type '{}' updated"),
        UTKAST_CONCURRENTLY_EDITED("Utkast '{}' of type '{}' was concurrently edited by multiple users"),
        UTKAST_DELETED("Utkast '{}' of type '{}' was deleted"),
        UTKAST_REVOKED("Utkast '{}' revoked by '{}' reason '{}' message '{}'"),
        UTKAST_PRINT("Intyg '{}' of type '{}' was printed"),
        UTKAST_READY_NOTIFICATION_SENT("Utkast '{}' of type '{}' was marked as ready and notification was sent"),
        PU_LOOKUP("Lookup performed on '{}' with result '{}'"),
        PP_TERMS_ACCEPTED("User '{}', personId '{}' accepted private practitioner terms of version '{}'"),
        NOTIFICATION_SENT("Sent notification of type '{}' to unit '{}' for '{}'"),
        ARENDE_RECEIVED_ANSWER("Received arende with amne '{}' for '{}' of type '{}' for unit '{}'"),
        ARENDE_RECEIVED_QUESTION("Received arende with amne '{}' for '{}' of type '{}' for unit '{}'"),
        MEDICINSKT_ARENDE_RECEIVED("Received medicinskt arende for '{}' of type '{}' for unit '{}' on questions '{}'"),
        ARENDE_CREATED_QUESTION("Created arende with amne '{}' for '{}' of type '{}' for unit '{}'"),
        ARENDE_CREATED_ANSWER("Created arende with amne '{}' for '{}' of type '{}' for unit '{}'"),
        LOGIN_OTHER_UNIT("Viewed intyg '{}' of type '{}' on other unit '{}'"),
        LOGIN_OTHER_CAREGIVER("Viewed intyg '{}' of type '{}' on other caregiver '{}' unit '{}'"),
        REVOKED_PRINT("Revoked intyg '{}' of type '{}' printed"),
        DIAGNOSKODVERK_CHANGED("Diagnoskodverk changed for utkast '{}' of type '{}'"),
        SCREEN_RESOLUTION("Width '{}', height '{}'"),
        SRS_CONSENT_SET("Consent set for '{}' to '{}'"),
        SRS_QUESTIONS_LISTED("Questions listed for diagnosis code '{}'"),
        SRS_INFORMATION_RETREIVED("SRS information retreived for certifiacte '{}' for diagnosis code '{}'"),
        SRS_SHOWN("SRS shown"),
        SRS_ATGARD_CLICKED("SRS atgard clicked"),
        SRS_STATISTIK_CLICKED("SRS statistik clicked"),
        SRS_CLICKED("SRS clicked"),
        SRS_GET_SRS_FOR_DIAGNOSIS_CODE("SRS information retreived for diagnosis code '{}'");

        private final String msg;

        MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }

}
