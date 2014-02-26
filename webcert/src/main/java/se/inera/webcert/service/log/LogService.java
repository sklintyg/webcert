package se.inera.webcert.service.log;

import se.inera.webcert.service.log.dto.LogRequest;


/**
 * @author andreaskaltenbach
 */
public interface LogService {

    void logReadOfIntyg(String intygId, String patientId);
    
    void logPrintOfIntyg(String intygId, String patientId);

    void logCreateOfIntyg(LogRequest logRequest);
}
