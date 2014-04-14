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
     * @param logRequest logRequest
     * @param patientId
     */
    void logReadOfIntyg(LogRequest logRequest);

    /**
     * Creates a log event when an user requests an intyg as PDF.
     *
     * @param logRequest logRequest
     * @param patientId
     */
    void logPrintOfIntyg(LogRequest logRequest);

    /**
     * Creates a log event when an user creates a new intyg.
     *
     * @param logRequest logRequest
     */
    void logCreateOfDraft(LogRequest logRequest);

    void logUpdateOfDraft(LogRequest logRequest);

    void logDeleteOfDraft(LogRequest logRequest);
}
