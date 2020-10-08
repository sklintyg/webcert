/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.persistence.fragasvar.model.Amne.KOMPLETTERING_AV_LAKARINTYG;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    public void logMailSent(String unitHsaId, String reason) {
        logEvent(MonitoringEvent.MAIL_SENT, unitHsaId, reason);
    }

    @Override
    public void logMailMissingAddress(String unitHsaId, String reason) {
        logEvent(MonitoringEvent.MAIL_MISSING_ADDRESS, unitHsaId, reason);
    }

    @Override
    public void logUserLogin(String userHsaId, String role, String roleTypeName, String authScheme, String origin) {
        logEvent(MonitoringEvent.USER_LOGIN, userHsaId, role, roleTypeName != null ? roleTypeName : role, authScheme, origin);
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
    public void logIntygPrintPdf(String intygsId, String intygsTyp, boolean isEmployerCopy) {
        logEvent(MonitoringEvent.INTYG_PRINT_PDF, intygsId, intygsTyp, isEmployerCopy ? "MINIMAL" : "FULL");
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
    public void logIntygSent(String intygsId, String intygsTyp, String recipient) {
        logEvent(MonitoringEvent.INTYG_SENT, intygsId, intygsTyp, recipient);
    }

    @Override
    public void logIntygRevoked(String intygsId, String intygsTyp, String userHsaId, String reason) {
        logEvent(MonitoringEvent.INTYG_REVOKED, intygsId, intygsTyp, userHsaId, reason);
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
    public void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId, String userHsaId, int nrPrefillElements) {
        if (nrPrefillElements > 0) {
            logEvent(MonitoringEvent.UTKAST_CREATED_PREFILL, intygsId, intygsTyp, nrPrefillElements, userHsaId, unitHsaId);
        } else {
            logEvent(MonitoringEvent.UTKAST_CREATED, intygsId, intygsTyp, userHsaId, unitHsaId);
        }
    }

    @Override
    public void logUtkastCreatedTemplateManual(String intygsId, String intygsTyp, String userHsaId, String unitHsaId,
        String originalIntygsId, String originalIntygsTyp) {
        logEvent(MonitoringEvent.UTKAST_CREATED_TEMPLATE_MANUAL, intygsId, intygsTyp,
            userHsaId, unitHsaId, originalIntygsId, originalIntygsTyp);
    }

    @Override
    public void logUtkastCreatedTemplateAuto(String intygsId, String intygsTyp, String userHsaId, String unitHsaId,
        String originalIntygsId, String originalIntygsTyp) {
        logEvent(MonitoringEvent.UTKAST_CREATED_TEMPLATE_AUTO, intygsId, intygsTyp,
            userHsaId, unitHsaId, originalIntygsId, originalIntygsTyp);
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
    public void logUtkastSignFailed(String errorMessage, String intygsId) {
        logEvent(MonitoringEvent.UTKAST_SIGN_FAILED, intygsId, errorMessage);
    }

    @Override
    public void logUtkastLocked(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_LOCKED, intygsId, intygsTyp);
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
    public void logBrowserInfo(String browserName, String browserVersion, String osFamily, String osVersion, String width, String height,
        String netIdVersion) {
        logEvent(MonitoringEvent.BROWSER_INFO, browserName, browserVersion, osFamily, osVersion, width, height, netIdVersion);
    }

    @Override
    public void logIdpConnectivityCheck(String ip, String connectivity) {

        StringBuilder connectivityResult = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            IdpConnectivity[] conn = objectMapper.readValue(connectivity, IdpConnectivity[].class);
            for (IdpConnectivity connection : conn) {
                connectivityResult.append(connection.url).append(connection.connected ? " OK" : " Not OK").append("! ");
            }
        } catch (Exception e) {
            //Exceptions to be ignored
        }

        WebCertUser user = webCertUserService.getUser();

        logEvent(MonitoringEvent.IDP_CONNECTIVITY_CHECK,
            ip,
            user.getValdVardgivare() != null ? user.getValdVardgivare().getId() : "null",
            user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "null",
            connectivityResult.toString());
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
    public void logGetSrsForDiagnose(String diagnosisCode) {
        logEvent(MonitoringEvent.SRS_GET_SRS_FOR_DIAGNOSIS_CODE, diagnosisCode);
    }


    @Override
    public void logSrsLoaded(String userClientContext, String intygsId, String caregiverId, String careUnitId, String diagnosisCode) {
        logEvent(MonitoringEvent.SRS_LOADED, userClientContext, intygsId, diagnosisCode, caregiverId, careUnitId);
    }

    @Override
    public void logSrsPanelActivated(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_PANEL_ACTIVATED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsConsentAnswered(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_CONSENT_ANSWERED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsQuestionAnswered(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_QUESTION_ANSWERED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsCalculateClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_CALCULATE_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsHideQuestionsClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_HIDE_QUESTIONS_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsShowQuestionsClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_SHOW_QUESTIONS_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsMeasuresShowMoreClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_MEASURES_SHOW_MORE_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsMeasuresExpandOneClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_MEASURES_EXPAND_ONE_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsMeasuresLinkClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_MEASURES_LINK_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsStatisticsActivated(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_STATISTICS_ACTIVATED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsStatisticsLinkClicked(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_STATISTICS_LINK_CLICKED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSrsMeasuresDisplayed(String userClientContext, String intygsId, String caregiverId, String careUnitId) {
        logEvent(MonitoringEvent.SRS_MEASURES_DISPLAYED, userClientContext, intygsId, caregiverId, careUnitId);
    }

    @Override
    public void logSamlStatusForFailedLogin(String issuer, String samlStatus) {
        logEvent(MonitoringEvent.SAML_STATUS_LOGIN_FAIL, issuer, samlStatus);
    }

    @Override
    public void logTestCertificateErased(String certificateId, String careUnit, String createdUser) {
        logEvent(MonitoringEvent.TEST_CERTIFICATE_ERASED, certificateId, careUnit, createdUser);
    }

    @Override
    public void logMessageImported(String certificateId, String messageId, String caregiverId, String careUnitId, String messageType) {
        logEvent(MonitoringEvent.MESSAGE_IMPORTED, messageId, messageType, certificateId, caregiverId, careUnitId);
    }

    @Override
    public void logSignResponseReceived(String transactionId) {
        logEvent(MonitoringEvent.DSS_SIGNATURE_RESPONSE_RECEIVED, transactionId);
    }

    @Override
    public void logSignResponseInvalid(String transactionId, String intygsId, String s) {
        logEvent(MonitoringEvent.DSS_SIGNATURE_RESPONSE_INVALID, transactionId, intygsId, s);
    }

    @Override
    public void logSignRequestCreated(String transactionId, String intygsId) {
        logEvent(MonitoringEvent.DSS_SIGNATURE_REQUEST_CREATED, intygsId, transactionId);
    }

    @Override
    public void logSignServiceErrorReceived(String transactionId, String intygsId, String resultMajor, String resultMinor,
        String resultMessage) {
        logEvent(MonitoringEvent.DSS_SIGNATURE_RESPONSE_ERROR_RECEIVED, transactionId, intygsId, resultMajor, resultMinor, resultMessage);
    }

    @Override
    public void logSuccessfulCertificateEventLoaderBatch(List<String> certificateIdList, Integer batchSize) {
//TODO
    }

    @Override
    public void logFailedCertificateEventLoaderBatch(List<String> failedCertificates, Integer batchSize) {
//TODO
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {
        MAIL_SENT("Mail sent to unit '{}' for {}"),
        MAIL_MISSING_ADDRESS("Mail sent to admin on behalf of unit '{}' for {}"),
        USER_LOGIN("Login user '{}' as role '{}' roleTypeName '{}' using scheme '{}' with origin '{}'"),
        USER_LOGOUT("Logout user '{}' using scheme '{}'"),
        USER_SESSION_EXPIRY("Session expired for user '{}' using scheme '{}'"),
        USER_MISSING_MIU("No valid MIU was found for user '{}'"),
        USER_MISSING_MIU_ON_ENHET("No valid MIU was found for user '{}' on unit '{}'"),
        QUESTION_RECEIVED("Received question from '{}' with external reference '{}' and internal reference '{}' regarding intyg '{}' "
            + "to unit '{}' with subject '{}'"),
        QUESTION_RECEIVED_COMPLETION(
            "Received completion question from '{}' with external reference '{}' and internal reference '{}' "
                + "regarding intyg '{}' to unit '{}' with completion for questions '{}'"),
        ANSWER_RECEIVED("Received answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' "
            + "to unit '{}' with subject '{}'"),
        QUESTION_SENT("Sent question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' "
            + "with subject '{}'"),
        ANSWER_SENT(
            "Sent answer to question with external reference '{}' and internal reference '{}' regarding intyg '{}' to unit '{}' "
                + "with subject '{}'"),
        INTYG_READ("Intyg '{}' of type '{}' was read"),
        INTYG_REVOKE_STATUS_READ("Revoke status of Intyg '{}' of type '{}' was read."),
        INTYG_PRINT_PDF("Intyg '{}' of type '{}' was printed as PDF with '{}' content"),
        INTYG_SIGNED("Intyg '{}' of type '{}' signed by '{}' using scheme '{}' and relation code '{}'"),
        INTYG_REGISTERED("Intyg '{}' of type '{}' registered with Intygstjänsten"),
        INTYG_SENT("Intyg '{}' of type '{}' sent to recipient '{}'"),
        INTYG_REVOKED("Intyg '{}' of type '{}' revoked by '{}' reason '{}'"),
        INTYG_COPIED("Utkast '{}' created as a copy of '{}'"),
        INTYG_COPIED_RENEWAL("Utkast '{}' created as a renewal copy of '{}'"),
        INTYG_COPIED_REPLACEMENT("Utkast '{}' created as a replacement copy of '{}'"),
        INTYG_COPIED_COMPLETION("Utkast '{}' created as a completion copy of '{}'"),
        UTKAST_READ("Utkast '{}' of type '{}' was read"),
        UTKAST_CREATED("Utkast '{}' of type '{}' created by '{}' on unit '{}'"),
        UTKAST_CREATED_TEMPLATE_MANUAL(
            "Utkast '{}' of type '{}' was manually created by '{}' on unit '{}' from signed template '{}' of type '{}'"),
        UTKAST_CREATED_TEMPLATE_AUTO(
            "Utkast '{}' of type '{}' automatically created by '{}' on unit '{}' from signed template '{}' of type '{}'"),
        UTKAST_CREATED_PREFILL("Utkast '{}' of type '{}' created with '{}' forifyllnad svar by '{}' on unit '{}'"),
        UTKAST_EDITED("Utkast '{}' of type '{}' was edited"),
        UTKAST_PATIENT_UPDATED("Patient details for utkast '{}' of type '{}' updated"),
        UTKAST_CONCURRENTLY_EDITED("Utkast '{}' of type '{}' was concurrently edited by multiple users"),
        UTKAST_DELETED("Utkast '{}' of type '{}' was deleted"),
        UTKAST_REVOKED("Utkast '{}' revoked by '{}' reason '{}' message '{}'"),
        UTKAST_PRINT("Intyg '{}' of type '{}' was printed"),
        UTKAST_READY_NOTIFICATION_SENT("Utkast '{}' of type '{}' was marked as ready and notification was sent"),
        UTKAST_SIGN_FAILED("Utkast '{}' failed signing process with message '{}'"),
        UTKAST_LOCKED("Utkast '{}' of type '{}' was locked"),
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
        BROWSER_INFO("Name '{}' Version '{}' OSFamily '{}' OSVersion '{}' Width '{}' Height '{}' NetIdVersion '{}'"),

        SRS_LOADED("SRS loaded in client context '{}' for intyg '{}' and diagnosis code '{}' with caregiver '{}' and care unit '{}'"),
        SRS_PANEL_ACTIVATED("SRS panel activated in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_CONSENT_ANSWERED("SRS consent answered in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_QUESTION_ANSWERED("SRS question answered in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_CALCULATE_CLICKED(
            "SRS calculate prediction clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_HIDE_QUESTIONS_CLICKED(
            "SRS hide questions clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_SHOW_QUESTIONS_CLICKED(
            "SRS show questions clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_MEASURES_SHOW_MORE_CLICKED(
            "SRS show more measures clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_MEASURES_EXPAND_ONE_CLICKED(
            "SRS expand one measure text clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_MEASURES_LINK_CLICKED(
            "SRS measures link clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_STATISTICS_ACTIVATED(
            "SRS statistics tab activated in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_STATISTICS_LINK_CLICKED(
            "SRS statistics link clicked in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),
        SRS_MEASURES_DISPLAYED("SRS measures displayed in client context '{}' for intyg '{}' with caregiver '{}' and care unit '{}'"),

        SRS_GET_SRS_FOR_DIAGNOSIS_CODE("SRS information retreived for diagnosis code '{}'"),

        IDP_CONNECTIVITY_CHECK("IDP Connectivity for ip '{}' with care giver '{}' and care unit '{}': {}"),

        SAML_STATUS_LOGIN_FAIL("Login failed at IDP '{}' with status message '{}'"),

        TEST_CERTIFICATE_ERASED("Test certificate '{}' on care unit '{}' created by '{}' was erased"),

        MESSAGE_IMPORTED("Message '{}' with type '{}' for certificate '{}' on caregiver '{}' and care unit '{}' was imported"),

        DSS_SIGNATURE_RESPONSE_RECEIVED("Received sign response from sign service with transactionID '{}'"),

        DSS_SIGNATURE_RESPONSE_INVALID("Failed to read or validate sign response with transactionID '{}' for certificate '{}': {}"),

        DSS_SIGNATURE_REQUEST_CREATED("Sign request for certificate '{}' created with transactionID '{}'"),

        DSS_SIGNATURE_RESPONSE_ERROR_RECEIVED(
            "Received error from sign service for request with transactionID '{}' for certificate '{}' with error message: {} - {} - {}");


        private final String msg;

        MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }

}
