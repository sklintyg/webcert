/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
     * Creates a log event when a user requests an intyg as PDF.
     *
     * @param logRequest the logging details
     */
    void logPrintIntygAsPDF(LogRequest logRequest);

    /**
     * Creates a log event when a user requests an intyg as PDF.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logPrintIntygAsPDF(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user requests a print-out of an intyg as draft.
     *
     * @param logRequest the logging details
     */
    void logPrintIntygAsDraft(LogRequest logRequest);

    /**
     * Creates a log event when a user requests a print-out of an intyg as draft.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logPrintIntygAsDraft(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user requests a print-out of a revoked intyg.
     *
     * @param logRequest the logging details
     */
    void logPrintRevokedIntygAsPDF(LogRequest logRequest);

    /**
     * Creates a log event when a user requests a print-out of a revoked intyg.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logPrintRevokedIntygAsPDF(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user sends a signed intyg directly to a recipient.
     *
     * @param logRequest the logging details
     */
    void logSendIntygToRecipient(LogRequest logRequest);

    /**
     * Creates a log event when a user sends a signed intyg directly to a recipient.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logSendIntygToRecipient(LogRequest logRequest, LogUser user);

    /**
     * Creates a log event when a user sends a signed intyg directly to a recipient.
     *
     * @param patientId the id of the patient
     */
    void logShowPrediction(String patientId);

    /**
     * Creates a log event when a user shows SRS prediction.
     *
     * @param logRequest the logging details
     * @param user the user who performs the action that is being logged
     */
    void logShowPrediction(LogRequest logRequest, LogUser user);

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
