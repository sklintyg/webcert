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
     */
    void logReadOfIntyg(LogRequest logRequest);

    /**
     * Creates a log event when an user requests an intyg as PDF.
     *
     * @param logRequest logRequest
     */
    void logPrintOfIntyg(LogRequest logRequest);

    /**
     * Creates a log event when an user creates a new draft.
     *
     * @param logRequest logRequest
     */
    void logCreateOfDraft(LogRequest logRequest);

    /**
     * Creates a log event when an user updates a draft.
     *
     * @param logRequest logRequest
     */
    void logUpdateOfDraft(LogRequest logRequest);
    
    /**
     * Creates a log event when an user deletes an unsigned draft.
     *
     * @param logRequest logRequest
     */
    void logDeleteOfDraft(LogRequest logRequest);
    
    /**
     * Creates a log event when an user signs a draft.
     *
     * @param logRequest logRequest
     */
    void logSigningOfDraft(LogRequest logRequest);
    
    /**
     * Creates a log event when an user sends a signed intyg directly to a recipient.
     *
     * @param logRequest logRequest
     */
    void logSendIntygToRecipient(LogRequest logRequest);
}
