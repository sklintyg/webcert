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

package se.inera.intyg.webcert.web.service.access;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Service to check the current users right to access actions on a Certificate.
 */
public interface CertificateAccessService {
    /**
     * Check if the user is allowed to read a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param vardenhet
     *            The careUnit which the certificate belongs to.
     * @param personnummer
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToRead(String certificateType, Vardenhet vardenhet, Personnummer personnummer);

    /**
     * Check if the user is allowed to replace a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param vardenhet
     *            The careUnit which the certificate belongs to.
     * @param personnummer
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToReplace(String certificateType, Vardenhet vardenhet, Personnummer personnummer);

    /**
     * Check if the user is allowed to renew a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToRenew(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to print a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @param isEmployer
     *            If the print out is for an employer.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToPrint(String certificateType, Vardenhet careUnit, Personnummer patient, boolean isEmployer);

    /**
     * Check if the user is allowed to invalidate a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToInvalidate(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to send a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToSend(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to create a administrative question for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToCreateQuestion(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to answer a complement question for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @param newCertificate
     *            If the answer includes creating a new certificate/draft.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToAnswerComplementQuestion(String certificateType, Vardenhet careUnit, Personnummer patient, boolean newCertificate);

    /**
     * Check if the user is allowed to answer a administrative question for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToAnswerAdminQuestion(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to read questions for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToReadQuestions(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to forward questions for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToForwardQuestions(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to set complement question as handled for a certificate.
     *
     * @param certificateType
     *            The type of the certificate being checked.
     * @param careUnit
     *            The careUnit which the certificate belongs to.
     * @param patient
     *            The patient which the certificate belongs to.
     * @return
     *         AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToSetComplementAsHandled(String certificateType, Vardenhet careUnit, Personnummer patient);
}
