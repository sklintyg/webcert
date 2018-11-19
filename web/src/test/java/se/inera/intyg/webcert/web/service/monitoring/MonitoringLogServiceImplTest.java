/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {

    private static final String INTYGS_ID = "INTYGS_ID";
    private static final String ENHET = "ENHET";
    private static final String VARDGIVARE = "VARDGIVARE";
    private static final String COPY_INTYGS_ID = "COPY_INTYGS_ID";
    private static final String ORGINAL_INTYG_ID = "ORGINAL_INTYG_ID";
    private static final String INTYGS_TYP = "INTYGS_TYP";
    private static final String HSA_ID = "HSA_ID";
    private static final String RECIPIENT = "RECIPIENT";
    private static final String AUTH_SCHEME = "AUTH_SCHEME";
    private static final String REASON = "REASON";
    private static final String REVOKE_MESSAGE = "REVOKE_MESSAGE";
    private static final Integer AVTAL_VERSION = 98;
    private static final String HAN_TYPE = "HAN_TYPE";
    private static final String RESULT = "RESULT";
    private static final String FRAGESTALLARE = "FRAGESTALLARE";
    private static final String EXTERN_REFERENS = "EXTERN_REFERENS";
    private static final long INTERN_REFERENS = 97;
    private static final Amne AMNE = Amne.ARBETSTIDSFORLAGGNING;
    private static final String PERSON_ID = "19121212-1212";

    private static final Personnummer PERSON_NUMMER = Personnummer.createPersonnummer(PERSON_ID).get();

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    private MonitoringLogService logService = new MonitoringLogServiceImpl();

    @Before
    public void setup() {

        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @After
    public void teardown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    public void shouldLogAnswerReceived() {
        logService.logAnswerReceived(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
                "ANSWER_RECEIVED Received answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    public void shouldLogAnswerReceivedWhenAllParamtersNull() {
        logService.logAnswerReceived(null, null, null, null, null);
        verifyLog(Level.INFO,
                "ANSWER_RECEIVED Received answer to question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    private void verifyLog(Level logLevel, String logMessage) {
        // Verify and capture logging interaction
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        // Verify log
        assertThat(loggingEvent.getLevel(), equalTo(logLevel));
        assertThat(loggingEvent.getFormattedMessage(),
                equalTo(logMessage));
    }

    @Test
    public void shouldLogAnswerSent() {
        logService.logAnswerSent(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
                "ANSWER_SENT Sent answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    public void shouldLogAnswerSentWithAllParametersNull() {
        logService.logAnswerSent(null, null, null, null, null);
        verifyLog(Level.INFO,
                "ANSWER_SENT Sent answer to question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    public void shouldLogIntygCopied() {
        logService.logIntygCopied(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED Utkast 'COPY_INTYGS_ID' created as a copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    public void shouldLogIntygCopiedRenewal() {
        logService.logIntygCopiedRenewal(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_RENEWAL Utkast 'COPY_INTYGS_ID' created as a renewal copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    public void shouldLogIntygCopiedReplacement() {
        logService.logIntygCopiedReplacement(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_REPLACEMENT Utkast 'COPY_INTYGS_ID' created as a replacement copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    public void shouldLogIntygCopiedCompletion() {
        logService.logIntygCopiedCompletion(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_COMPLETION Utkast 'COPY_INTYGS_ID' created as a completion copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    public void shouldLogIntygPrintPdf() {
        logService.logIntygPrintPdf(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "INTYG_PRINT_PDF Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was printed as PDF");
    }

    @Test
    public void shouldLogIntygRead() {
        logService.logIntygRead(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "INTYG_READ Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was read");
    }

    @Test
    public void shouldLogIntygRegistered() {
        logService.logIntygRegistered(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "INTYG_REGISTERED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' registered with Intygstjänsten");
    }

    @Test
    public void shouldLogIntygRevoked() {
        logService.logIntygRevoked(INTYGS_ID, HSA_ID, REASON);
        verifyLog(Level.INFO, "INTYG_REVOKED Intyg 'INTYGS_ID' revoked by 'HSA_ID' reason 'REASON'");
    }

    @Test
    public void shouldLogIntygSent() {
        logService.logIntygSent(INTYGS_ID, RECIPIENT);
        verifyLog(Level.INFO, "INTYG_SENT Intyg 'INTYGS_ID' sent to recipient 'RECIPIENT'");
    }

    @Test
    public void shouldLogIntygSigned() {
        logService.logIntygSigned(INTYGS_ID, INTYGS_TYP, HSA_ID, AUTH_SCHEME, null);
        verifyLog(Level.INFO,
                "INTYG_SIGNED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' signed by 'HSA_ID' using scheme 'AUTH_SCHEME' and relation code 'NO RELATION'");
    }

    @Test
    public void shouldLogIntygSignedWithRelation() {
        logService.logIntygSigned(INTYGS_ID, INTYGS_TYP, HSA_ID, AUTH_SCHEME, RelationKod.KOMPLT);
        verifyLog(Level.INFO,
                "INTYG_SIGNED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' signed by 'HSA_ID' using scheme 'AUTH_SCHEME' and relation code 'KOMPLT'");
    }

    @Test
    public void shouldLogMailMissingAddress() {
        logService.logMailMissingAddress(HSA_ID, REASON);
        verifyLog(Level.INFO, "MAIL_MISSING_ADDRESS Mail sent to admin on behalf of unit 'HSA_ID' for REASON");
    }

    @Test
    public void shouldLogMailSent() {
        logService.logMailSent(HSA_ID, REASON);
        verifyLog(Level.INFO, "MAIL_SENT Mail sent to unit 'HSA_ID' for REASON");
    }

    @Test
    public void shouldLogMissingMedarbetarUppdrag() {
        logService.logMissingMedarbetarUppdrag(HSA_ID);
        verifyLog(Level.INFO, "USER_MISSING_MIU No valid MIU was found for user 'HSA_ID'");
    }

    @Test
    public void shouldLogMissingMedarbetarUppdragWithEnhet() {
        logService.logMissingMedarbetarUppdrag(HSA_ID, ENHET);
        verifyLog(Level.INFO, "USER_MISSING_MIU_ON_ENHET No valid MIU was found for user 'HSA_ID' on unit 'ENHET'");
    }

    @Test
    public void shouldLogNotificationSent() {
        logService.logNotificationSent(HAN_TYPE, ENHET, INTYGS_ID);
        verifyLog(Level.INFO, "NOTIFICATION_SENT Sent notification of type 'HAN_TYPE' to unit 'ENHET' for 'INTYGS_ID'");
    }

    @Test
    public void shouldLogArendeReceived() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KONTKT, null, false);
        verifyLog(Level.INFO,
                "ARENDE_RECEIVED_QUESTION Received arende with amne 'KONTKT' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    public void shouldLogArendeReceivedWithAllParametersNull() {
        logService.logArendeReceived(null, null, null, null, null, false);
        verifyLog(Level.INFO, "ARENDE_RECEIVED_QUESTION Received arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    public void shouldLogArendeReceivedCompletion() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KOMPLT, Arrays.asList("1", "2"), false);
        verifyLog(Level.INFO,
                "MEDICINSKT_ARENDE_RECEIVED Received medicinskt arende for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET' on questions '[1, 2]'");
    }

    @Test
    public void shouldLogArendeReceivedAnswer() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KONTKT, null, true);
        verifyLog(Level.INFO,
                "ARENDE_RECEIVED_ANSWER Received arende with amne 'KONTKT' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    public void shouldLogArendeReceivedAnswerAmneMissing() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, null, null, true);
        verifyLog(Level.INFO,
                "ARENDE_RECEIVED_ANSWER Received arende with amne 'NO AMNE' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    public void shouldLogArendeCreated() {
        logService.logArendeCreated(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.AVSTMN, false);
        verifyLog(Level.INFO,
                "ARENDE_CREATED_QUESTION Created arende with amne 'AVSTMN' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    public void shouldLogArendeCreatedWithAllParametersNull() {
        logService.logArendeCreated(null, null, null, null, false);
        verifyLog(Level.INFO, "ARENDE_CREATED_QUESTION Created arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    public void shouldLogArendeCreatedAnswer() {
        logService.logArendeCreated(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.AVSTMN, true);
        verifyLog(Level.INFO,
                "ARENDE_CREATED_ANSWER Created arende with amne 'AVSTMN' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    public void shouldLogArendeCreatedAnswerWithAllParametersNull() {
        logService.logArendeCreated(null, null, null, null, true);
        verifyLog(Level.INFO, "ARENDE_CREATED_ANSWER Created arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    public void shouldLogPrivatePractitionerTermsApproved() {
        logService.logPrivatePractitionerTermsApproved(HSA_ID, PERSON_NUMMER, AVTAL_VERSION);
        verifyLog(Level.INFO,
                "PP_TERMS_ACCEPTED User 'HSA_ID', personId '9a8b138a666f84da32e9383b49a15f46f6e08d2c492352aa0dfcc3f993773b0d' accepted private practitioner terms of version '98'");
    }

    @Test
    public void shouldLogPULookup() {
        logService.logPULookup(PERSON_NUMMER, RESULT);
        verifyLog(Level.INFO,
                "PU_LOOKUP Lookup performed on '9a8b138a666f84da32e9383b49a15f46f6e08d2c492352aa0dfcc3f993773b0d' with result 'RESULT'");
    }

    @Test
    public void shouldLogQuestionReceived() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE, null);
        verifyLog(Level.INFO,
                "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    public void shouldLogQuestionReceivedWhenAllParametersNull() {
        logService.logQuestionReceived(null, null, null, null, null, null, null);
        verifyLog(Level.INFO,
                "QUESTION_RECEIVED Received question from 'null' with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    public void shouldLogQuestionReceivedCompletion() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, Amne.KOMPLETTERING_AV_LAKARINTYG,
                Arrays.asList("KOMP1", "KOMP2"));
        verifyLog(Level.INFO,
                "QUESTION_RECEIVED_COMPLETION Received completion question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with completion for questions 'KOMP1,KOMP2'");
    }

    @Test
    public void shouldLogQuestionReceivedCompletionWrongSubject() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE,
                Arrays.asList("KOMP1", "KOMP2"));
        verifyLog(Level.INFO,
                "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    public void shouldLogQuestionSent() {
        logService.logQuestionSent(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
                "QUESTION_SENT Sent question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    public void shouldLogQuestionSentWhenParametersAllNull() {
        logService.logQuestionSent(null, null, null, null, null);
        verifyLog(Level.INFO,
                "QUESTION_SENT Sent question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    public void shouldLogUserLogin() {
        logService.logUserLogin(HSA_ID, AUTH_SCHEME, UserOriginType.NORMAL.name());
        verifyLog(Level.INFO, "USER_LOGIN Login user 'HSA_ID' using scheme 'AUTH_SCHEME' with origin 'NORMAL'");
    }

    @Test
    public void shouldLogUserLogout() {
        logService.logUserLogout(HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "USER_LOGOUT Logout user 'HSA_ID' using scheme 'AUTH_SCHEME'");
    }

    @Test
    public void shouldLogUserSessionExpired() {
        logService.logUserSessionExpired(HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "USER_SESSION_EXPIRY Session expired for user 'HSA_ID' using scheme 'AUTH_SCHEME'");
    }

    @Test
    public void shouldLogUtkastConcurrentlyEdited() {
        logService.logUtkastConcurrentlyEdited(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO,
                "UTKAST_CONCURRENTLY_EDITED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was concurrently edited by multiple users");
    }

    @Test
    public void shouldLogUtkastCreated() {
        logService.logUtkastCreated(INTYGS_ID, INTYGS_TYP, ENHET, HSA_ID);
        verifyLog(Level.INFO, "UTKAST_CREATED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' created by 'HSA_ID' on unit 'ENHET'");
    }

    @Test
    public void shouldLogUtkastDeleted() {
        logService.logUtkastDeleted(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_DELETED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was deleted");
    }

    @Test
    public void shouldLogUtkastRevoked() {
        logService.logUtkastRevoked(INTYGS_ID, HSA_ID, REASON, REVOKE_MESSAGE);
        verifyLog(Level.INFO, "UTKAST_REVOKED Utkast 'INTYGS_ID' revoked by 'HSA_ID' reason 'REASON' message 'REVOKE_MESSAGE'");
    }

    @Test
    public void shouldLogUtkastEdited() {
        logService.logUtkastEdited(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_EDITED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was edited");
    }

    @Test
    public void shouldLogUtkastPrint() {
        logService.logUtkastPrint(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_PRINT Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was printed");
    }

    @Test
    public void shouldLogUtkastRead() {
        logService.logUtkastRead(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_READ Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was read");
    }

    @Test
    public void shouldLogIntegratedOtherUnit() {
        logService.logIntegratedOtherUnit(INTYGS_ID, INTYGS_TYP, ENHET);
        verifyLog(Level.INFO, "LOGIN_OTHER_UNIT Viewed intyg 'INTYGS_ID' of type 'INTYGS_TYP' on other unit 'ENHET'");
    }

    @Test
    public void shouldLogIntegratedOtherCaregiver() {
        logService.logIntegratedOtherCaregiver(INTYGS_ID, INTYGS_TYP, VARDGIVARE, ENHET);
        verifyLog(Level.INFO,
                "LOGIN_OTHER_CAREGIVER Viewed intyg 'INTYGS_ID' of type 'INTYGS_TYP' on other caregiver 'VARDGIVARE' unit 'ENHET'");
    }

    @Test
    public void shouldLogScreenResolution() {
        logService.logScreenResolution("WIDTH", "HEIGHT");
        verifyLog(Level.INFO, "SCREEN_RESOLUTION Width 'WIDTH', height 'HEIGHT'");
    }

    @Test
    public void shouldLogRevokedPrinted() {
        logService.logRevokedPrint(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "REVOKED_PRINT Revoked intyg 'INTYGS_ID' of type 'INTYGS_TYP' printed");
    }

    @Test
    public void shouldLogUtkastPatientDetailsUpdated() {
        logService.logUtkastPatientDetailsUpdated(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_PATIENT_UPDATED Patient details for utkast 'INTYGS_ID' of type 'INTYGS_TYP' updated");
    }

    @Test
    public void shouldLogDiagnoskodverkChanged() {
        logService.logDiagnoskodverkChanged(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "DIAGNOSKODVERK_CHANGED Diagnoskodverk changed for utkast 'INTYGS_ID' of type 'INTYGS_TYP'");
    }

    @Test
    public void shouldLogConsentSet() {
        logService.logSetSrsConsent(PERSON_NUMMER, true);
        verifyLog(Level.INFO,
                "SRS_CONSENT_SET Consent set for '9a8b138a666f84da32e9383b49a15f46f6e08d2c492352aa0dfcc3f993773b0d' to 'true'");
    }

    @Test
    public void shouldLogQuestionsListed() {
        logService.logListSrsQuestions("J20");
        verifyLog(Level.INFO, "SRS_QUESTIONS_LISTED Questions listed for diagnosis code 'J20'");
    }

    @Test
    public void shouldLogSrsInformationRetreived() {
        logService.logSrsInformationRetreived("J20", "INTYGS_ID");
        verifyLog(Level.INFO, "SRS_INFORMATION_RETREIVED SRS information retreived for certifiacte 'INTYGS_ID' for diagnosis code 'J20'");
    }

    @Test
    public void shouldLogSrsShown() {
        logService.logSrsShown();
        verifyLog(Level.INFO, "SRS_SHOWN SRS shown");
    }

    @Test
    public void shouldLogSrsClicked() {
        logService.logSrsClicked();
        verifyLog(Level.INFO, "SRS_CLICKED SRS clicked");
    }

    @Test
    public void shouldLogSrsAtgardClicked() {
        logService.logSrsAtgardClicked();
        verifyLog(Level.INFO, "SRS_ATGARD_CLICKED SRS atgard clicked");
    }

    @Test
    public void shouldLogSrsStatistikClicked() {
        logService.logSrsStatistikClicked();
        verifyLog(Level.INFO, "SRS_STATISTIK_CLICKED SRS statistik clicked");
    }
}
