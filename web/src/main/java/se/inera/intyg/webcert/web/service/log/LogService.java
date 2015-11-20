package se.inera.intyg.webcert.web.service.log;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;

/**
 * Service for logging user actions according to PDL requirements.
 *
 * @author andreaskaltenbach
 */
public interface LogService {

    /**
     * Creates a log event when a user creates an intyg.
     *
     * @param logRequest the logging details
     */
    void logCreateIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user creates an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logCreateIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user accesses an intyg.
     *
     * @param logRequest the logging details
     */
    void logReadIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user accesses an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logReadIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user updates an intyg.
     *
     * @param logRequest the logging details
     */
    void logUpdateIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user updates an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logUpdateIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user deletes an intyg.
     *
     * @param logRequest the logging details
     */
    void logDeleteIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user deletes an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logDeleteIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user signs an intyg.
     *
     * @param logRequest the logging details
     */
    void logSignIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user signs an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logSignIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user revokes an intyg.
     *
     * @param logRequest the logging details
     */
    void logRevokeIntyg(LogRequest logRequest);

    /**
     * Creates a log event when a user revokes an intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logRevokeIntyg(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when an user requests an intyg as PDF.
     *
     * @param logRequest the logging details
     */
    void logPrintIntygAsPDF(LogRequest logRequest);

    /**
     * Creates a log event when an user requests an intyg as PDF.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logPrintIntygAsPDF(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when an user requests a print-out of an intyg as draft.
     *
     * @param logRequest the logging details
     */
    void logPrintIntygAsDraft(LogRequest logRequest);

    /**
     * Creates a log event when an user requests a print-out of an intyg as draft.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logPrintIntygAsDraft(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when an user sends a signed intyg directly to a recipient.
     *
     * @param logRequest the logging details
     */
    void logSendIntygToRecipient(LogRequest logRequest);

    /**
     * Creates a log event when an user sends a signed intyg directly to a recipient.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logSendIntygToRecipient(LogRequest logRequest, LogUser user);

    /**
     * Use this to create a {@link LogUser} instance from a supplied {@link WebCertUser}
     *
     * Use when you can't access the WebCertUser in the current ThreadLocal, e.g. a background job spawned
     * by a HTTP request that has manually supplied the job with the original WebCertUser.
     *
     * @return A {@link LogUser} instance
     */
    LogUser getLogUser(WebCertUser webCertUser);

}
