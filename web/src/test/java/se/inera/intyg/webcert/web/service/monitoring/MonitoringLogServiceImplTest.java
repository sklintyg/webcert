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

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import se.inera.certificate.modules.support.api.dto.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {

    private static final String INTYGS_ID = "INTYGS_ID";
    private static final String ENHET = "ENHET";
    private static final String COPY_INTYGS_ID = "COPY_INTYGS_ID";
    private static final String ORGINAL_INTYG_ID = "ORGINAL_INTYG_ID";
    private static final String INTYGS_TYP = "INTYGS_TYP";
    private static final String HSA_ID = "HSA_ID";
    private static final String RECIPIENT = "RECIPIENT";
    private static final String AUTH_SCHEME = "AUTH_SCHEME";
    private static final String REASON = "REASON";
    private static final Integer AVTAL_VERSION = 98;
    private static final String HAN_TYPE = "HAN_TYPE";
    private static final Personnummer PERSON_NUMMER = new Personnummer("PERSON_NUMMER");
    private static final String RESULT = "RESULT";
    private static final String FRAGESTALLARE = "FRAGESTALLARE";
    private static final String EXTERN_REFERENS = "EXTERN_REFERENS";
    private static final long INTERN_REFERENS = 97;
    private static final String AMNE = "AMNE";
    private static final String UNIT_HSA_ID = "UNIT_HSA_ID";
    private static final String USER_HSA_ID = "USER_HSA_ID";
    private static final String PERSON_ID = "PERSON_ID";

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
                "ANSWER_RECEIVED Received answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'AMNE'");
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
                "ANSWER_SENT Sent answer to question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'AMNE'");
    }

    @Test
    public void shouldLogIntygCopied() {
        logService.logIntygCopied(COPY_INTYGS_ID, ORGINAL_INTYG_ID);
        verifyLog(Level.INFO, "INTYG_COPIED Utkast 'COPY_INTYGS_ID' created as a copy of 'ORGINAL_INTYG_ID'");
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
        logService.logIntygRevoked(INTYGS_ID, HSA_ID);
        verifyLog(Level.INFO, "INTYG_REVOKED Intyg 'INTYGS_ID' revoked by 'HSA_ID'");
    }

    @Test
    public void shouldLogIntygSent() {
        logService.logIntygSent(INTYGS_ID, RECIPIENT);
        verifyLog(Level.INFO, "INTYG_SENT Intyg 'INTYGS_ID' sent to recipient 'RECIPIENT'");
    }

    @Test
    public void shouldLogIntygSigned() {
        logService.logIntygSigned(INTYGS_ID, HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "INTYG_SIGNED Intyg 'INTYGS_ID' signed by 'HSA_ID' using scheme 'AUTH_SCHEME'");
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
        logService.logNotificationSent(HAN_TYPE, ENHET);
        verifyLog(Level.INFO, "NOTIFICATION_SENT Sent notification of type 'HAN_TYPE' to unit 'ENHET'");
    }

    @Test
    public void shouldLogPrivatePractitionerTermsApproved() {
        logService.logPrivatePractitionerTermsApproved(HSA_ID, PERSON_ID, AVTAL_VERSION);
        verifyLog(Level.INFO,
                "PP_TERMS_ACCEPTED User 'HSA_ID', personId 'ad060a2437cb0e66f41f3305bc8ba6e69b9db04805d6c7fddd720079ef673921' accepted private practitioner terms of version '98'");
    }

    @Test
    public void shouldLogPULookup() {
        logService.logPULookup(PERSON_NUMMER, RESULT);
        verifyLog(Level.INFO,
                "PU_LOOKUP Lookup performed on '83e43d8552bbc5ef0fd9fa200688dc2a4ea6c443df46e641d30b07ebc0967b0d' with result 'RESULT'");
    }

    @Test
    public void shouldLogQuestionReceived() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE);
        verifyLog(Level.INFO,
                "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'AMNE'");
    }

    @Test
    public void shouldLogQuestionSent() {
        logService.logQuestionSent(EXTERN_REFERENS, INTERN_REFERENS, INTYGS_ID, ENHET, AMNE);
        verifyLog(Level.INFO,
                "QUESTION_SENT Sent question with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'AMNE'");
    }

    @Test
    public void shouldLogQuestionSentWhenParametersAllNull() {
        logService.logQuestionSent(null, null, null, null, null);
        verifyLog(Level.INFO,
                "QUESTION_SENT Sent question with external reference 'null' and internal reference 'null' regarding intyg 'null' to unit 'null' with subject 'null'");
    }

    @Test
    public void shouldLogUserLogin() {
        logService.logUserLogin(HSA_ID, AUTH_SCHEME);
        verifyLog(Level.INFO, "USER_LOGIN Login user 'HSA_ID' using scheme 'AUTH_SCHEME'");
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
        verifyLog(Level.INFO, "UTKAST_CONCURRENTLY_EDITED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was concurrently edited by multiple users");
    }

    @Test
    public void shouldLogUtkastCreated() {
        logService.logUtkastCreated(INTYGS_ID, INTYGS_TYP, UNIT_HSA_ID, USER_HSA_ID);
        verifyLog(Level.INFO, "UTKAST_CREATED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' created by 'USER_HSA_ID' on unit 'UNIT_HSA_ID'");
    }

    @Test
    public void shouldLogUtkastDeleted() {
        logService.logUtkastDeleted(INTYGS_ID, INTYGS_TYP);
        verifyLog(Level.INFO, "UTKAST_DELETED Utkast 'INTYGS_ID' of type 'INTYGS_TYP' was deleted");
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
}
