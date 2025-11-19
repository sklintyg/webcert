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
package se.inera.intyg.webcert.web.service.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.web.service.mail.MailNotification;

// CHECKSTYLE:OFF LineLength
@ExtendWith(MockitoExtension.class)
class MonitoringLogServiceImplTest {

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
    private static final String USER_ROLE = "USER_ROLE";
    private static final String USER_ROLE_TYPE_NAME = "USER_ROLE_TYPE_NAME";
    private static final String CARE_PROVIDER = "careProvider";
    private static final String UNIT = "unit";


    private static final Personnummer PERSON_NUMMER = Personnummer.createPersonnummer(PERSON_ID).orElseThrow();
    private static final String MESSAGE_ID = "messageId";
    private static final String SALT = "salt";

    @Mock
    private Appender<ILoggingEvent> mockAppender;
    @Mock
    private MailNotification mailNotification;
    @Spy
    private HashUtility hashUtility;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @InjectMocks
    private MonitoringLogServiceImpl logService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(hashUtility, SALT, SALT);
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    void teardown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void shouldLogAnswerReceived() {
        logService.logAnswerReceived(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
            "ANSWER_RECEIVED Received answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    void shouldLogAnswerReceivedWhenAllParamtersNull() {
        logService.logAnswerReceived(null, null, null, null, null);
        verifyLog(Level.INFO,
            "ANSWER_RECEIVED Received answer to question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    private void verifyLog(Level logLevel, String logMessage) {
        // Verify and capture logging interaction
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertEquals(logLevel, loggingEvent.getLevel());
        assertEquals(logMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    void shouldLogAnswerSent() {
        logService.logAnswerSent(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
            "ANSWER_SENT Sent answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    void shouldLogAnswerSentWithAllParametersNull() {
        logService.logAnswerSent(null, null, null, null, null);
        verifyLog(Level.INFO,
            "ANSWER_SENT Sent answer to question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    void shouldLogIntygCopied() {
        logService.logIntygCopied(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED Utkast 'COPY_INTYGS_ID' created as a copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    void shouldLogIntygCopiedRenewal() {
        logService.logIntygCopiedRenewal(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_RENEWAL Utkast 'COPY_INTYGS_ID' created as a renewal copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    void shouldLogIntygCopiedReplacement() {
        logService.logIntygCopiedReplacement(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_REPLACEMENT Utkast 'COPY_INTYGS_ID' created as a replacement copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    void shouldLogIntygCopiedCompletion() {
        logService.logIntygCopiedCompletion(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED_COMPLETION Utkast 'COPY_INTYGS_ID' created as a completion copy of 'ORGINAL_INTYG_ID'");
    }

    @Test
    void shouldLogIntygPrintPdfMinimal() {
        logService.logIntygPrintPdf(INTYGS_ID, INTYGS_TYP, true);
        verifyLog(Level.INFO, "INTYG_PRINT_PDF Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was printed as PDF with 'MINIMAL' content");
    }

    @Test
    void shouldLogIntygPrintPdfComplete() {
        logService.logIntygPrintPdf(INTYGS_ID, INTYGS_TYP, false);
        verifyLog(Level.INFO, "INTYG_PRINT_PDF Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was printed as PDF with 'FULL' content");
    }

    @Test
    void shouldLogIntygRead() {
        logService.logIntygRead(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "INTYG_READ Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was read");
    }

    @Test
    void shouldLogIntygRegistered() {
        logService.logIntygRegistered(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "INTYG_REGISTERED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' registered with Intygstjänsten");
    }

    @Test
    void shouldLogIntygRevoked() {
        logService.logIntygRevoked(INTYGS_ID, INTYGS_TYP, HSA_ID, REASON);
        verifyLog(Level.INFO, "INTYG_REVOKED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' revoked by 'HSA_ID' reason 'REASON'");
    }

    @Test
    void shouldLogIntygSent() {
        logService.logIntygSent(INTYGS_ID, INTYGS_TYP, RECIPIENT);
        verifyLog(Level.INFO, "INTYG_SENT Intyg 'INTYGS_ID' of type 'INTYGS_TYP' sent to recipient 'RECIPIENT'");
    }

    @Test
    void shouldLogIntygSigned() {
        logService.logIntygSigned(INTYGS_ID, INTYGS_TYP, HSA_ID, AUTH_SCHEME, null);
        verifyLog(Level.INFO,
            "INTYG_SIGNED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' signed by 'HSA_ID' using scheme 'AUTH_SCHEME' and relation code 'NO RELATION'");
    }

    @Test
    void shouldLogIntygSignedWithRelation() {
        logService.logIntygSigned(INTYGS_ID, INTYGS_TYP, HSA_ID, AUTH_SCHEME, RelationKod.KOMPLT);
        verifyLog(Level.INFO,
            "INTYG_SIGNED Intyg 'INTYGS_ID' of type 'INTYGS_TYP' signed by 'HSA_ID' using scheme 'AUTH_SCHEME' and relation code 'KOMPLT'");
    }

    @Test
    void shouldLogMailMissingAddress() {
        logService.logMailMissingAddress(HSA_ID, REASON, mailNotification);
        verifyLog(Level.INFO, "MAIL_MISSING_ADDRESS Mail sent to admin on behalf of unit 'HSA_ID' for REASON");
    }

    @Test
    void shouldLogMailSent() {
        logService.logMailSent(HSA_ID, REASON, mailNotification);
        verifyLog(Level.INFO, "MAIL_SENT Mail sent to unit 'HSA_ID' for REASON");
    }

    @Test
    void shouldLogMissingMedarbetarUppdrag() {
        logService.logMissingMedarbetarUppdrag(HSA_ID);
        verifyLog(Level.INFO, "USER_MISSING_MIU No valid MIU was found for user 'HSA_ID'");
    }

    @Test
    void shouldLogMissingMedarbetarUppdragWithEnhet() {
        logService.logMissingMedarbetarUppdrag(HSA_ID, ENHET);
        verifyLog(Level.INFO, "USER_MISSING_MIU_ON_ENHET No valid MIU was found for user 'HSA_ID' on unit 'ENHET'");
    }

    @Test
    void shouldLogNotificationSent() {
        logService.logNotificationSent(HAN_TYPE, ENHET, INTYGS_ID);
        verifyLog(Level.INFO, "NOTIFICATION_SENT Sent notification of type 'HAN_TYPE' to unit 'ENHET' for 'INTYGS_ID'");
    }

    @Test
    void shouldLogArendeReceived() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KONTKT, null, false, MESSAGE_ID);
        verifyLog(Level.INFO,
            "ARENDE_RECEIVED_QUESTION Received arende with amne 'KONTKT' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    void shouldLogArendeReceivedWithAllParametersNull() {
        logService.logArendeReceived(null, null, null, null, null, false, null);
        verifyLog(Level.INFO, "ARENDE_RECEIVED_QUESTION Received arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    void shouldLogArendeReceivedCompletion() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KOMPLT, Arrays.asList("1", "2"), false, MESSAGE_ID);
        verifyLog(Level.INFO,
            "MEDICINSKT_ARENDE_RECEIVED Received medicinskt arende for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET' on questions '[1, 2]'");
    }

    @Test
    void shouldLogArendeReceivedAnswer() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.KONTKT, null, true, MESSAGE_ID);
        verifyLog(Level.INFO,
            "ARENDE_RECEIVED_ANSWER Received arende with amne 'KONTKT' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    void shouldLogArendeReceivedAnswerAmneMissing() {
        logService.logArendeReceived(INTYGS_ID, INTYGS_TYP, ENHET, null, null, true, MESSAGE_ID);
        verifyLog(Level.INFO,
            "ARENDE_RECEIVED_ANSWER Received arende with amne 'NO AMNE' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    void shouldLogArendeCreated() {
        logService.logArendeCreated(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.AVSTMN, false, MESSAGE_ID);
        verifyLog(Level.INFO,
            "ARENDE_CREATED_QUESTION Created arende with amne 'AVSTMN' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    void shouldLogArendeCreatedWithAllParametersNull() {
        logService.logArendeCreated(null, null, null, null, false, null);
        verifyLog(Level.INFO, "ARENDE_CREATED_QUESTION Created arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    void shouldLogArendeCreatedAnswer() {
        logService.logArendeCreated(INTYGS_ID, INTYGS_TYP, ENHET, ArendeAmne.AVSTMN, true, MESSAGE_ID);
        verifyLog(Level.INFO,
            "ARENDE_CREATED_ANSWER Created arende with amne 'AVSTMN' for 'INTYGS_ID' of type 'INTYGS_TYP' for unit 'ENHET'");
    }

    @Test
    void shouldLogArendeCreatedAnswerWithAllParametersNull() {
        logService.logArendeCreated(null, null, null, null, true, null);
        verifyLog(Level.INFO, "ARENDE_CREATED_ANSWER Created arende with amne 'NO AMNE' for 'null' of type 'null' for unit 'null'");
    }

    @Test
    void shouldLogPrivatePractitionerTermsApproved() {
        logService.logPrivatePractitionerTermsApproved(HSA_ID, PERSON_NUMMER, AVTAL_VERSION);
        verifyLog(Level.INFO,
            "PP_TERMS_ACCEPTED User 'HSA_ID', personId 'be125ef854ae8e7083ab76ebd2d7cd748e05603e02aec5cc3afeacd57d8f5f4b' accepted private practitioner terms of version '98'");
    }

    @Test
    void shouldLogPULookup() {
        logService.logPULookup(PERSON_NUMMER, RESULT);
        verifyLog(Level.INFO,
            "PU_LOOKUP Lookup performed on 'be125ef854ae8e7083ab76ebd2d7cd748e05603e02aec5cc3afeacd57d8f5f4b' with result 'RESULT'");
    }

    @Test
    void shouldLogQuestionReceived() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE, null);
        verifyLog(Level.INFO,
            "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    void shouldLogQuestionReceivedWhenAllParametersNull() {
        logService.logQuestionReceived(null, null, null, null, null, null, null);
        verifyLog(Level.INFO,
            "QUESTION_RECEIVED Received question from 'null' with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    void shouldLogQuestionReceivedCompletion() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, Amne.KOMPLETTERING_AV_LAKARINTYG,
            Arrays.asList("KOMP1", "KOMP2"));
        verifyLog(Level.INFO,
            "QUESTION_RECEIVED_COMPLETION Received completion question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with completion for questions 'KOMP1,KOMP2'");
    }

    @Test
    void shouldLogQuestionReceivedCompletionWrongSubject() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE,
            Arrays.asList("KOMP1", "KOMP2"));
        verifyLog(Level.INFO,
            "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    void shouldLogQuestionSent() {
        logService.logQuestionSent(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
            "QUESTION_SENT Sent question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'ARBETSTIDSFORLAGGNING'");
    }

    @Test
    void shouldLogQuestionSentWhenParametersAllNull() {
        logService.logQuestionSent(null, null, null, null, null);
        verifyLog(Level.INFO,
            "QUESTION_SENT Sent question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'NO AMNE'");
    }

    @Test
    void shouldLogUserLogin() {
        logService.logUserLogin(HSA_ID, USER_ROLE, USER_ROLE_TYPE_NAME, AUTH_SCHEME, UserOriginType.NORMAL.name());
        verifyLog(Level.INFO,
            "USER_LOGIN Login user 'HSA_ID' as role 'USER_ROLE' roleTypeName 'USER_ROLE_TYPE_NAME' using scheme 'AUTH_SCHEME' with origin 'NORMAL'");
    }

    @Test
    void shouldLogUserLogout() {
        logService.logUserLogout(HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "USER_LOGOUT Logout user 'HSA_ID' using scheme 'AUTH_SCHEME'");
    }

    @Test
    void shouldLogUserSessionExpired() {
        logService.logUserSessionExpired(HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "USER_SESSION_EXPIRY Session expired for user 'HSA_ID' using scheme 'AUTH_SCHEME'");
    }

    @Test
    void shouldLogUtkastConcurrentlyEdited() {
        logService.logUtkastConcurrentlyEdited(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO,
            "UTKAST_CONCURRENTLY_EDITED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was concurrently edited by multiple users");
    }

    @Test
    void shouldLogUtkastCreated() {
        logService.logUtkastCreated(INTYGS_ID, INTYGS_TYP, ENHET, HSA_ID, 0);
        verifyLog(Level.INFO,
            "UTKAST_CREATED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' created by 'HSA_ID' on unit 'ENHET'");
    }

    @Test
    void shouldLogUtkastCreatedWithPrefill() {
        logService.logUtkastCreated(INTYGS_ID, INTYGS_TYP, ENHET, HSA_ID, 2);
        verifyLog(Level.INFO,
            "UTKAST_CREATED_PREFILL Utkast 'INTYGS_ID' of type 'INTYGS_TYP' created with '2' forifyllnad svar by 'HSA_ID' on unit 'ENHET'");
    }

    @Test
    void shouldLogUtkastDeleted() {
        logService.logUtkastDeleted(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_DELETED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was deleted");
    }

    @Test
    void shouldLogUtkastPruned() {
        logService.logUtkastPruned(INTYGS_ID, INTYGS_TYP, Period.between(LocalDate.now(), LocalDate.now().plusDays(5)));
        verifyLog(Level.INFO, "UTKAST_PRUNED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was pruned due to being stale for more than '5' days");
    }

    @Test
    void shouldLogUtkastRevoked() {
        logService.logUtkastRevoked(INTYGS_ID, HSA_ID, REASON);
        verifyLog(Level.INFO, "UTKAST_REVOKED Utkast 'INTYGS_ID' revoked by 'HSA_ID' reason 'REASON'");
    }

    @Test
    void shouldLogUtkastEdited() {
        logService.logUtkastEdited(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_EDITED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was edited");
    }

    @Test
    void shouldLogUtkastPrint() {
        logService.logUtkastPrint(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_PRINT Intyg 'INTYGS_ID' of type 'INTYGS_TYP' was printed");
    }

    @Test
    void shouldLogUtkastRead() {
        logService.logUtkastRead(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_READ Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was read");
    }

    @Test
    void shouldLogIntegratedOtherUnit() {
        logService.logIntegratedOtherUnit(INTYGS_ID, INTYGS_TYP, VARDGIVARE, ENHET, CARE_PROVIDER, UNIT);
        verifyLog(Level.INFO, "LOGIN_OTHER_UNIT Viewed intyg 'INTYGS_ID' of type 'INTYGS_TYP' on other unit 'ENHET'");
    }

    @Test
    void shouldLogIntegratedOtherCaregiver() {
        logService.logIntegratedOtherCaregiver(INTYGS_ID, INTYGS_TYP, VARDGIVARE, ENHET, CARE_PROVIDER, UNIT);
        verifyLog(Level.INFO,
            "LOGIN_OTHER_CAREGIVER Viewed intyg 'INTYGS_ID' of type 'INTYGS_TYP' on other caregiver 'VARDGIVARE' unit 'ENHET'");
    }

    @Test
    void shouldLogBrowserInfo() {
        logService.logBrowserInfo("BROWSERNAME", "VERSION", "OS", "OS-VERSION", "WIDTH", "HEIGHT", "NETIDVERSION");
        verifyLog(Level.INFO,
            "BROWSER_INFO Name 'BROWSERNAME' Version 'VERSION' OSFamily 'OS' OSVersion 'OS-VERSION' Width 'WIDTH' Height 'HEIGHT' NetIdVersion 'NETIDVERSION'");
    }

    @Test
    void shouldLogRevokedPrinted() {
        logService.logRevokedPrint(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "REVOKED_PRINT Revoked intyg 'INTYGS_ID' of type 'INTYGS_TYP' printed");
    }

    @Test
    void shouldLogUtkastPatientDetailsUpdated() {
        logService.logUtkastPatientDetailsUpdated(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_PATIENT_UPDATED Patient details for utkast 'INTYGS_ID' of type 'INTYGS_TYP' updated");
    }

    @Test
    void shouldLogDiagnoskodverkChanged() {
        logService.logDiagnoskodverkChanged(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "DIAGNOSKODVERK_CHANGED Diagnoskodverk changed for utkast 'INTYGS_ID' of type 'INTYGS_TYP'");
    }

    @Test
    void shouldLogSrsLoaded() {
        logService.logSrsLoaded("UTK", "intyg", "vardgivare", "vardenhet", "F438A");
        verifyLog(Level.INFO,
            "SRS_LOADED SRS loaded in client context 'UTK' for intyg 'intyg' and diagnosis code 'F438A' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsPanelActivated() {
        logService.logSrsPanelActivated("FRL", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_PANEL_ACTIVATED SRS panel activated in client context 'FRL' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsConsentAnswered() {
        logService.logSrsConsentAnswered("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_CONSENT_ANSWERED SRS consent answered in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsQuestionAnswered() {
        logService.logSrsQuestionAnswered("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_QUESTION_ANSWERED SRS question answered in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsCalculateClicked() {
        logService.logSrsCalculateClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_CALCULATE_CLICKED SRS calculate prediction clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsHideQuestionsClicked() {
        logService.logSrsHideQuestionsClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_HIDE_QUESTIONS_CLICKED SRS hide questions clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsShowQuestionsClicked() {
        logService.logSrsShowQuestionsClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_SHOW_QUESTIONS_CLICKED SRS show questions clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsMeasuresShowMoreClicked() {
        logService.logSrsMeasuresShowMoreClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_MEASURES_SHOW_MORE_CLICKED SRS show more measures clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsMeasuresLinkClicked() {
        logService.logSrsMeasuresLinkClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_MEASURES_LINK_CLICKED SRS measures link clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsStatisticsActivated() {
        logService.logSrsStatisticsActivated("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_STATISTICS_ACTIVATED SRS statistics tab activated in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogSrsStatisticsLinkClicked() {
        logService.logSrsStatisticsLinkClicked("UTK", "intyg", "vardgivare", "vardenhet");
        verifyLog(Level.INFO,
            "SRS_STATISTICS_LINK_CLICKED SRS statistics link clicked in client context 'UTK' for intyg 'intyg' with caregiver 'vardgivare' and care unit 'vardenhet'");
    }

    @Test
    void shouldLogTestCertificateErased() {
        logService.logTestCertificateErased("CertificateId", "CareUnitId", "CreatedUserId");
        verifyLog(Level.INFO,
            "TEST_CERTIFICATE_ERASED Test certificate 'CertificateId' on care unit 'CareUnitId' created by 'CreatedUserId' was erased");
    }

    @Test
    void shouldLogMessageImported() {
        logService.logMessageImported("CertificateId", "MessageId", "CareGiverId", "CareUnitId", "MessageType");
        verifyLog(Level.INFO,
            "MESSAGE_IMPORTED Message 'MessageId' with type 'MessageType' for certificate 'CertificateId' on caregiver 'CareGiverId' and care unit 'CareUnitId' was imported");
    }

    @Test
    void shouldLogSubscriptionServiceCallFailure() {
        final var hsaIds = Collections.singleton(HSA_ID);
        logService.logSubscriptionServiceCallFailure(hsaIds, "exceptionMessage");
        verifyLog(Level.INFO,
            "SUBSCRIPTION_SERVICE_CALL_FAILURE Subscription service call failure for id's '[HSA_ID]', with exceptionMessage "
                + "'exceptionMessage'");
    }

    @Test
    void shouldLogLoginAttemptMissingSubscription() {
        logService.logLoginAttemptMissingSubscription("userId", "SITHS", "[HSA_ID_1, HSA_ID_2]");
        verifyLog(Level.INFO,
            "LOGIN_ATTEMPT_MISSING_SUBSCRIPTION User id 'userId' attempting login with 'SITHS' was denied access to "
                + "organizations '[HSA_ID_1, HSA_ID_2]' due to missing subscriptions");
    }

    @Test
    void shouldLogSubscriptionWarnings() {
        logService.logSubscriptionWarnings("userId", "SITHS", "[HSA_ID_1, HSA_ID_2]");
        verifyLog(Level.INFO,
            "MISSING_SUBSCRIPTION_WARNING User id 'userId' logging in with 'SITHS' received subscription warning for "
                + "organizations '[HSA_ID_1, HSA_ID_2]'");
    }
// CHECKSTYLE:ON LineLength
}