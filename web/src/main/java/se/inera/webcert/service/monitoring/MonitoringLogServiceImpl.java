package se.inera.webcert.service.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.inera.certificate.logging.LogMarkers;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);
    
    public void logEvent(String logMsg, Object... logMsgArgs) {
        logEvent(null, logMsg, logMsgArgs);
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.monitoring.MonitoringLogService#logEvent(java.lang.String, java.lang.Object)
     */
    @Override
    public void logEvent(MonitoringEvent logEvent, String logMsg, Object... logMsgArgs) {
        
        if (logEvent != null) {
            logMsg = logEvent + " " + logMsg;
        }
        
        LOG.info(LogMarkers.MONITORING, logMsg, logMsgArgs);
    }
    
    public enum MonitoringEvent {
        MAIL_SENT,
        USER_LOGIN,
        USER_LOGOUT,
        QUESTION_RECEIVED,
        ANSWER_RECEIVED,
        QUESTION_SENT,
        ANSWER_SENT,
        INTYG_SIGNED,
        INTYG_REGISTERED,
        INTYG_SENT,
        INTYG_REVOKED,
        INTYG_COPIED,
        UTKAST_CREATED,
        UTKAST_DELETED;
    }
}
