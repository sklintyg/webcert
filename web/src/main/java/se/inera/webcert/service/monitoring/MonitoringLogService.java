package se.inera.webcert.service.monitoring;

import se.inera.webcert.service.monitoring.MonitoringLogServiceImpl.MonitoringEvent;

public interface MonitoringLogService {
    
    void logEvent(String logMsg, Object... logMsgArgs);

    void logEvent(MonitoringEvent logEvent, String logMsg, Object... logMsgArgs);

}
