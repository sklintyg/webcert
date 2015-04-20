package se.inera.webcert.service.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.inera.certificate.logging.LogMarkers;

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
    public void logQuestionReceived(String fragestallare, String intygsId, String externReferens) {
        logEvent(MonitoringEvent.QUESTION_RECEIVED, fragestallare, intygsId, externReferens);
    }

    @Override
    public void logAnswerReceived(Long fragaSvarsId, String intygsId) {
        logEvent(MonitoringEvent.ANSWER_RECEIVED, fragaSvarsId, intygsId);
    }

    @Override
    public void logQuestionSent(Long fragaSvarsId, String intygId) {
        logEvent(MonitoringEvent.QUESTION_SENT, fragaSvarsId, intygId);
    }

    @Override
    public void logAnswerSent(Long fragaSvarsId, String intygsId) {
        logEvent(MonitoringEvent.ANSWER_SENT, fragaSvarsId, intygsId);
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
    public void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId) {
        logEvent(MonitoringEvent.UTKAST_CREATED, intygsId, intygsTyp, unitHsaId);
    }

    @Override
    public void logUtkastEdited(String intygsId, String intygsTyp) {
        logEvent(MonitoringEvent.UTKAST_EDITED, intygsId, intygsTyp);
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
    public void logPULookup(String personNummer, String result) {
        logEvent(MonitoringEvent.PU_LOOKUP, personNummer, result);
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
        QUESTION_RECEIVED("Received question from '{}' regarding intyg '{}' with reference '{}'"),
        ANSWER_RECEIVED("Received answer to question '{}' regarding intyg '{}'"),
        QUESTION_SENT("Sent question '{}' regarding intyg '{}'"),
        ANSWER_SENT("Sent answer to question '{}' regarding intyg '{}'"),
        INTYG_READ("Intyg '{}' of type '{}' was read"),
        INTYG_PRINT_PDF("Intyg '{}' of type '{}' was printed as PDF"),
        INTYG_SIGNED("Intyg '{}' signed by '{}' using scheme '{}'"),
        INTYG_REGISTERED("Intyg '{}' of type '{}' registered with Intygstjänsten"),
        INTYG_SENT("Intyg '{}' sent to recipient '{}'"),
        INTYG_REVOKED("Intyg '{}' revoked by '{}'"),
        INTYG_COPIED("Utkast '{}' created as a copy of '{}'"),
        UTKAST_READ("Utkast '{}' of type '{}' was read"),
        UTKAST_CREATED("Utkast '{}' of type '{}' created on unit '{}'"),
        UTKAST_EDITED("Utkast '{}' of type '{}' was edited"),
        UTKAST_DELETED("Utkast '{}' of type '{}' was deleted"),
        UTKAST_PRINT("Intyg '{}' of type '{}' was printed"),
        PU_LOOKUP("Lookup performed on '{}' with result '{}'");

        private String msg;

        private MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }
}
