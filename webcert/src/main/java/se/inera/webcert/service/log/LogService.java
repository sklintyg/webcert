package se.inera.webcert.service.log;

import se.inera.webcert.service.log.dto.LogRequest;


/**
 * Service for logging user actions according to PDL requirements.
 * 
 * @author andreaskaltenbach
 */
public interface LogService {

    /**
     * Creates a log event when a user accesses an intyg.
     * 
     * @param intygId
     * @param patientId
     */
    void logReadOfIntyg(String intygId, String patientId);
    
    /**
     * Creates a log event when an user requests an intyg as PDF.
     * 
     * @param intygId
     * @param patientId
     */
    void logPrintOfIntyg(String intygId, String patientId);

    /**
     * Creates a log event when an user creates a new intyg.
     * 
     * @param logRequest
     */
    void logCreateOfIntyg(LogRequest logRequest);
}
