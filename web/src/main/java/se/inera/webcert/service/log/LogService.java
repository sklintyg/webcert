package se.inera.webcert.service.log;

import se.inera.webcert.hsa.model.WebCertUser;
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
     * @param logRequest
     *            logRequest
     */
    void logReadOfIntyg(LogRequest logRequest, WebCertUser user);

    /**
     * Creates a log event when an user requests an intyg as PDF.
     *
     * @param logRequest
     *            logRequest
     */
    void logPrintOfIntygAsPDF(LogRequest logRequest, WebCertUser user);

    /**
     * Creates a log event when an user requests a print-out of an intyg as draft.
     *
     * @param logRequest
     *            logRequest
     */
    void logPrintOfIntygAsDraft(LogRequest logRequest, WebCertUser user);

    /**
     * Creates a log event when an user sends a signed intyg directly to a recipient.
     *
     * @param logRequest
     *            logRequest
     */
    void logSendIntygToRecipient(LogRequest logRequest, WebCertUser user);
}
