/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.access;

/**
 * Service to check the current users right to access actions on a Certificate.
 */
public interface CertificateAccessService {

    /**
     * Check if the user is allowed to read a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToRead(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to replace a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToReplace(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to renew a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToRenew(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to print a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToPrint(AccessEvaluationParameters accessEvaluationParameters, boolean isEmployer);

    /**
     * Check if the user is allowed to invalidate a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToInvalidate(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to send a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToSend(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to approve certificate receivers.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToApproveReceivers(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to create a administrative question for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToCreateQuestion(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to answer a complement question for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToAnswerComplementQuestion(AccessEvaluationParameters accessEvaluationParameters, boolean newCertificate);

    /**
     * Check if the user is allowed to answer a administrative question for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToAnswerAdminQuestion(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to read questions for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToReadQuestions(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to forward questions for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToForwardQuestions(AccessEvaluationParameters accessEvaluationParameters);

    /**
     * Check if the user is allowed to set complement question as handled for a certificate.
     *
     * @param accessEvaluationParameters Parameters to use for access evaluation
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToSetComplementAsHandled(AccessEvaluationParameters accessEvaluationParameters);
}
