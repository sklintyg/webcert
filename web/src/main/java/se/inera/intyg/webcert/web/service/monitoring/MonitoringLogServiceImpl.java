/**
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of statistik (https://github.com/sklintyg/statistik).
 *
 * statistik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * statistik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.util.logging.HashUtility;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

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
    public void logUserLogin(String userHsaId, String authScheme) {
        logEvent(MonitoringEvent.USER_LOGIN, userHsaId, authScheme);
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
    public void logQuestionReceived(String fragestallare, String intygsId, String externReferens, Long internReferens, String enhet, String amne) {
        logEvent(MonitoringEvent.QUESTION_RECEIVED, fragestallare, externReferens, internReferens, intygsId, enhet, amne);
    }

    @Override
    public void logAnswerReceived(String externReferens, Long internReferens, String intygsId, String enhet, String amne) {
        logEvent(MonitoringEvent.ANSWER_RECEIVED, externReferens, internReferens, intygsId, enhet, amne);
    }

    @Override
    public void logQuestionSent(String externReferens, Long internReferens, String intygsId, String enhet, String amne) {
        logEvent(MonitoringEvent.QUESTION_SENT, externReferens, internReferens, intygsId, enhet, amne);
    }

    @Override
    public void logAnswerSent(String externReferens, Long internReferens, String intygsId, String enhet, String amne) {
        logEvent(MonitoringEvent.ANSWER_SENT, externReferens, internReferens, intygsId, enhet, amne);
    }

    @Override
    public void logIntygRead(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_READ, intygsId, intygsTyp);
    }

    @Override
    public void logIntygPrintPdf(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.INTYG_PRINT_PDF, intygsId, intygsTyp);
    }

    @Override
    public void logIntygSigned(String intygsId, String userHsaId, String authScheme) {
        logEvent(MonitoringEvent.INTYG_SIGNED, intygsId, userHsaId, authScheme);
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
    public void logIntygRevoked(String intygsId, String userHsaId) {
        logEvent(MonitoringEvent.INTYG_REVOKED, intygsId, userHsaId);
    }

    @Override
    public void logIntygCopied(String copyIntygsId, String originalIntygId) {
        logEvent(MonitoringEvent.INTYG_COPIED, copyIntygsId, originalIntygId);
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
    public void logUtkastRead(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_READ, intygsId, intygsTyp);
    }

    @Override
    public void logUtkastPrint(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_PRINT, intygsId, intygsTyp);
    }

    @Override
    public void logPULookup(Personnummer personNummer, String result) {
        logEvent(MonitoringEvent.PU_LOOKUP, Personnummer.getPnrHashSafe(personNummer), result);
    }

    @Override
    public void logPrivatePractitionerTermsApproved(String userId, String personId, Integer avtalVersion) {
        logEvent(MonitoringEvent.PP_TERMS_ACCEPTED, userId, HashUtility.hash(personId), avtalVersion);
    }

    @Override
    public void logNotificationSent(String hanType, String unitId) {
        logEvent(MonitoringEvent.NOTIFICATION_SENT, hanType, unitId);
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {
        MAIL_SENT("Mail sent to unit '{}' for {}"),
        MAIL_MISSING_ADDRESS("Mail sent to admin on behalf of unit '{}' for {}"),
        USER_LOGIN("Login user '{}' using scheme '{}'"),
        USER_LOGOUT("Logout user '{}' using scheme '{}'"),
        USER_SESSION_EXPIRY("Session expired for user '{}' using scheme '{}'"),
        USER_MISSING_MIU("No valid MIU was found for user '{}'"),
        USER_MISSING_MIU_ON_ENHET("No valid MIU was found for user '{}' on unit '{}'"),
        QUESTION_RECEIVED("Received question from '{}' with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' with subject '{}'"),
        ANSWER_RECEIVED("Received answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' with subject '{}'"),
        QUESTION_SENT("Sent question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' with subject '{}'"),
        ANSWER_SENT("Sent answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' with subject '{}'"),
        INTYG_READ("Intyg '{}' of type '{}' was read"),
        INTYG_PRINT_PDF("Intyg '{}' of type '{}' was printed as PDF"),
        INTYG_SIGNED("Intyg '{}' signed by '{}' using scheme '{}'"),
        INTYG_REGISTERED("Intyg '{}' of type '{}' registered with Intygstjänsten"),
        INTYG_SENT("Intyg '{}' sent to recipient '{}'"),
        INTYG_REVOKED("Intyg '{}' revoked by '{}'"),
        INTYG_COPIED("Utkast '{}' created as a copy of '{}'"),
        UTKAST_READ("Utkast '{}' of type '{}' was read"),
        UTKAST_CREATED("Utkast '{}' of type '{}' created by '{}' on unit '{}'"),
        UTKAST_EDITED("Utkast '{}' of type '{}' was edited"),
        UTKAST_CONCURRENTLY_EDITED("Utkast '{}' of type '{}' was concurrently edited by multiple users"),
        UTKAST_DELETED("Utkast '{}' of type '{}' was deleted"),
        UTKAST_PRINT("Intyg '{}' of type '{}' was printed"),
        PU_LOOKUP("Lookup performed on '{}' with result '{}'"),
        PP_TERMS_ACCEPTED("User '{}', personId '{}' accepted private practitioner terms of version '{}'"),
        NOTIFICATION_SENT("Sent notification of type '{}' to unit '{}'");

        private final String msg;

        MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }
}
