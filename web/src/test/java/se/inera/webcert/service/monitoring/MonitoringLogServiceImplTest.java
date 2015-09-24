package se.inera.webcert.service.monitoring;

import static org.hamcrest.CoreMatchers.is;
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

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {
    
    private static final Long FRAGA_SVARS_ID = new Long(99);
    private static final String INTYGS_ID = "INTYGS_ID";
    private static final String ENHET = "ENHET";
    private static final String COPY_INTYGS_ID = "COPY_INTYGS_ID";
    private static final String ORGINAL_INTYG_ID = "ORGINAL_INTYG_ID";
    private static final String INTYGS_TYP = "INTYGS_TYP";
    private static final String HSA_ID = "HSA_ID";
    private static final String RECIPIENT = "RECIPIENT";
    private static final String AUTH_SCHEME = "AUTH_SCHEME";
    private static final String REASON = "REASON";
    private static final String USER_ID = "USER_ID";
    private static final Integer AVTAL_VERSION = 98;
    private static final String HAN_TYPE = "HAN_TYPE";
    private static final String PERSON_NUMMER = "PERSON_NUMMER";
    private static final String RESULT = "RESULT";
    private static final String FRAGESTALLARE = "FRAGESTALLARE";
    private static final String EXTERN_REFERENS = "EXTERN_REFERENS";
    private static final long INTERN_REFERENS = 97;
    private static final String AMNE = "AMNE";
    private static final String UNIT_HSA_ID = "UNIT_HSA_ID";
    private static final String USER_HSA_ID = "USER_HSA_ID"; 

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
    
    MonitoringLogService logService = new MonitoringLogServiceImpl();

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
        logService.logAnswerReceived(FRAGA_SVARS_ID, INTYGS_ID, ENHET);
        verifyLog(Level.INFO, "ANSWER_RECEIVED Received answer to question '99' regarding intyg 'INTYGS_ID' to unit 'ENHET'");
    }

    private void verifyLog(Level logLevel, String logMessage) {
        // Verify and capture logging interaction
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        // Verify log
        assertThat(loggingEvent.getLevel(), is(logLevel));
        assertThat(loggingEvent.getFormattedMessage(), 
                is(logMessage));
    }

    @Test
    public void shouldLogAnswerSent() {
        logService.logAnswerSent(FRAGA_SVARS_ID, INTYGS_ID);
        verifyLog(Level.INFO, "ANSWER_SENT Sent answer to question '99' regarding intyg 'INTYGS_ID'");
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
        logService.logNotificationSent(ENHET, HAN_TYPE);
        verifyLog(Level.INFO, "NOTIFICATION_SENT Sent notification of type 'ENHET' to unit 'HAN_TYPE' ");
    }

    @Test
    public void shouldLogPrivatePractitionerTermsApproved() {
        logService.logPrivatePractitionerTermsApproved(USER_ID, AVTAL_VERSION);
        verifyLog(Level.INFO, "PP_TERMS_ACCEPTED User 'e5bb97d1792ff76e360cd8e928b6b9b53bda3e4fe88b026e961c2facf963a361' accepted private practitioner terms of version 98");
    }

  @Test
    public void shouldLogPULookup() {
      logService.logPULookup(PERSON_NUMMER, RESULT);
        verifyLog(Level.INFO, "PU_LOOKUP Lookup performed on '83e43d8552bbc5ef0fd9fa200688dc2a4ea6c443df46e641d30b07ebc0967b0d' with result 'RESULT'");
    }

    @Test
    public void shouldLogQuestionReceived() {
        logService.logQuestionReceived(FRAGESTALLARE, INTYGS_ID, EXTERN_REFERENS, INTERN_REFERENS, ENHET, AMNE);
        verifyLog(Level.INFO, "QUESTION_RECEIVED Received question from 'FRAGESTALLARE' with external reference 'EXTERN_REFERENS' and internal reference '97' regarding intyg 'INTYGS_ID' to unit 'ENHET' with subject 'AMNE'");
    }

    @Test
    public void shouldLogQuestionSent() {
        logService.logQuestionSent(FRAGA_SVARS_ID, INTYGS_ID);
        verifyLog(Level.INFO, "QUESTION_SENT Sent question '99' regarding intyg 'INTYGS_ID'");
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
